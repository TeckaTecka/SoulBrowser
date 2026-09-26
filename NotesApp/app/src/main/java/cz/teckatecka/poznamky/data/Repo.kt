package cz.teckatecka.poznamky.data

import android.content.Context
import cz.teckatecka.poznamky.reminder.Reminders
import cz.teckatecka.poznamky.widget.WidgetUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Jediný vstupní bod pro zápisy: po každé změně obnoví widgety, přeplánuje připomínky
 * a zvýší [version], na kterou reaguje UI.
 */
object Repo {
    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version

    fun db(context: Context) = NotesDb.get(context)

    fun saveNote(context: Context, note: Note, touch: Boolean = true): Note {
        val withReminder = if (note.reminderEnabled) note.copy(reminderNextDate = Reminders.nextDate(note)) else note
        val saved = db(context).save(withReminder, touch)
        changed(context)
        return saved
    }

    fun toggleItem(context: Context, noteId: Long, index: Int) {
        val note = db(context).note(noteId) ?: return
        if (index !in note.items.indices) return
        val items = note.items.toMutableList()
        items[index] = items[index].copy(done = !items[index].done)
        db(context).save(note.copy(items = items))
        changed(context)
    }

    fun trash(context: Context, id: Long) { db(context).setDeleted(id, true); changed(context) }
    fun restore(context: Context, id: Long) { db(context).setDeleted(id, false); changed(context) }
    fun deleteForever(context: Context, id: Long) { db(context).deleteForever(id); changed(context) }
    fun emptyTrash(context: Context) { db(context).emptyTrash(); changed(context) }

    fun saveTab(context: Context, tab: NoteTab): NoteTab = db(context).saveTab(tab).also { changed(context) }
    fun deleteTab(context: Context, id: Long) { db(context).deleteTab(id); changed(context) }
    fun moveTab(context: Context, id: Long, delta: Int) { db(context).moveTab(id, delta); changed(context) }

    fun changed(context: Context) {
        _version.value++
        WidgetUpdater.updateAll(context)
        Reminders.scheduleAll(context)
    }
}

/** Nastavení (SharedPreferences). */
class Settings(context: Context) {
    private val p = context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var sortMode: SortMode
        get() = SortMode.entries.getOrElse(p.getInt("sort", 0)) { SortMode.MODIFIED_DESC }
        set(v) = p.edit().putInt("sort", v.ordinal).apply()
    var viewMode: Int // 0 = volný, 1 = dva sloupce, 2 = seznam
        get() = p.getInt("view_mode2", 0)
        set(v) = p.edit().putInt("view_mode2", v).apply()
    var contentMode: Int // 0 = celý obsah, 1 = krátký náhled, 2 = jen název
        get() = p.getInt("content_mode", 1)
        set(v) = p.edit().putInt("content_mode", v).apply()
    var currentTab: Long
        get() = p.getLong("current_tab", NoteTab.COMMON_ID)
        set(v) = p.edit().putLong("current_tab", v).apply()
    var theme: Int // 0 = podle systému, 1 = světlé, 2 = tmavé
        get() = p.getInt("theme", 0)
        set(v) = p.edit().putInt("theme", v).apply()
    var doneItemsBottom: Boolean
        get() = p.getBoolean("done_bottom", false)
        set(v) = p.edit().putBoolean("done_bottom", v).apply()
    var askBeforeDelete: Boolean
        get() = p.getBoolean("ask_delete", true)
        set(v) = p.edit().putBoolean("ask_delete", v).apply()
    var showCalendarTab: Boolean
        get() = p.getBoolean("calendar_tab", true)
        set(v) = p.edit().putBoolean("calendar_tab", v).apply()
    var widgetShowCreated: Boolean
        get() = p.getBoolean("w_created", false)
        set(v) = p.edit().putBoolean("w_created", v).apply()
    var widgetShowModified: Boolean
        get() = p.getBoolean("w_modified", false)
        set(v) = p.edit().putBoolean("w_modified", v).apply()
    var widgetShowReminder: Boolean
        get() = p.getBoolean("w_reminder", true)
        set(v) = p.edit().putBoolean("w_reminder", v).apply()
    var defaultColor: Int
        get() = p.getInt("default_color", 0)
        set(v) = p.edit().putInt("default_color", v).apply()
}
