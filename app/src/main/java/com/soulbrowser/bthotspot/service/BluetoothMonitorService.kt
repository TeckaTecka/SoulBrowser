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
import com.soulbrowser.bthotspot.data.EventLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
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
    private var heartbeatStarted = false
    private var pendingDisable: Job? = null

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
                val watched = prefs.selectedDevices.first()
                if (watched.none { it.address == device.address }) return@launch

                if (!prefs.serviceEnabled.first()) {
                    Log.i(TAG, "automation paused — ignoring $action")
                    return@launch
                }

                when (action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        // A reconnect cancels any pending delayed disable.
                        if (pendingDisable?.isActive == true) {
                            pendingDisable?.cancel()
                            EventLog.log(applicationContext, "Auto se vrátilo — vypnutí zrušeno")
                        }
                        if (prefs.skipWhenOnWifiInternet.first() && onWifiInternet()) {
                            Log.i(TAG, "on WiFi with internet — skipping enable")
                            EventLog.log(applicationContext, "Auto připojeno, ale telefon je na WiFi s internetem → hotspot nezapínám")
                            return@launch
                        }
                        Log.i(TAG, "Matched device connected — enabling hotspot")
                        EventLog.log(applicationContext, "Auto připojeno → zapínám hotspot")
                        HotspotController.setState(applicationContext, true) { ok ->
                            if (ok) scope.launch { HotspotEvents.onEnabled(applicationContext) }
                        }
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        if (prefs.autoDisableOnDisconnect.first()) {
                            scheduleDisable()
                        } else {
                            Log.i(TAG, "Matched device disconnected — auto-disable off, leaving hotspot on")
                            EventLog.log(applicationContext, "Auto odpojeno — auto-vypnutí je vypnuté, hotspot ponechán")
                        }
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
        // Always enter the foreground first so we never violate the startForeground()
        // timeout contract (a source of crashes when started in the background).
        try {
            startAsForeground()
        } catch (e: Exception) {
            Log.e(TAG, "startForeground failed: ${e.javaClass.simpleName}: ${e.message}")
        }
        try {
            if (intent?.action == ACTION_ENABLE_HOTSPOT) {
                HotspotController.enable(applicationContext)
            } else {
                checkAlreadyConnected()
            }
            ServiceWatchdog.schedule(applicationContext)
            startHeartbeat()
        } catch (e: Exception) {
            Log.e(TAG, "onStartCommand error: ${e.javaClass.simpleName}: ${e.message}")
        }
        return START_STICKY
    }

    /** Disable the hotspot, honouring the configurable grace period (0..600 s). */
    private suspend fun scheduleDisable() {
        pendingDisable?.cancel()
        val delaySec = prefs.disconnectDelaySeconds.first()
        if (delaySec <= 0) {
            Log.i(TAG, "disconnected — disabling now")
            EventLog.log(applicationContext, "Auto odpojeno → vypínám hotspot")
            disableHotspot()
            return
        }
        EventLog.log(applicationContext, "Auto odpojeno → vypnutí naplánováno za ${delaySec}s")
        pendingDisable = scope.launch {
            delay(delaySec * 1000L)
            Log.i(TAG, "disconnect grace elapsed — disabling")
            EventLog.log(applicationContext, "Prodleva ${delaySec}s uplynula → vypínám hotspot")
            disableHotspot()
        }
    }

    private fun disableHotspot() {
        HotspotController.setState(applicationContext, false) { ok ->
            if (ok) scope.launch { HotspotEvents.onDisabled(applicationContext) }
        }
    }

    /** True if the phone currently routes through a WiFi network that has validated internet. */
    private fun onWifiInternet(): Boolean {
        return try {
            val cm = getSystemService(android.net.ConnectivityManager::class.java)
            val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) &&
                caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            false
        }
    }

    /** Every 60s while alive: post/clear the "accessibility disabled" warning. */
    private fun startHeartbeat() {
        if (heartbeatStarted) return
        heartbeatStarted = true
        scope.launch {
            while (isActive) {
                ServiceWatchdog.checkHealth(applicationContext)
                delay(60_000)
            }
        }
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
                            val targets = prefs.selectedDevices.first().map { it.address }.toSet()
                            if (targets.any { it in addresses } && !triggered) {
                                triggered = true
                                Log.i(TAG, "Device already connected (profile=$p) — enabling hotspot")
                                EventLog.log(applicationContext, "Auto už připojené při startu → zapínám hotspot")
                                HotspotController.setState(applicationContext, true) { ok ->
                                    if (ok) scope.launch { HotspotEvents.onEnabled(applicationContext) }
                                }
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
