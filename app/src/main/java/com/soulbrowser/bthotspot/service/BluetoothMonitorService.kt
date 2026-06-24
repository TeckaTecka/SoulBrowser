package com.soulbrowser.bthotspot.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.soulbrowser.bthotspot.MainActivity
import com.soulbrowser.bthotspot.R
import com.soulbrowser.bthotspot.data.PrefsRepository
import com.soulbrowser.bthotspot.hotspot.HotspotController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "BTMonitorService"
private const val NOTIFICATION_ID = 1
private const val CHANNEL_ID = "bthotspot_service"
private const val ACTION_ENABLE_HOTSPOT = "com.soulbrowser.bthotspot.ENABLE_HOTSPOT"

/**
 * Long-running foreground service that registers a BroadcastReceiver for Bluetooth
 * ACL connect/disconnect events and triggers hotspot control accordingly.
 *
 * Uses foregroundServiceType="connectedDevice" (required on Android 14+).
 */
class BluetoothMonitorService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var prefs: PrefsRepository

    private val btReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            }
            device ?: return

            val action = intent.action ?: return
            Log.d(TAG, "BT event: $action  device=${device.address}")

            scope.launch {
                val selected = prefs.selectedDevice.first() ?: return@launch
                if (selected.address != device.address) return@launch

                when (action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        Log.i(TAG, "Matched device connected — enabling hotspot")
                        HotspotController.enable(applicationContext)
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        Log.i(TAG, "Matched device disconnected — disabling hotspot")
                        HotspotController.disable(applicationContext)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = PrefsRepository(this)
        registerBluetoothReceiver()
        Log.i(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_ENABLE_HOTSPOT) {
            HotspotController.enable(applicationContext)
            return START_STICKY
        }
        startAsForeground()
        checkAlreadyConnected()
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(btReceiver)
        scope.cancel()
        super.onDestroy()
        Log.i(TAG, "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // -------- helpers --------

    @SuppressLint("MissingPermission")
    private fun checkAlreadyConnected() {
        // BLUETOOTH_CONNECT is a dangerous runtime permission on Android 12+.
        // Without it, proxy.connectedDevices throws SecurityException → crash.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "BLUETOOTH_CONNECT not granted — skipping checkAlreadyConnected")
            return
        }
        val adapter = getSystemService(BluetoothManager::class.java).adapter ?: return
        // Check both A2DP (music) and HEADSET/HFP (handsfree calls) — car stereos may use either
        for (profile in listOf(BluetoothProfile.A2DP, BluetoothProfile.HEADSET)) {
            adapter.getProfileProxy(this, object : BluetoothProfile.ServiceListener {
                private var triggered = false
                override fun onServiceConnected(p: Int, proxy: BluetoothProfile) {
                    try {
                        val addresses = proxy.connectedDevices.map { it.address }.toSet()
                        adapter.closeProfileProxy(p, proxy)
                        if (triggered) return
                        scope.launch {
                            val target = prefs.selectedDevice.first() ?: return@launch
                            if (target.address in addresses && !triggered) {
                                triggered = true
                                Log.i(TAG, "Device already connected (profile=$p) — enabling hotspot")
                                HotspotController.enable(applicationContext)
                            }
                        }
                    } catch (e: SecurityException) {
                        Log.w(TAG, "SecurityException in checkAlreadyConnected: ${e.message}")
                        adapter.closeProfileProxy(p, proxy)
                    } catch (e: Exception) {
                        Log.w(TAG, "Exception in checkAlreadyConnected: ${e.message}")
                        adapter.closeProfileProxy(p, proxy)
                    }
                }
                override fun onServiceDisconnected(p: Int) {}
            }, profile)
        }
    }

    private fun registerBluetoothReceiver() {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(btReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(btReceiver, filter)
        }
    }

    private fun startAsForeground() {
        createNotificationChannel()

        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val enableHotspotIntent = PendingIntent.getService(
            this, 1,
            Intent(this, BluetoothMonitorService::class.java).apply { action = ACTION_ENABLE_HOTSPOT },
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openAppIntent)
            .addAction(R.drawable.ic_notification, getString(R.string.enable_hotspot_action), enableHotspotIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.channel_description)
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
