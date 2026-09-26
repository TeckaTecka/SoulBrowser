package cz.teckatecka.poznamky.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import cz.teckatecka.poznamky.R
import cz.teckatecka.poznamky.data.Note
import cz.teckatecka.poznamky.data.NotesDb
import cz.teckatecka.poznamky.data.ReminderType
import cz.teckatecka.poznamky.ui.EditorActivity
import cz.teckatecka.poznamky.widget.WidgetUpdater
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** Výpočet termínů připomínek, kalendář a plánování notifikací. */
object Reminders {
    private const val CHANNEL = "reminders"
    private const val PREFS = "reminders"

    private fun monthStep(t: ReminderType) = when (t) {
        ReminderType.MONTH -> 1
        ReminderType.TWO_MONTHS -> 2
        ReminderType.QUARTER -> 3
        ReminderType.HALF_YEAR -> 6
        ReminderType.YEAR -> 12
        else -> 0
    }

    private fun weekStep(t: ReminderType) = when (t) {
        ReminderType.WEEK -> 1
        ReminderType.TWO_WEEKS -> 2
        ReminderType.FOUR_WEEKS -> 4
        else -> 0
    }

    private fun startOfDay(ms: Long): Calendar = Calendar.getInstance().apply {
        timeInMillis = ms
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

    private fun daysBetween(a: Calendar, b: Calendar): Long =
        Math.round((b.timeInMillis - a.timeInMillis) / TimeUnit.DAYS.toMillis(1).toDouble())

    /** Kotva pro 2/4týdenní opakování: první zvolený den v týdnu od data začátku. */
    private fun weekAnchor(n: Note): Calendar = startOfDay(n.reminderOneTimeDate).apply {
        while (get(Calendar.DAY_OF_WEEK) != n.reminderWeekDay) add(Calendar.DAY_OF_MONTH, 1)
    }

    /** Připadá připomínka poznámky na daný den? (kalendář, widgety Dnes/Datum) */
    fun occursOn(n: Note, dayMs: Long): Boolean {
        if (!n.reminderEnabled || n.deleted) return false
        val day = startOfDay(dayMs)
        val type = n.reminderType
        return when {
            type == ReminderType.ONE_TIME -> daysBetween(startOfDay(n.reminderOneTimeDate), day) == 0L
            type == ReminderType.DAY -> true
            weekStep(type) > 0 -> day.get(Calendar.DAY_OF_WEEK) == n.reminderWeekDay &&
                Math.floorMod(daysBetween(weekAnchor(n), day), 7L * weekStep(type)) == 0L
            else -> {
                val step = monthStep(type)
                val monthOk = step == 1 || Math.floorMod(day.get(Calendar.MONTH) + 1 - n.reminderYearMonth, step) == 0
                val maxDay = day.getActualMaximum(Calendar.DAY_OF_MONTH)
                monthOk && day.get(Calendar.DAY_OF_MONTH) == minOf(n.reminderMonthDay, maxDay)
            }
        }
    }

    /** Nejbližší termín (včetně času) po [now]. U jednorázové je to přímo zvolené datum. */
    fun nextDate(n: Note, now: Long = System.currentTimeMillis()): Long {
        if (n.reminderType == ReminderType.ONE_TIME) return n.reminderOneTimeDate
        val time = Calendar.getInstance().apply { timeInMillis = n.reminderOneTimeDate }
        val c = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, time.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        if (c.timeInMillis <= now) c.add(Calendar.DAY_OF_MONTH, 1)
        // Hledáme den po dni – nejvýš ~13 měsíců, což pokryje i roční opakování.
        repeat(400) {
            if (occursOn(n, c.timeInMillis)) return c.timeInMillis
            c.add(Calendar.DAY_OF_MONTH, 1)
        }
        return 0
    }

    fun notesOn(context: Context, dayMs: Long): List<Note> {
        val minutes = { n: Note -> Calendar.getInstance().apply { timeInMillis = n.reminderOneTimeDate }
            .let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) } }
        return NotesDb.get(context).activeCalendarNotes().filter { occursOn(it, dayMs) }.sortedBy(minutes)
    }

    // ---------- notifikace ----------

    private fun firedKey(id: Long) = "fired_$id"

    /** Termín, na který má připomínka ještě zazvonit (null = nic nečeká). */
    private fun pendingTime(n: Note, now: Long, prefs: android.content.SharedPreferences): Long? {
        var t = nextDate(n, now - 60_000)
        if (prefs.getLong(firedKey(n.id), 0) == t) {
            if (n.reminderType == ReminderType.ONE_TIME) return null
            t = nextDate(n, t + 1)
        }
        return t.takeIf { it > 0 && it > now - 60_000 }
    }

    /** Naplánuje jediný alarm na nejbližší nevyřízenou připomínku. */
    fun scheduleAll(context: Context) {
        val now = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val next = NotesDb.get(context).activeCalendarNotes()
            .filter { it.reminderNotification }
            .mapNotNull { pendingTime(it, now, prefs) }
            .minOrNull()
        val am = context.getSystemService(AlarmManager::class.java)
        val pi = PendingIntent.getBroadcast(
            context, 0, Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        am.cancel(pi)
        if (next == null) return
        val at = maxOf(next, now + 1000)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    /** Zobrazí notifikace pro všechny právě splatné připomínky. */
    fun fireDue(context: Context) {
        val now = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        NotesDb.get(context).activeCalendarNotes().filter { it.reminderNotification }.forEach { n ->
            val t = pendingTime(n, now, prefs)
            if (t != null && t <= now + 30_000) {
                notify(context, n)
                prefs.edit().putLong(firedKey(n.id), t).apply()
            }
        }
        scheduleAll(context)
        WidgetUpdater.updateAll(context)
    }

    /** „Upozornit hned“ z Nastavení kalendáře. */
    fun notifyNow(context: Context, n: Note) = notify(context, n, force = true)

    private fun notify(context: Context, n: Note, force: Boolean = false) {
        val settings = cz.teckatecka.poznamky.data.Settings(context)
        if (!force && !settings.remindersOn) return
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val nm = context.getSystemService(NotificationManager::class.java)
        // Vibrace nejde u kanálu měnit po vytvoření – proto dva kanály podle nastavení „Vibrační signál“.
        val vibrate = settings.reminderVibrate
        val channelId = if (vibrate) CHANNEL else CHANNEL + "_silent"
        nm.createNotificationChannel(
            NotificationChannel(channelId, context.getString(R.string.channel_reminders), NotificationManager.IMPORTANCE_HIGH)
                .apply { enableVibration(vibrate) },
        )
        val open = PendingIntent.getActivity(
            context, n.id.toInt(), EditorActivity.intent(context, n.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val text = n.plainText().take(500)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_note)
            .setContentTitle(n.title.ifBlank { context.getString(R.string.app_name) })
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(n.id.toInt(), notification)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Reminders.fireDue(context)
}

/** Po restartu telefonu / aktualizaci appky znovu naplánuje alarmy a obnoví widgety. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Reminders.scheduleAll(context)
        WidgetUpdater.updateAll(context)
    }
}
