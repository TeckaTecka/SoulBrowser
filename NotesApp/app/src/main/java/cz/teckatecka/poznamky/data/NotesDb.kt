package cz.teckatecka.poznamky.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * SQLite databáze se stejným schématem jako původní My Notes (mynotes.db, verze 16).
 * Díky tomu jde záloha .bak přenášet oběma směry.
 */
class NotesDb private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, NAME, null, VERSION) {

    init {
        // Jeden soubor bez -wal, aby šla DB přímo kopírovat jako záloha.
        setWriteAheadLoggingEnabled(false)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABS)
        db.execSQL(CREATE_NOTES)
        db.execSQL(CREATE_USERS)
        val now = System.currentTimeMillis()
        listOf(NoteTab.COMMON_ID to "Obecné", NoteTab.WORK_ID to "Práce", NoteTab.HOME_ID to "Domov")
            .forEachIndexed { i, (id, title) ->
                db.insert(TABS, null, tabValues(NoteTab(id, title, spool = i, timeStamp = now)))
            }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        ensureColumns(db)
    }

    // ---------- Notes ----------

    fun notesInTab(tabId: Long, sort: SortMode): List<Note> =
        queryNotes("tab_id=? AND deleted=0", arrayOf(tabId.toString()), sort.orderBy)

    fun allActiveNotes(sort: SortMode = SortMode.MODIFIED_DESC): List<Note> =
        queryNotes("deleted=0", null, sort.orderBy)

    fun deletedNotes(): List<Note> = queryNotes("deleted=1", null, "time_stamp desc")

    fun activeCalendarNotes(): List<Note> = queryNotes("reminder_enabled=1 AND deleted=0", null, null)

    fun note(id: Long): Note? = queryNotes("id=?", arrayOf(id.toString()), null).firstOrNull()

    /** Uloží poznámku; nové dostane ID max+1. Vrací uloženou verzi. */
    fun save(note: Note, touch: Boolean = true): Note {
        val db = writableDatabase
        val toSave = note.copy(
            id = if (note.id == Note.NEW_ID) nextId(db, NOTES) else note.id,
            timeStamp = if (touch || note.timeStamp == 0L) System.currentTimeMillis() else note.timeStamp,
        )
        db.insertWithOnConflict(NOTES, null, noteValues(toSave), SQLiteDatabase.CONFLICT_REPLACE)
        return toSave
    }

    fun setDeleted(id: Long, deleted: Boolean) {
        writableDatabase.update(NOTES, ContentValues().apply {
            put("deleted", if (deleted) 1 else 0)
            put("time_stamp", System.currentTimeMillis())
        }, "id=?", arrayOf(id.toString()))
    }

    fun deleteForever(id: Long) {
        writableDatabase.delete(NOTES, "id=?", arrayOf(id.toString()))
    }

    fun emptyTrash() {
        writableDatabase.delete(NOTES, "deleted=1", null)
    }

    // ---------- Tabs ----------

    fun tabs(): List<NoteTab> {
        readableDatabase.query(TABS, null, "deleted=0", null, null, null, "spool, id").use { c ->
            val out = ArrayList<NoteTab>()
            while (c.moveToNext()) out += c.toTab()
            return out
        }
    }

    fun saveTab(tab: NoteTab): NoteTab {
        val db = writableDatabase
        val t = if (tab.id <= 0) tab.copy(id = nextId(db, TABS), spool = tabs().size) else tab
        db.insertWithOnConflict(TABS, null, tabValues(t.copy(timeStamp = System.currentTimeMillis())), SQLiteDatabase.CONFLICT_REPLACE)
        return t
    }

    /** Smaže záložku a její poznámky přesune do koše (jako původní appka). */
    fun deleteTab(id: Long) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.update(TABS, ContentValues().apply { put("deleted", 1) }, "id=?", arrayOf(id.toString()))
            db.update(NOTES, ContentValues().apply { put("deleted", 1) }, "tab_id=?", arrayOf(id.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun moveTab(id: Long, delta: Int) {
        val list = tabs().toMutableList()
        val i = list.indexOfFirst { it.id == id }
        val j = i + delta
        if (i < 0 || j !in list.indices) return
        list.add(j, list.removeAt(i))
        list.forEachIndexed { idx, t -> saveTab(t.copy(spool = idx)) }
    }

    // ---------- Import ze zálohy ----------

    /**
     * Načte zálohu (.bak = SQLite soubor z původní appky nebo z této) a nahradí jí aktuální data.
     * Chybějící sloupce (starší verze zálohy) doplní výchozími hodnotami.
     */
    fun replaceWithBackup(backupFile: java.io.File) {
        val src = SQLiteDatabase.openDatabase(backupFile.path, null, SQLiteDatabase.OPEN_READWRITE)
        src.use { s ->
            val db = writableDatabase
            db.beginTransaction()
            try {
                db.delete(NOTES, null, null)
                db.delete(TABS, null, null)
                copyTable(s, db, TABS)
                copyTable(s, db, NOTES)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    private fun copyTable(src: SQLiteDatabase, dst: SQLiteDatabase, table: String) {
        val dstCols = columns(dst, table)
        src.rawQuery("SELECT * FROM $table", null).use { c ->
            while (c.moveToNext()) {
                val cv = ContentValues()
                for (i in 0 until c.columnCount) {
                    val name = c.getColumnName(i)
                    if (name !in dstCols) continue
                    when (c.getType(i)) {
                        Cursor.FIELD_TYPE_NULL -> cv.putNull(name)
                        Cursor.FIELD_TYPE_INTEGER -> cv.put(name, c.getLong(i))
                        Cursor.FIELD_TYPE_FLOAT -> cv.put(name, c.getDouble(i))
                        Cursor.FIELD_TYPE_BLOB -> cv.put(name, c.getBlob(i))
                        else -> cv.put(name, c.getString(i))
                    }
                }
                dst.insertWithOnConflict(table, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            }
        }
    }

    // ---------- interní ----------

    private fun queryNotes(where: String?, args: Array<String>?, order: String?): List<Note> {
        readableDatabase.query(NOTES, null, where, args, null, null, order).use { c ->
            val out = ArrayList<Note>(c.count)
            while (c.moveToNext()) out += c.toNote()
            return out
        }
    }

    private fun nextId(db: SQLiteDatabase, table: String): Long =
        db.rawQuery("SELECT IFNULL(MAX(id),0)+1 FROM $table", null).use { it.moveToFirst(); it.getLong(0) }

    private fun columns(db: SQLiteDatabase, table: String): Set<String> =
        db.rawQuery("PRAGMA table_info($table)", null).use { c ->
            buildSet { while (c.moveToNext()) add(c.getString(1)) }
        }

    /** Doplní sloupce, které mohou chybět (např. DB ze starší verze). */
    private fun ensureColumns(db: SQLiteDatabase) {
        val have = columns(db, NOTES)
        NOTE_EXTRA_COLUMNS.forEach { (name, def) ->
            if (name !in have) db.execSQL("ALTER TABLE $NOTES ADD COLUMN $name $def")
        }
        val tabCols = columns(db, TABS)
        if ("color" !in tabCols) db.execSQL("ALTER TABLE $TABS ADD COLUMN color INTEGER DEFAULT 0")
        if ("font_color" !in tabCols) db.execSQL("ALTER TABLE $TABS ADD COLUMN font_color INTEGER DEFAULT 0")
    }

    private fun Cursor.str(n: String): String? = getColumnIndex(n).let { if (it < 0 || isNull(it)) null else getString(it) }
    private fun Cursor.int(n: String, d: Int = 0): Int = getColumnIndex(n).let { if (it < 0 || isNull(it)) d else getInt(it) }
    private fun Cursor.long(n: String, d: Long = 0): Long = getColumnIndex(n).let { if (it < 0 || isNull(it)) d else getLong(it) }

    private fun Cursor.toNote() = Note(
        id = long("id"),
        tabId = long("tab_id", NoteTab.COMMON_ID),
        type = int("note_type"),
        readOnly = int("note_edit_type") == 1,
        title = str("title") ?: "",
        body = str("body") ?: "",
        items = NoteItem.listFromJson(str("items")),
        attachmentsJson = str("attachments"),
        pinned = int("pin", 1) == 0,
        spool = int("spool"),
        deleted = int("deleted") == 1,
        color = int("color"),
        timeStamp = long("time_stamp"),
        fontSize = int("font_size"),
        fontName = str("font_name"),
        fontColor = int("font_color"),
        fontSizeTitle = int("font_size_title"),
        fontNameTitle = str("font_name_title"),
        fontColorTitle = int("font_color_title"),
        createdTimeStamp = long("created_time_stamp"),
        reminderEnabled = int("reminder_enabled") == 1,
        reminderType = ReminderType.from(int("reminder_type", 1)),
        reminderWeekDay = int("reminder_week_day", 2),
        reminderMonthDay = int("reminder_month_day", 1),
        reminderOneTimeDate = long("reminder_one_time_date"),
        reminderNextDate = long("reminder_next_date_time"),
        selectionStart = int("selection_start"),
        scrollY = int("scroll_y"),
        reverseAlignment = int("reverse_alignment") == 1,
        reminderNotification = int("reminder_notification", 1) == 1,
        bodyTextStyle = str("body_text_style"),
        titleTextStyle = str("title_text_style"),
        reminderYearMonth = int("reminder_year_month", 1),
    )

    private fun noteValues(n: Note) = ContentValues().apply {
        put("id", n.id)
        put("tab_id", n.tabId)
        put("note_type", n.type)
        put("note_edit_type", if (n.readOnly) 1 else 0)
        put("title", n.title)
        put("body", n.body)
        put("items", NoteItem.listToJson(n.items))
        put("attachments", n.attachmentsJson ?: "[]")
        put("pin", if (n.pinned) 0 else 1)
        put("spool", n.spool)
        put("deleted", if (n.deleted) 1 else 0)
        put("color", n.color)
        put("time_stamp", n.timeStamp)
        put("font_size", n.fontSize)
        put("font_name", n.fontName)
        put("font_color", n.fontColor)
        put("font_size_title", n.fontSizeTitle)
        put("font_name_title", n.fontNameTitle)
        put("font_color_title", n.fontColorTitle)
        put("created_time_stamp", n.createdTimeStamp)
        put("reminder_enabled", if (n.reminderEnabled) 1 else 0)
        put("reminder_type", n.reminderType.value)
        put("reminder_week_day", n.reminderWeekDay)
        put("reminder_month_day", n.reminderMonthDay)
        put("reminder_one_time_date", n.reminderOneTimeDate)
        put("reminder_next_date_time", n.reminderNextDate)
        put("selection_start", n.selectionStart)
        put("scroll_y", n.scrollY)
        put("reverse_alignment", if (n.reverseAlignment) 1 else 0)
        put("reminder_notification", if (n.reminderNotification) 1 else 0)
        put("body_text_style", n.bodyTextStyle ?: "[]")
        put("title_text_style", n.titleTextStyle ?: "[]")
        put("reminder_year_month", n.reminderYearMonth)
    }

    private fun Cursor.toTab() = NoteTab(
        id = long("id"),
        title = str("title") ?: "",
        spool = int("spool"),
        deleted = int("deleted") == 1,
        color = int("color"),
        timeStamp = long("time_stamp"),
        fontColor = int("font_color"),
    )

    private fun tabValues(t: NoteTab) = ContentValues().apply {
        put("id", t.id)
        put("title", t.title)
        put("spool", t.spool)
        put("deleted", if (t.deleted) 1 else 0)
        put("color", t.color)
        put("time_stamp", t.timeStamp)
        put("font_color", t.fontColor)
    }

    companion object {
        const val NAME = "mynotes.db"
        const val VERSION = 16
        private const val NOTES = "Notes"
        private const val TABS = "NoteTabs"

        private const val CREATE_TABS =
            "create table NoteTabs(id INTEGER, title, spool INTEGER, deleted INTEGER DEFAULT 0, color INTEGER DEFAULT 0, time_stamp, font_color INTEGER DEFAULT 0, PRIMARY KEY (id))"
        private const val CREATE_NOTES =
            "create table Notes(id INTEGER, tab_id INTEGER, note_type INTEGER DEFAULT 0, note_edit_type INTEGER DEFAULT 0, title, body, items, attachments, pin INTEGER DEFAULT 1, spool INTEGER, deleted INTEGER DEFAULT 0, color INTEGER DEFAULT 0, time_stamp, font_size INTEGER DEFAULT 0, font_name, font_color INTEGER DEFAULT 0, font_size_title INTEGER DEFAULT 0, font_name_title, font_color_title INTEGER DEFAULT 0, created_time_stamp, reminder_enabled INTEGER DEFAULT 0, reminder_type INTEGER DEFAULT 1, reminder_week_day INTEGER DEFAULT 2, reminder_month_day INTEGER DEFAULT 1, reminder_one_time_date, reminder_next_date_time, selection_start INTEGER DEFAULT 0, scroll_y INTEGER DEFAULT 0, reverse_alignment INTEGER DEFAULT 0, reminder_notification INTEGER DEFAULT 1, body_text_style, title_text_style, reminder_year_month INTEGER DEFAULT 1, PRIMARY KEY (id))"
        private const val CREATE_USERS =
            "create table Users(id INTEGER, name, pin, pin_attempts INTEGER, secret_question_id INTEGER, secret_answer, secret_attempts INTEGER, secret_last_failed_time_stamp, last_success_login_time_stamp, PRIMARY KEY (id))"

        private val NOTE_EXTRA_COLUMNS = listOf(
            "note_edit_type" to "INTEGER DEFAULT 0", "attachments" to "", "font_size" to "INTEGER DEFAULT 0",
            "font_name" to "", "font_color" to "INTEGER DEFAULT 0", "font_size_title" to "INTEGER DEFAULT 0",
            "font_name_title" to "", "font_color_title" to "INTEGER DEFAULT 0", "created_time_stamp" to "",
            "reminder_enabled" to "INTEGER DEFAULT 0", "reminder_type" to "INTEGER DEFAULT 1",
            "reminder_week_day" to "INTEGER DEFAULT 2", "reminder_month_day" to "INTEGER DEFAULT 1",
            "reminder_one_time_date" to "", "reminder_next_date_time" to "", "selection_start" to "INTEGER DEFAULT 0",
            "scroll_y" to "INTEGER DEFAULT 0", "reverse_alignment" to "INTEGER DEFAULT 0",
            "reminder_notification" to "INTEGER DEFAULT 1", "body_text_style" to "", "title_text_style" to "",
            "reminder_year_month" to "INTEGER DEFAULT 1",
        )

        @Volatile private var instance: NotesDb? = null
        fun get(context: Context): NotesDb =
            instance ?: synchronized(this) { instance ?: NotesDb(context).also { instance = it } }
    }
}

/** Řazení – stejné varianty jako v původní appce; připnuté vždy nahoře. */
enum class SortMode(val orderBy: String, val label: String) {
    MODIFIED_DESC("pin, time_stamp desc", "Od nejnovějších"),
    MODIFIED("pin, time_stamp", "Od nejstarších"),
    TITLE("pin, title COLLATE LOCALIZED", "Podle názvu A–Z"),
    TITLE_DESC("pin, title COLLATE LOCALIZED desc", "Podle názvu Z–A"),
}
