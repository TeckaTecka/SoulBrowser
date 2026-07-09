package com.soulbrowser.bthotspot.client

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

private const val TAG = "WifiMonitor"
private const val ONGOING_CHANNEL = "client_ongoing"
private const val ALERT_CHANNEL = "client_alert"
private const val ONGOING_ID = 1
private const val ALERT_ID = 2

const val PREFS = "client"
const val KEY_SOUND_URI = "sound_uri"
const val KEY_NOTIFY_ENABLED = "notify_enabled"
const val KEY_SOUND_ENABLED = "sound_enabled"
const val KEY_VIBRATE_ENABLED = "vibrate_enabled"

/**
 * Runs on the in-car device. Watches for a WiFi connection (the phone's hotspot) and, when it
 * comes up, plays a sound + shows a heads-up notification — so you know you're online even while
 * Waze covers the screen.
 */
class WifiMonitorService : Service() {

    private lateinit var cm: ConnectivityManager
    private var callback: ConnectivityManager.NetworkCallback? = null
    private var connected = false

    override fun onCreate() {
        super.onCreate()
        cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        createChannels()
        startForeground(ONGOING_ID, ongoingNotification())
        registerCallback()
        Log.i(TAG, "started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        callback?.let { runCatching { cm.unregisterNetworkCallback(it) } }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerCallback() {
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = onWifiChanged(true)
            override fun onLost(network: Network) = onWifiChanged(false)
        }
        callback = cb
        // Ignore the current state at startup so we only alert on a real new connection.
        connected = isWifiConnectedNow()
        cm.registerNetworkCallback(request, cb)
    }

    private fun isWifiConnectedNow(): Boolean {
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun onWifiChanged(nowConnected: Boolean) {
        if (nowConnected == connected) return
        connected = nowConnected
        Log.i(TAG, "wifi connected=$nowConnected")
        if (nowConnected) {
            val p = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            if (p.getBoolean(KEY_SOUND_ENABLED, true)) playSound()
            if (p.getBoolean(KEY_VIBRATE_ENABLED, false)) vibrate()
            if (p.getBoolean(KEY_NOTIFY_ENABLED, true)) alertConnected()
        } else {
            getSystemService(NotificationManager::class.java).cancel(ALERT_ID)
        }
    }

    private fun playSound() {
        try {
            val stored = getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_SOUND_URI, null)
            val uri: Uri = if (!stored.isNullOrEmpty()) Uri.parse(stored)
            else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(applicationContext, uri)?.play()
        } catch (e: Exception) {
            Log.w(TAG, "playSound failed: ${e.message}")
        }
    }

    private fun vibrate() {
        try {
            val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(400)
            }
        } catch (_: Exception) {}
    }

    private fun alertConnected() {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(this, ALERT_CHANNEL)
            .setContentTitle("Připojeno k hotspotu")
            .setContentText("Telefon je připojen — internet v autě je aktivní")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_STATUS)
            .build()
        getSystemService(NotificationManager::class.java).notify(ALERT_ID, n)
    }

    private fun ongoingNotification(): Notification =
        Notification.Builder(this, ONGOING_CHANNEL)
            .setContentTitle("Hlídání hotspotu běží")
            .setContentText("Upozorní zvukem po připojení k hotspotu")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .build()

    private fun createChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(ONGOING_CHANNEL, "Stav služby", NotificationManager.IMPORTANCE_LOW).apply {
                setShowBadge(false)
            }
        )
        nm.createNotificationChannel(
            // High importance → heads-up popup over Waze. Sound is played separately so the
            // user can pick it; keep the channel itself silent to avoid a double sound.
            NotificationChannel(ALERT_CHANNEL, "Připojení k hotspotu", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null)
                enableVibration(false)
            }
        )
    }
}
