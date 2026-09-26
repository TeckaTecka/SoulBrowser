package cz.teckatecka.poznamky.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.teckatecka.poznamky.data.NOTE_COLORS
import cz.teckatecka.poznamky.data.Note
import cz.teckatecka.poznamky.data.NoteTab
import cz.teckatecka.poznamky.data.ReminderType
import cz.teckatecka.poznamky.data.Repo
import java.text.DateFormatSymbols
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(current: Int, onDismiss: () -> Unit, title: String = "Barva", onPick: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NOTE_COLORS.forEach { c ->
                    Box(
                        Modifier.size(40.dp).clip(CircleShape)
                            .background(if (c == 0) MaterialTheme.colorScheme.surfaceVariant else Color(c))
                            .border(if (c == current) 3.dp else 1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable { onPick(c) },
                        contentAlignment = Alignment.Center,
                    ) { if (c == 0) Text("–") }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Zrušit") } },
    )
}

/** Barva textu – stejná paleta + tmavé/bílé. */
@Composable
fun FontDialog(note: Note, forTitle: Boolean = false, onDismiss: () -> Unit, onApply: (Note) -> Unit) {
    var size by remember { mutableStateOf((if (forTitle) note.effectiveTitleFontSize else note.effectiveFontSize).toFloat()) }
    var pickColor by remember { mutableStateOf(false) }
    var fontColor by remember { mutableStateOf(if (forTitle) note.fontColorTitle else note.fontColor) }
    if (pickColor) {
        ColorPickerDialog(fontColor, { pickColor = false }, "Barva textu") { fontColor = it; pickColor = false }
        return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (forTitle) "Písmo hlavy" else "Písmo") },
        text = {
            Column {
                Text("Velikost: ${size.toInt()} sp")
                Slider(value = size, onValueChange = { size = it }, valueRange = 8f..40f, steps = 31)
                Text("Ukázka textu", fontSize = size.sp)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Barva textu", Modifier.weight(1f))
                    Box(
                        Modifier.size(32.dp).clip(CircleShape)
                            .background(if (fontColor == 0) MaterialTheme.colorScheme.onSurface else Color(fontColor))
                            .clickable { pickColor = true },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val s = size.toInt()
                onApply(
                    if (forTitle) note.copy(fontSizeTitle = if (s == note.effectiveFontSize + 4) 0 else s, fontColorTitle = fontColor)
                    else note.copy(fontSize = if (s == Note.DEFAULT_FONT_SIZE) 0 else s, fontColor = fontColor),
                )
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Zrušit") } },
    )
}

@Composable
fun TabsDialog(tabs: List<NoteTab>, startWithNew: Boolean = false, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var edit by remember { mutableStateOf<NoteTab?>(if (startWithNew) NoteTab(0, "") else null) }
    var colorFor by remember { mutableStateOf<NoteTab?>(null) }
    var fontColorFor by remember { mutableStateOf<NoteTab?>(null) }
    var deleteFor by remember { mutableStateOf<NoteTab?>(null) }

    edit?.let { t ->
        var title by remember(t) { mutableStateOf(t.title) }
        AlertDialog(
            onDismissRequest = { edit = null },
            title = { Text(if (t.id <= 0) "Nová karta" else "Upravit kartu") },
            text = { OutlinedTextField(title, { title = it }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) Repo.saveTab(context, t.copy(title = title.trim()))
                    edit = null
                    if (startWithNew) onDismiss()
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { edit = null; if (startWithNew) onDismiss() }) { Text("Zrušit") } },
        )
        return
    }
    colorFor?.let { t ->
        ColorPickerDialog(t.color, { colorFor = null }, "Barva pozadí karty") { Repo.saveTab(context, t.copy(color = it)); colorFor = null }
        return
    }
    fontColorFor?.let { t ->
        ColorPickerDialog(t.fontColor, { fontColorFor = null }, "Barva písma karty") { Repo.saveTab(context, t.copy(fontColor = it)); fontColorFor = null }
        return
    }
    deleteFor?.let { t ->
        ConfirmDialog("Odstranit kartu „${t.title}“? Její poznámky se přesunou do koše.", { deleteFor = null }) {
            Repo.deleteTab(context, t.id); deleteFor = null
        }
        return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Správa karet") },
        text = {
            LazyColumn {
                itemsIndexed(tabs, key = { _, t -> t.id }) { i, t ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(24.dp).clip(CircleShape)
                                .background(if (t.color == 0) MaterialTheme.colorScheme.surfaceVariant else Color(t.color))
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable { colorFor = t },
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(t.title, Modifier.weight(1f).clickable { edit = t }.padding(vertical = 12.dp),
                            color = if (t.fontColor != 0) Color(t.fontColor) else MaterialTheme.colorScheme.onSurface)
                        IconButton(onClick = { fontColorFor = t }) { Icon(Icons.Default.FormatColorText, "Barva písma") }
                        IconButton(onClick = { Repo.moveTab(context, t.id, -1) }, enabled = i > 0) {
                            Icon(Icons.Default.KeyboardArrowUp, "Nahoru")
                        }
                        IconButton(onClick = { Repo.moveTab(context, t.id, 1) }, enabled = i < tabs.size - 1) {
                            Icon(Icons.Default.KeyboardArrowDown, "Dolů")
                        }
                        IconButton(onClick = { deleteFor = t }, enabled = tabs.size > 1) { Icon(Icons.Default.Delete, "Smazat") }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Hotovo") } },
        dismissButton = { TextButton(onClick = { edit = NoteTab(0, "") }) { Text("Přidat kartu") } },
    )
}

/** Názvy periodicity přesně jako v původní appce. */
fun reminderLabel(t: ReminderType) = when (t) {
    ReminderType.DAY -> "Denně"
    ReminderType.WEEK -> "Týdně"
    ReminderType.TWO_WEEKS -> "Každé 2 týdny"
    ReminderType.FOUR_WEEKS -> "Každé 4 týdny"
    ReminderType.MONTH -> "Měsíční"
    ReminderType.TWO_MONTHS -> "Každé 2 měsíce"
    ReminderType.QUARTER -> "Čtvrtletní"
    ReminderType.HALF_YEAR -> "Každého půl roku"
    ReminderType.YEAR -> "Každoročně"
    ReminderType.ONE_TIME -> "Jeden čas"
}

@Composable
private fun PickList(title: String, options: List<String>, selected: Int, onDismiss: () -> Unit, onPick: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn {
                itemsIndexed(options) { i, o ->
                    Row(Modifier.fillMaxWidth().clickable { onPick(i) }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.RadioButton(selected = i == selected, onClick = { onPick(i) })
                        Text(o)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("zrušení") } },
    )
}

/** „Nastavení kalendáře“ – celoobrazovkové okno podle fragment_reminder_change.xml. */
@Composable
fun ReminderDialog(note: Note, onDismiss: () -> Unit, onApply: (Note) -> Unit) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(true) }
    var n by remember { mutableStateOf(note) }
    var pick by remember { mutableStateOf(0) } // 1 periodicita, 2 den v týdnu, 3 měsíc roku, 4 den v měsíci
    val cal = Calendar.getInstance().apply { timeInMillis = n.reminderOneTimeDate }
    val weekTypes = setOf(ReminderType.WEEK, ReminderType.TWO_WEEKS, ReminderType.FOUR_WEEKS)
    val yearMonthTypes = setOf(ReminderType.TWO_MONTHS, ReminderType.QUARTER, ReminderType.HALF_YEAR, ReminderType.YEAR)
    val monthTypes = yearMonthTypes + ReminderType.MONTH
    val dayNames = DateFormatSymbols.getInstance().weekdays
    val months = DateFormatSymbols.getInstance().months
    val firstDow = cz.teckatecka.poznamky.data.Settings(context).firstDayOfWeek
    val weekOrder = (0 until 7).map { (firstDow - 1 + it) % 7 + 1 }
    fun applyAndClose() = onApply(if (enabled) n.copy(reminderEnabled = true) else note.copy(reminderEnabled = false, reminderNextDate = 0))

    when (pick) {
        1 -> PickList("Periodicita", ReminderType.ordered.map(::reminderLabel), ReminderType.ordered.indexOf(n.reminderType), { pick = 0 }) {
            n = n.copy(reminderType = ReminderType.ordered[it]); pick = 0
        }
        2 -> PickList("Den v týdnu", weekOrder.map { dayNames[it] }, weekOrder.indexOf(n.reminderWeekDay), { pick = 0 }) {
            n = n.copy(reminderWeekDay = weekOrder[it]); pick = 0
        }
        3 -> PickList("Měsíc roku", (0 until 12).map { months[it] }, n.reminderYearMonth - 1, { pick = 0 }) {
            n = n.copy(reminderYearMonth = it + 1); pick = 0
        }
        4 -> PickList("Den v měsíci", (1..31).map { "$it" }, n.reminderMonthDay - 1, { pick = 0 }) {
            n = n.copy(reminderMonthDay = it + 1); pick = 0
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        androidx.compose.material3.Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column {
                Row(
                    Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(50.dp).padding(10.dp))
                    Text("Nastavení kalendáře", Modifier.weight(1f).padding(start = 8.dp), fontSize = 20.sp)
                    IconButton(onClick = ::applyAndClose, Modifier.size(44.dp)) { Icon(Icons.Default.Check, "OK", tint = Color(0xFF43A047)) }
                }
                Column(Modifier.verticalScroll(rememberScrollState()).padding(4.dp)) {
                    Column(Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant).padding(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { enabled = !enabled }) {
                            Checkbox(enabled, { enabled = it })
                            Text("Přidat do kalendáře")
                        }
                        if (enabled) {
                            @Composable
                            fun Field(label: String, value: String, onClick: () -> Unit) {
                                Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(start = 4.dp, top = 8.dp, bottom = 4.dp)) {
                                    Text(label, color = MaterialTheme.colorScheme.outline)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(value, Modifier.weight(1f), fontSize = 18.sp)
                                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                            Field("Periodicita", reminderLabel(n.reminderType)) { pick = 1 }
                            if (n.reminderType in weekTypes) Field("Den v týdnu", dayNames[n.reminderWeekDay]) { pick = 2 }
                            if (n.reminderType in yearMonthTypes) Field("Měsíc roku", months[(n.reminderYearMonth - 1).coerceIn(0, 11)]) { pick = 3 }
                            if (n.reminderType in monthTypes) Field("Den v měsíci", "${n.reminderMonthDay}") { pick = 4 }
                            if (n.reminderType == ReminderType.ONE_TIME) {
                                Text("Datum (v budoucnu)", color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
                                Row(
                                    Modifier.padding(4.dp).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                        .clickable {
                                            DatePickerDialog(context, { _, y, m, d ->
                                                val c = Calendar.getInstance().apply { timeInMillis = n.reminderOneTimeDate; set(y, m, d) }
                                                n = n.copy(reminderOneTimeDate = c.timeInMillis, reminderWeekDay = c.get(Calendar.DAY_OF_WEEK),
                                                    reminderMonthDay = d, reminderYearMonth = m + 1)
                                            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                                        }.padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(22.dp))
                                    Text(android.text.format.DateFormat.getDateFormat(context).format(cal.time), fontSize = 18.sp,
                                        modifier = Modifier.padding(start = 6.dp))
                                }
                            }
                            Text("Čas:", color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
                            Text(
                                android.text.format.DateFormat.getTimeFormat(context).format(cal.time), fontSize = 18.sp,
                                modifier = Modifier.padding(4.dp).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                    .clickable {
                                        TimePickerDialog(context, { _, h, min ->
                                            val c = Calendar.getInstance().apply {
                                                timeInMillis = n.reminderOneTimeDate
                                                set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, min); set(Calendar.SECOND, 0)
                                            }
                                            n = n.copy(reminderOneTimeDate = c.timeInMillis)
                                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                                            android.text.format.DateFormat.is24HourFormat(context)).show()
                                    }.padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                            Text("Datum / čas příštího kalendáře", color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
                            val next = cz.teckatecka.poznamky.reminder.Reminders.nextDate(n.copy(reminderEnabled = true))
                            Text(if (next > 0) formatDateTime(context, next) else "–", modifier = Modifier.padding(start = 4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                                Switch(n.reminderNotification, { n = n.copy(reminderNotification = it) })
                                Text("Připomínka", Modifier.padding(start = 8.dp).weight(1f))
                                Row(
                                    Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                        .clickable { cz.teckatecka.poznamky.reminder.Reminders.notifyNow(context, n) }.padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Default.Alarm, null)
                                    Text("Upozornit hned", fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
                                }
                            }
                        }
                    }
                    Row(
                        Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp)).clickable(onClick = ::applyAndClose)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color(0xFF43A047))
                        Text("OK", fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}
