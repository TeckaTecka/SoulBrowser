package com.soulbrowser.bthotspot.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.soulbrowser.bthotspot.R
import com.soulbrowser.bthotspot.data.PrefsRepository
import kotlinx.coroutines.flow.first

private const val TAG = "HotspotEvents"

/**
 * Optional user-facing reactions when the hotspot is turned on/off:
 *  - plays a user-chosen sound (independent per direction),
 *  - posts a normal notification that Garmin Connect (and other companion apps) can mirror
 *    to a watch, just like SMS/Telegram.
 * All parts are opt-in via preferences.
 */
object HotspotEvents {

    private const val EVENT_CHANNEL_ID = "bthotspot_events"
    private const val EVENT_NOTIFICATION_ID = 3

    // Last state we announced. Prevents duplicate sound/notification for the same state
    // (e.g. the 15-min catch-up re-confirming an already-enabled hotspot).
    @Volatile
    private var lastAnnounced: Boolean? = null

    suspend fun onEnabled(context: Context) {
        if (lastAnnounced == true) return
        lastAnnounced = true
        fire(
            context,
            soundUri = PrefsRepository(context).enableSoundUri.first(),
            title = context.getString(R.string.event_enabled_title)
        )
    }

    suspend fun onDisabled(context: Context) {
        if (lastAnnounced == false) return
        lastAnnounced = false
        fire(
            context,
            soundUri = PrefsRepository(context).disableSoundUri.first(),
            title = context.getString(R.string.event_disabled_title)
        )
    }

    private suspend fun fire(context: Context, soundUri: String?, title: String) {
        playSound(context, soundUri)
        if (PrefsRepository(context).eventNotificationsEnabled.first()) {
            postNotification(context, title)
        }
    }

    private fun playSound(context: Context, uri: String?) {
        if (uri.isNullOrEmpty()) return
        try {
            RingtoneManager.getRingtone(context, Uri.parse(uri))?.play()
        } catch (e: Exception) {
            Log.w(TAG, "Could not play sound: ${e.message}")
        }
    }

    private fun postNotification(context: Context, title: String) {
        createChannel(context)
        // Silent on the phone (a custom sound may already be playing); the watch still shows it.
        val notification = NotificationCompat.Builder(context, EVENT_CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSilent(true)
            .setAutoCancel(true)
            .setTimeoutAfter(60_000)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(EVENT_NOTIFICATION_ID, notification)
    }

    private fun createChannel(context: Context) {
        val channel = NotificationChannel(
            EVENT_CHANNEL_ID,
            context.getString(R.string.event_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.event_channel_desc)
            setShowBadge(false)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
