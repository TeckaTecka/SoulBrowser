package com.teckatecka.netadmin.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.teckatecka.netadmin.NetAdminApp
import com.teckatecka.netadmin.R
import com.teckatecka.netadmin.data.db.AppDatabase
import com.teckatecka.netadmin.data.model.MonitoredHostEntity
import com.teckatecka.netadmin.utils.IpUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MonitoringWorker(
    private val ctx: WorkerParameters,
    appContext: Context
) : CoroutineWorker(appContext, ctx) {

    companion object {
        const val CHANNEL_ID   = "monitoring_alerts"
        const val WORK_NAME    = "host_monitoring"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val db  = AppDatabase.getInstance(applicationContext)
        val dao = db.monitoredHostDao()
        val hosts = dao.getAllOnce()

        ensureNotificationChannel()

        hosts.forEach { host ->
            val reachable = IpUtils.ping(host.address, timeoutMs = 3000) >= 0
            val wasUp = host.isUp

            if (wasUp && !reachable) {
                dao.update(host.copy(isUp = false, lastDownAt = System.currentTimeMillis(), lastChecked = System.currentTimeMillis()))
                sendNotification(
                    id      = host.id.toInt(),
                    title   = applicationContext.getString(R.string.monitoring_alert_down_title),
                    body    = applicationContext.getString(R.string.monitoring_alert_down_body, host.label.ifEmpty { host.address })
                )
            } else if (!wasUp && reachable) {
                dao.update(host.copy(isUp = true, lastChecked = System.currentTimeMillis()))
                sendNotification(
                    id      = (host.id + 100_000).toInt(),
                    title   = applicationContext.getString(R.string.monitoring_alert_up_title),
                    body    = applicationContext.getString(R.string.monitoring_alert_up_body, host.label.ifEmpty { host.address })
                )
            } else {
                dao.update(host.copy(lastChecked = System.currentTimeMillis()))
            }
        }

        Result.success()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                applicationContext.getString(R.string.monitoring_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = applicationContext.getString(R.string.monitoring_channel_desc)
            }
            val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(id: Int, title: String, body: String) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_monitoring)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(id, notification)
    }
}
