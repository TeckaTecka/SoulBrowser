package cz.teckatecka.poznamky.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import cz.teckatecka.poznamky.R
import cz.teckatecka.poznamky.data.Note
import cz.teckatecka.poznamky.data.NoteTab
import cz.teckatecka.poznamky.data.NotesDb
import cz.teckatecka.poznamky.data.Repo
import cz.teckatecka.poznamky.data.Settings
import cz.teckatecka.poznamky.data.contrastTextColor
import cz.teckatecka.poznamky.reminder.Reminders
import cz.teckatecka.poznamky.ui.EditorActivity
import cz.teckatecka.poznamky.ui.MainActivity
import cz.teckatecka.poznamky.ui.SelectNoteActivity
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Barevné varianty widgetu – jako bílý / černý / průhledný widget v původní appce. */
enum class WidgetStyle(val bg: Int, val alpha: Int, val text: Int) {
    WHITE(0xFFFFFFFF.toInt(), 255, 0xFF202124.toInt()),
    BLACK(0xFF202124.toInt(), 255, 0xFFF1F3F4.toInt()),
    TRANSPARENT(0xFF000000.toInt(), 90, 0xFFFFFFFF.toInt());

    /** Pozadí a barva textu pro konkrétní poznámku (vlastní barva poznámky má přednost). */
    fun colorsFor(note: Note?): Triple<Int, Int, Int> {
        val color = note?.color ?: 0
        val bgColor = if (color != 0) color else bg
        val a = if (color != 0) (if (this == TRANSPARENT) 200 else 255) else alpha
        val txt = when {
            note != null && note.fontColor != 0 -> note.fontColor
            color != 0 -> contrastTextColor(color)
            else -> text
        }
        return Triple(bgColor, a, txt)
    }
}

/** Uložený stav jednotlivých widgetů (vybraná poznámka, datum, index). */
object WidgetPrefs {
    private fun p(c: Context) = c.getSharedPreferences("widgets", Context.MODE_PRIVATE)
    fun noteId(c: Context, w: Int) = p(c).getLong("note_$w", Note.NEW_ID)
    fun setNoteId(c: Context, w: Int, id: Long) = p(c).edit().putLong("note_$w", id).apply()
    /** 0 = vždy dnešek, jinak konkrétní den (začátek dne v ms). */
    fun date(c: Context, w: Int) = p(c).getLong("date_$w", 0L)
    fun setDate(c: Context, w: Int, d: Long) = p(c).edit().putLong("date_$w", d).apply()
    fun index(c: Context, w: Int) = p(c).getInt("idx_$w", 0)
    fun setIndex(c: Context, w: Int, i: Int) = p(c).edit().putInt("idx_$w", i).apply()
    fun remove(c: Context, w: Int) = p(c).edit().remove("note_$w").remove("date_$w").remove("idx_$w").apply()

    fun effectiveDate(c: Context, w: Int): Long = date(c, w).takeIf { it != 0L } ?: startOfToday()
}

fun startOfToday(): Long = startOfDay(System.currentTimeMillis())
fun startOfDay(ms: Long): Long = Calendar.getInstance().apply {
    timeInMillis = ms
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

fun dayTitle(context: Context, day: Long): String {
    val fmt = DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEEEdMMMM")
    val text = DateFormat.format(fmt, Date(day)).toString()
    return if (startOfDay(day) == startOfToday()) context.getString(R.string.today) + ", " + text else text
}

private fun shiftDay(day: Long, delta: Int): Long =
    Calendar.getInstance().apply { timeInMillis = day; add(Calendar.DAY_OF_MONTH, delta) }.timeInMillis.let(::startOfDay)

object WidgetUpdater {
    const val ACTION_TOGGLE = "cz.teckatecka.poznamky.TOGGLE"
    const val ACTION_OPEN = "cz.teckatecka.poznamky.OPEN"
    const val EXTRA_NOTE = "note_id"
    const val EXTRA_INDEX = "item_index"

    private val noteProviders = mapOf(
        NoteWidgetWhite::class.java to WidgetStyle.WHITE,
        NoteWidgetBlack::class.java to WidgetStyle.BLACK,
        NoteWidgetTransparent::class.java to WidgetStyle.TRANSPARENT,
    )

    fun updateAll(context: Context) {
        val m = AppWidgetManager.getInstance(context)
        noteProviders.forEach { (cls, style) ->
            m.getAppWidgetIds(ComponentName(context, cls)).forEach { renderNote(context, m, it, style) }
        }
        m.getAppWidgetIds(ComponentName(context, TodayWidget::class.java)).forEach { renderToday(context, m, it) }
        m.getAppWidgetIds(ComponentName(context, DateWidget::class.java)).forEach { renderDate(context, m, it) }
        notifyListsChanged(context)
        scheduleMidnight(context)
    }

    fun styleOf(context: Context, widgetId: Int): WidgetStyle {
        val info = AppWidgetManager.getInstance(context).getAppWidgetInfo(widgetId) ?: return WidgetStyle.WHITE
        return noteProviders.entries.firstOrNull { it.key.name == info.provider.className }?.value ?: WidgetStyle.WHITE
    }

    // ---------- společné ----------

    private fun listAdapterIntent(context: Context, widgetId: Int, kind: String) =
        Intent(context, WidgetListService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            putExtra(WidgetListService.EXTRA_KIND, kind)
            // Unikátní data, jinak systém sdílí jednu factory pro všechny widgety.
            data = Uri.parse("poznamky://widget/$kind/$widgetId/${System.nanoTime()}")
        }

    private fun actionTemplate(context: Context, widgetId: Int) = PendingIntent.getBroadcast(
        context, widgetId, Intent(context, WidgetActionReceiver::class.java).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
    )

    private fun activityPi(context: Context, requestCode: Int, intent: Intent) = PendingIntent.getActivity(
        context, requestCode, intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun selfPi(context: Context, cls: Class<*>, widgetId: Int, action: String) = PendingIntent.getBroadcast(
        context, widgetId * 10 + DAY_ACTIONS.indexOf(action),
        Intent(context, cls).setAction(action).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun RemoteViews.applyColors(bg: Int, alpha: Int, text: Int, vararg texts: Int) {
        setInt(R.id.bg, "setColorFilter", bg)
        setInt(R.id.bg, "setImageAlpha", alpha)
        texts.forEach { setTextColor(it, text) }
        listOf(R.id.btn_add, R.id.btn_browse, R.id.btn_prev_date, R.id.btn_next_date, R.id.btn_prev_note, R.id.btn_next_note)
            .forEach { setInt(it, "setColorFilter", text) }
    }

    private fun infoLine(context: Context, n: Note): String {
        val s = Settings(context)
        val df = DateFormat.getDateFormat(context)
        val tf = DateFormat.getTimeFormat(context)
        val parts = mutableListOf<String>()
        if (s.widgetShowReminder && n.reminderEnabled && n.reminderNextDate > 0) {
            parts += "⏰ " + df.format(Date(n.reminderNextDate)) + " " + tf.format(Date(n.reminderNextDate))
        }
        if (s.widgetShowCreated && n.createdTimeStamp > 0) parts += "✚ " + df.format(Date(n.createdTimeStamp))
        if (s.widgetShowModified && n.timeStamp > 0) parts += "✎ " + df.format(Date(n.timeStamp)) + " " + tf.format(Date(n.timeStamp))
        return parts.joinToString("   ")
    }

    // ---------- widget Poznámka ----------

    fun renderNote(context: Context, m: AppWidgetManager, widgetId: Int, style: WidgetStyle = styleOf(context, widgetId)) {
        val note = NotesDb.get(context).note(WidgetPrefs.noteId(context, widgetId))?.takeIf { !it.deleted }
        val rv = RemoteViews(context.packageName, R.layout.widget_note)
        val (bg, alpha, text) = style.colorsFor(note)
        rv.applyColors(bg, alpha, text, R.id.title, R.id.empty, R.id.info_text)

        val select = activityPi(context, widgetId, SelectNoteActivity.intent(context, widgetId))
        rv.setOnClickPendingIntent(R.id.btn_browse, select)
        rv.setOnClickPendingIntent(R.id.btn_add, activityPi(context, 100_000 + widgetId, EditorActivity.intent(context, Note.NEW_ID, widgetId = widgetId)))

        if (note == null) {
            rv.setTextViewText(R.id.title, context.getString(R.string.app_name))
            rv.setOnClickPendingIntent(R.id.title, select)
            rv.setOnClickPendingIntent(R.id.empty, select)
            rv.setViewVisibility(R.id.info, View.GONE)
        } else {
            rv.setTextViewText(R.id.title, note.title)
            rv.setViewVisibility(R.id.title, if (note.title.isBlank()) View.INVISIBLE else View.VISIBLE)
            rv.setTextViewTextSize(R.id.title, TypedValue.COMPLEX_UNIT_SP, note.effectiveTitleFontSize.toFloat())
            rv.setOnClickPendingIntent(R.id.title, activityPi(context, 200_000 + widgetId, EditorActivity.intent(context, note.id)))
            val info = infoLine(context, note)
            rv.setTextViewText(R.id.info_text, info)
            rv.setViewVisibility(R.id.info, if (info.isEmpty()) View.GONE else View.VISIBLE)
        }
        rv.setRemoteAdapter(R.id.list, listAdapterIntent(context, widgetId, WidgetListService.KIND_NOTE))
        rv.setEmptyView(R.id.list, R.id.empty)
        rv.setPendingIntentTemplate(R.id.list, actionTemplate(context, widgetId))
        m.updateAppWidget(widgetId, rv)
    }

    // ---------- widget Dnes (seznam poznámek dne) ----------

    fun renderToday(context: Context, m: AppWidgetManager, widgetId: Int) {
        val day = WidgetPrefs.effectiveDate(context, widgetId)
        val rv = RemoteViews(context.packageName, R.layout.widget_today)
        val (bg, alpha, text) = WidgetStyle.TRANSPARENT.colorsFor(null)
        rv.applyColors(bg, alpha, text, R.id.date_title, R.id.empty)
        rv.setTextViewText(R.id.date_title, dayTitle(context, day))
        rv.setOnClickPendingIntent(R.id.btn_prev_date, selfPi(context, TodayWidget::class.java, widgetId, ACTION_PREV_DATE))
        rv.setOnClickPendingIntent(R.id.btn_next_date, selfPi(context, TodayWidget::class.java, widgetId, ACTION_NEXT_DATE))
        rv.setOnClickPendingIntent(R.id.date_title, selfPi(context, TodayWidget::class.java, widgetId, ACTION_TODAY))
        rv.setOnClickPendingIntent(R.id.btn_add, activityPi(context, 300_000 + widgetId, EditorActivity.intent(context, Note.NEW_ID, reminderDay = day)))
        rv.setOnClickPendingIntent(R.id.empty, activityPi(context, 400_000 + widgetId, MainActivity.intent(context, NoteTab.CALENDAR_ID)))
        rv.setRemoteAdapter(R.id.list, listAdapterIntent(context, widgetId, WidgetListService.KIND_DAY))
        rv.setEmptyView(R.id.list, R.id.empty)
        rv.setPendingIntentTemplate(R.id.list, actionTemplate(context, widgetId))
        m.updateAppWidget(widgetId, rv)
    }

    // ---------- widget Datum (jedna poznámka dne, listování) ----------

    fun dateWidgetNote(context: Context, widgetId: Int): Pair<Note?, Pair<Int, Int>> {
        val notes = Reminders.notesOn(context, WidgetPrefs.effectiveDate(context, widgetId))
        if (notes.isEmpty()) return null to (0 to 0)
        val i = WidgetPrefs.index(context, widgetId).coerceIn(0, notes.size - 1)
        return notes[i] to (i to notes.size)
    }

    fun renderDate(context: Context, m: AppWidgetManager, widgetId: Int) {
        val day = WidgetPrefs.effectiveDate(context, widgetId)
        val (note, pos) = dateWidgetNote(context, widgetId)
        val rv = RemoteViews(context.packageName, R.layout.widget_date)
        val (bg, alpha, text) = WidgetStyle.TRANSPARENT.colorsFor(note)
        rv.applyColors(bg, alpha, text, R.id.date_title, R.id.title, R.id.counter, R.id.empty, R.id.info_text)
        rv.setTextViewText(R.id.date_title, dayTitle(context, day))
        rv.setTextViewText(R.id.title, note?.title ?: "")
        rv.setTextViewText(R.id.counter, if (pos.second > 0) "${pos.first + 1}/${pos.second}" else "0/0")
        val navVis = if (pos.second > 1) View.VISIBLE else View.INVISIBLE
        rv.setViewVisibility(R.id.btn_prev_note, navVis)
        rv.setViewVisibility(R.id.btn_next_note, navVis)
        val info = note?.let { infoLine(context, it) } ?: ""
        rv.setTextViewText(R.id.info_text, info)
        rv.setViewVisibility(R.id.info, if (info.isEmpty()) View.GONE else View.VISIBLE)
        rv.setOnClickPendingIntent(R.id.btn_prev_date, selfPi(context, DateWidget::class.java, widgetId, ACTION_PREV_DATE))
        rv.setOnClickPendingIntent(R.id.btn_next_date, selfPi(context, DateWidget::class.java, widgetId, ACTION_NEXT_DATE))
        rv.setOnClickPendingIntent(R.id.date_title, selfPi(context, DateWidget::class.java, widgetId, ACTION_TODAY))
        rv.setOnClickPendingIntent(R.id.btn_prev_note, selfPi(context, DateWidget::class.java, widgetId, ACTION_PREV_NOTE))
        rv.setOnClickPendingIntent(R.id.btn_next_note, selfPi(context, DateWidget::class.java, widgetId, ACTION_NEXT_NOTE))
        rv.setOnClickPendingIntent(R.id.btn_add, activityPi(context, 500_000 + widgetId, EditorActivity.intent(context, Note.NEW_ID, reminderDay = day)))
        if (note != null) rv.setOnClickPendingIntent(R.id.title, activityPi(context, 600_000 + widgetId, EditorActivity.intent(context, note.id)))
        rv.setRemoteAdapter(R.id.list, listAdapterIntent(context, widgetId, WidgetListService.KIND_DATE))
        rv.setEmptyView(R.id.list, R.id.empty)
        rv.setPendingIntentTemplate(R.id.list, actionTemplate(context, widgetId))
        m.updateAppWidget(widgetId, rv)
    }

    /** Obsah seznamů se musí po změně dat explicitně obnovit. */
    fun notifyListsChanged(context: Context) {
        val m = AppWidgetManager.getInstance(context)
        (noteProviders.keys + listOf(TodayWidget::class.java, DateWidget::class.java)).forEach { cls ->
            m.getAppWidgetIds(ComponentName(context, cls)).forEach { m.notifyAppWidgetViewDataChanged(it, R.id.list) }
        }
    }

    /** O půlnoci přepne widgety Dnes/Datum na nový den. */
    private fun scheduleMidnight(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java)
        val pi = PendingIntent.getBroadcast(
            context, 1, Intent(context, MidnightReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        am.set(AlarmManager.RTC, shiftDay(startOfToday(), 1) + 5_000, pi)
    }

    private val DAY_ACTIONS by lazy { listOf(ACTION_PREV_DATE, ACTION_NEXT_DATE, ACTION_TODAY, ACTION_PREV_NOTE, ACTION_NEXT_NOTE) }

    const val ACTION_PREV_DATE = "cz.teckatecka.poznamky.PREV_DATE"
    const val ACTION_NEXT_DATE = "cz.teckatecka.poznamky.NEXT_DATE"
    const val ACTION_TODAY = "cz.teckatecka.poznamky.TODAY"
    const val ACTION_PREV_NOTE = "cz.teckatecka.poznamky.PREV_NOTE"
    const val ACTION_NEXT_NOTE = "cz.teckatecka.poznamky.NEXT_NOTE"
}

// ---------- Providery ----------

abstract class NoteWidgetBase(private val style: WidgetStyle) : AppWidgetProvider() {
    override fun onUpdate(context: Context, m: AppWidgetManager, ids: IntArray) {
        ids.forEach { WidgetUpdater.renderNote(context, m, it, style) }
    }

    override fun onDeleted(context: Context, ids: IntArray) = ids.forEach { WidgetPrefs.remove(context, it) }
}

class NoteWidgetWhite : NoteWidgetBase(WidgetStyle.WHITE)
class NoteWidgetBlack : NoteWidgetBase(WidgetStyle.BLACK)
class NoteWidgetTransparent : NoteWidgetBase(WidgetStyle.TRANSPARENT)

/** Společné ovládání data pro widgety Dnes a Datum. */
abstract class DayWidgetBase : AppWidgetProvider() {
    abstract fun render(context: Context, m: AppWidgetManager, id: Int)

    override fun onUpdate(context: Context, m: AppWidgetManager, ids: IntArray) = ids.forEach { render(context, m, it) }
    override fun onDeleted(context: Context, ids: IntArray) = ids.forEach { WidgetPrefs.remove(context, it) }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (id == AppWidgetManager.INVALID_APPWIDGET_ID) return super.onReceive(context, intent)
        val day = WidgetPrefs.effectiveDate(context, id)
        fun setDay(d: Long) {
            WidgetPrefs.setDate(context, id, if (d == startOfToday()) 0L else d)
            WidgetPrefs.setIndex(context, id, 0)
        }
        when (intent.action) {
            WidgetUpdater.ACTION_PREV_DATE -> setDay(shiftDay(day, -1))
            WidgetUpdater.ACTION_NEXT_DATE -> setDay(shiftDay(day, 1))
            WidgetUpdater.ACTION_TODAY -> setDay(startOfToday())
            WidgetUpdater.ACTION_PREV_NOTE, WidgetUpdater.ACTION_NEXT_NOTE -> {
                val count = Reminders.notesOn(context, day).size
                if (count > 0) {
                    val delta = if (intent.action == WidgetUpdater.ACTION_NEXT_NOTE) 1 else -1
                    WidgetPrefs.setIndex(context, id, Math.floorMod(WidgetPrefs.index(context, id) + delta, count))
                }
            }
            else -> return super.onReceive(context, intent)
        }
        val m = AppWidgetManager.getInstance(context)
        render(context, m, id)
        m.notifyAppWidgetViewDataChanged(id, R.id.list)
    }
}

class TodayWidget : DayWidgetBase() {
    override fun render(context: Context, m: AppWidgetManager, id: Int) = WidgetUpdater.renderToday(context, m, id)
}

class DateWidget : DayWidgetBase() {
    override fun render(context: Context, m: AppWidgetManager, id: Int) = WidgetUpdater.renderDate(context, m, id)
}

/** Kliknutí na položky v seznamech widgetů: odškrtnutí položky nebo otevření poznámky. */
class WidgetActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getLongExtra(WidgetUpdater.EXTRA_NOTE, Note.NEW_ID)
        if (noteId == Note.NEW_ID) return
        if (intent.hasExtra(WidgetUpdater.EXTRA_INDEX)) {
            val pending = goAsync()
            Thread {
                try {
                    Repo.toggleItem(context, noteId, intent.getIntExtra(WidgetUpdater.EXTRA_INDEX, -1))
                    WidgetUpdater.notifyListsChanged(context)
                } finally {
                    pending.finish()
                }
            }.start()
        } else {
            context.startActivity(
                EditorActivity.intent(context, noteId).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
            )
        }
    }
}

class MidnightReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        WidgetUpdater.updateAll(context)
        WidgetUpdater.notifyListsChanged(context)
    }
}
