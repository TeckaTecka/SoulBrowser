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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

private const val TAG = "WifiMonitor"
private const val ONGOING_CHANNEL = "client_ongoing"
private const val ALERT_CHANNEL = "client_alert"
private const val ONGOING_ID = 1
private const val ALERT_ID = 2
private const val REPEAT_CAP = 20
private const val ACTION_ACK = "com.soulbrowser.bthotspot.client.ACK"

const val PREFS = "client"
const val KEY_SOUND_URI = "sound_uri"
const val KEY_SOUND_DISCONNECT_URI = "sound_disconnect_uri"
const val KEY_NOTIFY_ENABLED = "notify_enabled"
const val KEY_SOUND_ENABLED = "sound_enabled"
const val KEY_VIBRATE_ENABLED = "vibrate_enabled"
const val KEY_NOTIFY_DISCONNECT = "notify_disconnect"
const val KEY_REPEAT_ENABLED = "repeat_enabled"
const val KEY_REPEAT_INTERVAL = "repeat_interval_sec"

/**
 * Runs on the in-car device. Watches for a WiFi connection (the phone's hotspot) and alerts on
 * connect (and optionally on disconnect) with a sound + heads-up notification — so you know you're
 * online even while Waze covers the screen. Can repeat the connect sound until acknowledged.
 */
class WifiMonitorService : Service() {

    private lateinit var cm: ConnectivityManager
    private var callback: ConnectivityManager.NetworkCallback? = null
    private var connected = false

    private val repeatHandler = Handler(Looper.getMainLooper())
    private var repeatCount = 0
    private val repeatRunnable = object : Runnable {
        override fun run() {
            if (repeatCount >= REPEAT_CAP) return
            repeatCount++
            playSound(KEY_SOUND_URI)
            repeatHandler.postDelayed(this, repeatIntervalMs())
        }
    }

    override fun onCreate() {
        super.onCreate()
        cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        createChannels()
        startForeground(ONGOING_ID, ongoingNotification())
        registerCallback()
        Log.i(TAG, "started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_ACK) {
            stopRepeat()
            getSystemService(NotificationManager::class.java).cancel(ALERT_ID)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopRepeat()
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
        val p = prefs()
        if (nowConnected) {
            stopRepeat()
            if (p.getBoolean(KEY_SOUND_ENABLED, true)) playSound(KEY_SOUND_URI)
            if (p.getBoolean(KEY_VIBRATE_ENABLED, false)) vibrate()
            if (p.getBoolean(KEY_NOTIFY_ENABLED, true)) alertConnected(p.getBoolean(KEY_REPEAT_ENABLED, false))
            if (p.getBoolean(KEY_REPEAT_ENABLED, false) && p.getBoolean(KEY_SOUND_ENABLED, true)) startRepeat()
        } else {
            stopRepeat()
            getSystemService(NotificationManager::class.java).cancel(ALERT_ID)
            if (p.getBoolean(KEY_NOTIFY_DISCONNECT, false)) {
                if (p.getBoolean(KEY_SOUND_ENABLED, true)) playSound(KEY_SOUND_DISCONNECT_URI)
                alertDisconnected()
            }
        }
    }

    private fun startRepeat() {
        repeatCount = 0
        repeatHandler.postDelayed(repeatRunnable, repeatIntervalMs())
    }

    private fun stopRepeat() {
        repeatHandler.removeCallbacks(repeatRunnable)
    }

    private fun repeatIntervalMs(): Long {
        val sec = prefs().getInt(KEY_REPEAT_INTERVAL, 10).coerceIn(3, 120)
        return sec * 1000L
    }

    private fun playSound(uriKey: String) {
        try {
            val stored = prefs().getString(uriKey, null)
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

    private fun alertConnected(withAck: Boolean) {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val builder = Notification.Builder(this, ALERT_CHANNEL)
            .setContentTitle("Připojeno k hotspotu")
            .setContentText("Telefon je připojen — internet v autě je aktivní")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_STATUS)
        if (withAck) {
            val ack = PendingIntent.getService(
                this, 1,
                Intent(this, WifiMonitorService::class.java).setAction(ACTION_ACK),
                PendingIntent.FLAG_IMMUTABLE
            )
            @Suppress("DEPRECATION")
            builder.setOngoing(true)
                .addAction(Notification.Action.Builder(0, "Potvrdit", ack).build())
        }
        getSystemService(NotificationManager::class.java).notify(ALERT_ID, builder.build())
    }

    private fun alertDisconnected() {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(this, ALERT_CHANNEL)
            .setContentTitle("Odpojeno od hotspotu")
            .setContentText("Spojení s telefonem bylo přerušeno")
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

    private fun prefs() = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

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
