package cz.teckatecka.poznamky.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import cz.teckatecka.poznamky.R
import cz.teckatecka.poznamky.data.Note
import cz.teckatecka.poznamky.data.NoteItem
import cz.teckatecka.poznamky.data.NotesDb
import cz.teckatecka.poznamky.data.Settings
import cz.teckatecka.poznamky.data.contrastTextColor
import cz.teckatecka.poznamky.reminder.Reminders
import java.util.Date

/** Obsah seznamů ve widgetech: položky jedné poznámky, nebo poznámky vybraného dne. */
class WidgetListService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = Factory(
        applicationContext,
        intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0),
        intent.getStringExtra(EXTRA_KIND) ?: KIND_NOTE,
    )

    companion object {
        const val EXTRA_KIND = "kind"
        const val KIND_NOTE = "note"
        const val KIND_DATE = "date"
        const val KIND_DAY = "day"
    }

    private class Factory(val context: Context, val widgetId: Int, val kind: String) : RemoteViewsFactory {
        private var note: Note? = null
        private var items: List<Pair<Int, NoteItem>> = emptyList()
        private var dayNotes: List<Note> = emptyList()
        private var textColor = 0

        override fun onCreate() {}
        override fun onDestroy() {}
        override fun getLoadingView(): RemoteViews? = null
        override fun getViewTypeCount() = 3
        override fun getItemId(position: Int) = position.toLong()
        override fun hasStableIds() = false

        override fun onDataSetChanged() {
            when (kind) {
                KIND_DAY -> {
                    dayNotes = Reminders.notesOn(context, WidgetPrefs.effectiveDate(context, widgetId))
                }
                else -> {
                    val n = if (kind == KIND_DATE) {
                        WidgetUpdater.dateWidgetNote(context, widgetId).first
                    } else {
                        NotesDb.get(context).note(WidgetPrefs.noteId(context, widgetId))?.takeIf { !it.deleted }
                    }
                    note = n
                    val style = if (kind == KIND_DATE) WidgetStyle.TRANSPARENT else WidgetUpdater.styleOf(context, widgetId)
                    textColor = style.colorsFor(n).third
                    val indexed = n?.items?.mapIndexed { i, it -> i to it } ?: emptyList()
                    // Hotové položky dolů, pokud je to v nastavení zapnuté (jako v původní appce).
                    items = if (Settings(context).doneItemsBottom) indexed.sortedBy { it.second.done } else indexed
                }
            }
        }

        override fun getCount(): Int = when {
            kind == KIND_DAY -> dayNotes.size
            note == null -> 0
            note!!.isList -> items.size
            else -> if (note!!.body.isBlank()) 0 else 1
        }

        override fun getViewAt(position: Int): RemoteViews {
            if (kind == KIND_DAY) return dayNoteView(position)
            val n = note ?: return RemoteViews(context.packageName, R.layout.widget_item_text)
            val size = n.effectiveFontSize.toFloat()
            return if (n.isList) {
                val (index, item) = items.getOrNull(position) ?: return RemoteViews(context.packageName, R.layout.widget_item_check)
                RemoteViews(context.packageName, R.layout.widget_item_check).apply {
                    setTextViewText(R.id.text, item.title)
                    setTextViewTextSize(R.id.text, TypedValue.COMPLEX_UNIT_SP, size)
                    setTextColor(R.id.text, textColor)
                    setInt(R.id.text, "setPaintFlags",
                        if (item.done) Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG else Paint.ANTI_ALIAS_FLAG)
                    setImageViewResource(R.id.check, if (item.done) R.drawable.ic_check_on else R.drawable.ic_check_off)
                    setInt(R.id.check, "setColorFilter", textColor)
                    val fill = Intent().putExtra(WidgetUpdater.EXTRA_NOTE, n.id).putExtra(WidgetUpdater.EXTRA_INDEX, index)
                    setOnClickFillInIntent(R.id.item, fill)
                }
            } else {
                RemoteViews(context.packageName, R.layout.widget_item_text).apply {
                    setTextViewText(R.id.text, n.body)
                    setTextViewTextSize(R.id.text, TypedValue.COMPLEX_UNIT_SP, size)
                    setTextColor(R.id.text, textColor)
                    setOnClickFillInIntent(R.id.text, Intent().putExtra(WidgetUpdater.EXTRA_NOTE, n.id))
                }
            }
        }

        private fun dayNoteView(position: Int): RemoteViews {
            val rv = RemoteViews(context.packageName, R.layout.widget_item_note)
            val n = dayNotes.getOrNull(position) ?: return rv
            val bg = if (n.color != 0) n.color else 0xFFFFFFFF.toInt()
            val txt = if (n.fontColor != 0) n.fontColor else contrastTextColor(bg)
            rv.setInt(R.id.item_bg, "setColorFilter", bg)
            rv.setInt(R.id.item_bg, "setImageAlpha", if (n.color != 0) 235 else 60)
            val fg = if (n.color == 0) 0xFFFFFFFF.toInt() else txt
            rv.setTextViewText(R.id.item_time, DateFormat.getTimeFormat(context).format(Date(n.reminderOneTimeDate)))
            rv.setTextViewText(R.id.item_title, n.title)
            rv.setViewVisibility(R.id.item_title, if (n.title.isBlank()) View.GONE else View.VISIBLE)
            val body = n.plainText()
            rv.setTextViewText(R.id.item_body, body)
            rv.setViewVisibility(R.id.item_body, if (body.isBlank()) View.GONE else View.VISIBLE)
            rv.setTextViewTextSize(R.id.item_body, TypedValue.COMPLEX_UNIT_SP, n.effectiveFontSize.toFloat())
            listOf(R.id.item_time, R.id.item_title, R.id.item_body).forEach { rv.setTextColor(it, fg) }
            rv.setOnClickFillInIntent(R.id.item, Intent().putExtra(WidgetUpdater.EXTRA_NOTE, n.id))
            return rv
        }
    }
}
