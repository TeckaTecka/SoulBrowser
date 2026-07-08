package com.soulbrowser.bthotspot.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.soulbrowser.bthotspot.R
import com.soulbrowser.bthotspot.data.PrefsRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

private const val TAG = "ServiceWatchdog"

/**
 * Keeps the app functional on aggressive OEMs (Xiaomi/HyperOS) that kill background apps
 * and silently disable the accessibility service ("loses the button").
 *
 * Two layers:
 *  1. A periodic WorkManager job (every 15 min, survives process death) that restarts the
 *     monitor service and re-checks health.
 *  2. checkHealth(): if the accessibility service is no longer connected, posts a prominent
 *     warning notification that opens Accessibility settings so the user can re-enable it —
 *     an app cannot re-enable its own accessibility service programmatically.
 */
object ServiceWatchdog {

    private const val WORK_NAME = "bthotspot_watchdog"
    private const val WARN_CHANNEL_ID = "bthotspot_warning"
    private const val WARN_NOTIFICATION_ID = 2

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<WatchdogWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
        Log.i(TAG, "Watchdog scheduled")
    }

    /** Best-effort restart of the foreground monitor service (may be blocked on Android 12+
     *  unless the app is exempt from battery optimizations — hence wrapped in try/catch). */
    fun ensureServiceRunning(context: Context) {
        val intent = Intent(context, BluetoothMonitorService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not (re)start service: ${e.javaClass.simpleName}")
        }
    }

    /** Posts or clears the "accessibility service is off" warning notification. */
    suspend fun checkHealth(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        // Respect the user's toggle — if warnings are off, never show one.
        if (!PrefsRepository(context).watchdogWarningEnabled.first()) {
            nm.cancel(WARN_NOTIFICATION_ID)
            return
        }
        createWarningChannel(context)
        if (HotspotAccessibilityService.isConnected()) {
            nm.cancel(WARN_NOTIFICATION_ID)
            return
        }
        val settingsIntent = PendingIntent.getActivity(
            context, 2,
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, WARN_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.watchdog_warn_title))
            .setContentText(context.getString(R.string.watchdog_warn_text))
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.watchdog_warn_text)))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(settingsIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(WARN_NOTIFICATION_ID, notification)
        Log.w(TAG, "Accessibility service not connected — warning posted")
    }

    private fun createWarningChannel(context: Context) {
        val channel = NotificationChannel(
            WARN_CHANNEL_ID,
            context.getString(R.string.watchdog_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.watchdog_channel_desc)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}

class WatchdogWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Log.i(TAG, "Watchdog tick")
        ServiceWatchdog.ensureServiceRunning(applicationContext)
        ServiceWatchdog.checkHealth(applicationContext)
        // Safety net: if the car is connected but the hotspot is off, turn it on.
        try {
            CatchUp.enableIfCarConnected(applicationContext)
        } catch (e: Exception) {
            Log.w(TAG, "catch-up failed: ${e.message}")
        }
        return Result.success()
    }
}
