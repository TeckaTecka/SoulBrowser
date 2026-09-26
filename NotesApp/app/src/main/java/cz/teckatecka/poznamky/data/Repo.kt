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

/** Nastavení (SharedPreferences) – volby a výchozí hodnoty jako v původní appce. */
class Settings(context: Context) {
    private val p = context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private fun bool(key: String, def: Boolean) = object : kotlin.properties.ReadWriteProperty<Any?, Boolean> {
        override fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>) = p.getBoolean(key, def)
        override fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: Boolean) =
            p.edit().putBoolean(key, value).apply()
    }

    var sortMode: SortMode
        get() = SortMode.entries.getOrElse(p.getInt("sort", 0)) { SortMode.MODIFIED_DESC }
        set(v) = p.edit().putInt("sort", v.ordinal).apply()
    var viewMode: Int // 0 = volný, 1 = dva sloupce, 2 = seznam
        get() = p.getInt("view_mode2", 0)
        set(v) = p.edit().putInt("view_mode2", v).apply()
    var contentMode: Int // 0 = plné zobrazení, 1 = krátký náhled, 2 = pouze název
        get() = p.getInt("content_mode2", 0)
        set(v) = p.edit().putInt("content_mode2", v).apply()
    var currentTab: Long
        get() = p.getLong("current_tab", NoteTab.COMMON_ID)
        set(v) = p.edit().putLong("current_tab", v).apply()
    var theme: Int // 0 = podle systému, 1 = světlé, 2 = tmavé
        get() = p.getInt("theme", 0)
        set(v) = p.edit().putInt("theme", v).apply()
    /** java.util.Calendar: 1 = neděle, 2 = pondělí. */
    var firstDayOfWeek: Int
        get() = p.getInt("first_dow", 2)
        set(v) = p.edit().putInt("first_dow", v).apply()

    var doneItemsBottom by bool("done_bottom", false)
    var colorFullTab by bool("color_full_tab", true)
    var showReminderTime by bool("show_reminder_ts", true)
    var showCreatedTime by bool("show_created_ts", true)
    var showModifiedTime by bool("show_modified_ts", true)
    var reverseAlignment by bool("reverse_alignment", false)
    var showCalendarTab by bool("calendar_tab", true)
    var calendarToday by bool("calendar_today", false)
    var calendarTitleText by bool("calendar_title_text", true)
    var remindersOn by bool("reminders_on", true)
    var reminderVibrate by bool("reminder_vibro", true)
    var runEditorFromWidget by bool("run_editor_from_widget", false)
    var askBeforeDelete by bool("ask_delete", true)
    var backspaceRemovesItem by bool("backspace_remove_item", true)
    var highlightLinks by bool("highlight_links", true)
    var highlightEmails by bool("highlight_emails", true)
    var highlightPhones by bool("highlight_phones", true)
    var calendarSelectedDay: Long
        get() = p.getLong("calendar_day", 0L)
        set(v) = p.edit().putLong("calendar_day", v).apply()

    var defaultColor: Int
        get() = p.getInt("default_color", 0)
        set(v) = p.edit().putInt("default_color", v).apply()
}
