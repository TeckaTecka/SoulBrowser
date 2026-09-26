package cz.teckatecka.poznamky.data

import org.json.JSONArray
import org.json.JSONObject

/** Položka seznamu (checklistu). JSON klíče odpovídají původní aplikaci My Notes. */
data class NoteItem(
    val title: String,
    val done: Boolean = false,
    /** Formátování položky z původní appky – zachováváme beze změny. */
    val textStyles: JSONArray? = null,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("mTitle", title)
        put("mDone", if (done) 1 else 0)
        if (textStyles != null) put("mTextStyles", textStyles)
    }

    companion object {
        fun listFromJson(json: String?): List<NoteItem> {
            if (json.isNullOrBlank()) return emptyList()
            return try {
                val arr = JSONArray(json)
                (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    NoteItem(
                        title = o.optString("mTitle", ""),
                        done = o.optInt("mDone", 0) == 1,
                        textStyles = o.optJSONArray("mTextStyles"),
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun listToJson(items: List<NoteItem>): String =
            JSONArray().apply { items.forEach { put(it.toJson()) } }.toString()
    }
}

object NoteType {
    const val TEXT = 0
    const val LIST = 1
}

/** Periodicita připomínky – hodnoty shodné s původní DB. */
enum class ReminderType(val value: Int) {
    DAY(1), WEEK(2), TWO_WEEKS(5), FOUR_WEEKS(6), MONTH(3), TWO_MONTHS(7),
    QUARTER(8), HALF_YEAR(9), YEAR(10), ONE_TIME(4);

    companion object {
        /** Pořadí jako v nabídce původní appky. */
        val ordered = listOf(DAY, WEEK, TWO_WEEKS, FOUR_WEEKS, MONTH, TWO_MONTHS, QUARTER, HALF_YEAR, YEAR, ONE_TIME)
        fun from(v: Int) = entries.firstOrNull { it.value == v } ?: DAY
    }
}

data class Note(
    val id: Long = NEW_ID,
    val tabId: Long = NoteTab.COMMON_ID,
    val type: Int = NoteType.TEXT,
    val readOnly: Boolean = false,
    val title: String = "",
    val body: String = "",
    val items: List<NoteItem> = emptyList(),
    val attachmentsJson: String? = null,
    val pinned: Boolean = false,
    val spool: Int = 0,
    val deleted: Boolean = false,
    val color: Int = 0,
    val timeStamp: Long = 0,
    val fontSize: Int = 0,
    val fontName: String? = null,
    val fontColor: Int = 0,
    val fontSizeTitle: Int = 0,
    val fontNameTitle: String? = null,
    val fontColorTitle: Int = 0,
    val createdTimeStamp: Long = System.currentTimeMillis(),
    val reminderEnabled: Boolean = false,
    val reminderType: ReminderType = ReminderType.DAY,
    val reminderWeekDay: Int = 2, // java.util.Calendar.MONDAY
    val reminderMonthDay: Int = 1,
    val reminderOneTimeDate: Long = System.currentTimeMillis(),
    val reminderNextDate: Long = 0,
    val selectionStart: Int = 0,
    val scrollY: Int = 0,
    val reverseAlignment: Boolean = false,
    val reminderNotification: Boolean = true,
    val bodyTextStyle: String? = null,
    val titleTextStyle: String? = null,
    val reminderYearMonth: Int = 1,
) {
    val isList get() = type == NoteType.LIST
    val effectiveFontSize get() = if (fontSize > 0) fontSize else DEFAULT_FONT_SIZE
    val effectiveTitleFontSize get() = if (fontSizeTitle > 0) fontSizeTitle else effectiveFontSize + 4

    /** Text pro náhledy / sdílení – u seznamu položky pod sebou. */
    fun plainText(): String =
        if (isList) items.joinToString("\n") { (if (it.done) "☑ " else "☐ ") + it.title } else body

    fun isEmpty() = title.isBlank() && body.isBlank() && items.all { it.title.isBlank() }

    /** Převod text <-> seznam jako v původní appce (každý řádek = položka). */
    fun toggledType(): Note = if (isList) {
        copy(type = NoteType.TEXT, body = items.joinToString("\n") { it.title })
    } else {
        copy(type = NoteType.LIST, items = body.split("\n").filter { it.isNotBlank() }.map { NoteItem(it) })
    }

    companion object {
        const val NEW_ID = -1L
        const val DEFAULT_FONT_SIZE = 14
    }
}

data class NoteTab(
    val id: Long,
    val title: String,
    val spool: Int = 0,
    val deleted: Boolean = false,
    val color: Int = 0,
    val timeStamp: Long = 0,
    val fontColor: Int = 0,
) {
    companion object {
        const val COMMON_ID = 1L
        const val WORK_ID = 2L
        const val HOME_ID = 3L
        /** Virtuální záložka kalendáře (stejně jako v původní appce). */
        const val CALENDAR_ID = -1L
    }
}

/** Paleta barev z původní appky (0 = výchozí/průhledná). */
val NOTE_COLORS = listOf(
    0x00000000L, 0xffffffa5, 0xffe3faa3, 0xffcae7b9, 0xffa0f1c7, 0xfff3de8a, 0xffffc35d,
    0xffffbb92, 0xffeabbf7, 0xff94a6aa, 0xffa6e2ff, 0xffb6d2ef, 0xffc6c2df, 0xffd6b2cf,
    0xffe6a2bf, 0xffeff3f8, 0xffb2c4df, 0xff6689bf, 0xff3d5272, 0xff1e2939, 0xffefc7c2,
    0xffffe5d4, 0xffbfd3c1, 0xff68a691, 0xff694f5d, 0xffff7455, 0xff1ba8b1, 0xfffffeaa,
    0xffff0079, 0xffdcff46,
).map { it.toInt() }

/** Rozumná barva textu na daném pozadí (0 = nechat na tématu). */
fun contrastTextColor(bg: Int): Int {
    if (bg == 0) return 0
    val r = (bg shr 16) and 0xff
    val g = (bg shr 8) and 0xff
    val b = bg and 0xff
    val lum = (0.299 * r + 0.587 * g + 0.114 * b)
    return if (lum > 140) 0xff202124.toInt() else 0xfff1f3f4.toInt()
}
