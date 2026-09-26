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
import androidx.compose.material.icons.filled.Delete
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
fun FontDialog(note: Note, onDismiss: () -> Unit, onApply: (Note) -> Unit) {
    var size by remember { mutableStateOf(note.effectiveFontSize.toFloat()) }
    var pickColor by remember { mutableStateOf(false) }
    var fontColor by remember { mutableStateOf(note.fontColor) }
    if (pickColor) {
        ColorPickerDialog(fontColor, { pickColor = false }, "Barva textu") { fontColor = it; pickColor = false }
        return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Písmo") },
        text = {
            Column {
                Text("Velikost: ${size.toInt()} sp")
                Slider(value = size, onValueChange = { size = it }, valueRange = 10f..32f, steps = 21)
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
                onApply(note.copy(fontSize = if (s == Note.DEFAULT_FONT_SIZE) 0 else s, fontColor = fontColor))
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Zrušit") } },
    )
}

@Composable
fun TabsDialog(tabs: List<NoteTab>, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var edit by remember { mutableStateOf<NoteTab?>(null) }
    var colorFor by remember { mutableStateOf<NoteTab?>(null) }
    var deleteFor by remember { mutableStateOf<NoteTab?>(null) }

    edit?.let { t ->
        var title by remember(t) { mutableStateOf(t.title) }
        AlertDialog(
            onDismissRequest = { edit = null },
            title = { Text(if (t.id <= 0) "Nová záložka" else "Přejmenovat") },
            text = { OutlinedTextField(title, { title = it }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) Repo.saveTab(context, t.copy(title = title.trim()))
                    edit = null
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { edit = null }) { Text("Zrušit") } },
        )
        return
    }
    colorFor?.let { t ->
        ColorPickerDialog(t.color, { colorFor = null }) { Repo.saveTab(context, t.copy(color = it)); colorFor = null }
        return
    }
    deleteFor?.let { t ->
        ConfirmDialog("Smazat záložku „${t.title}“? Její poznámky se přesunou do koše.", { deleteFor = null }) {
            Repo.deleteTab(context, t.id); deleteFor = null
        }
        return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Záložky") },
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
                        Text(t.title, Modifier.weight(1f).clickable { edit = t }.padding(vertical = 12.dp))
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
        dismissButton = { TextButton(onClick = { edit = NoteTab(0, "") }) { Text("Přidat záložku") } },
    )
}

private fun reminderLabel(t: ReminderType) = when (t) {
    ReminderType.DAY -> "Každý den"
    ReminderType.WEEK -> "Každý týden"
    ReminderType.TWO_WEEKS -> "Každé 2 týdny"
    ReminderType.FOUR_WEEKS -> "Každé 4 týdny"
    ReminderType.MONTH -> "Každý měsíc"
    ReminderType.TWO_MONTHS -> "Každé 2 měsíce"
    ReminderType.QUARTER -> "Čtvrtletně"
    ReminderType.HALF_YEAR -> "Pololetně"
    ReminderType.YEAR -> "Každý rok"
    ReminderType.ONE_TIME -> "Jednorázově"
}

/** Nastavení připomínky / kalendáře – stejné volby jako v původní appce. */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReminderDialog(note: Note, onDismiss: () -> Unit, onApply: (Note) -> Unit) {
    val context = LocalContext.current
    var n by remember { mutableStateOf(if (note.reminderEnabled) note else note.copy(reminderEnabled = true)) }
    var typeMenu by remember { mutableStateOf(false) }
    val cal = Calendar.getInstance().apply { timeInMillis = n.reminderOneTimeDate }
    val step = when (n.reminderType) {
        ReminderType.TWO_MONTHS, ReminderType.QUARTER, ReminderType.HALF_YEAR, ReminderType.YEAR -> true
        else -> false
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Připomínka") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Box {
                    OutlinedButton(onClick = { typeMenu = true }, modifier = Modifier.fillMaxWidth()) { Text(reminderLabel(n.reminderType)) }
                    DropdownMenu(typeMenu, { typeMenu = false }) {
                        ReminderType.ordered.forEach { t ->
                            DropdownMenuItem(text = { Text(reminderLabel(t)) }, onClick = { n = n.copy(reminderType = t); typeMenu = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        DatePickerDialog(context, { _, y, m, d ->
                            val c = Calendar.getInstance().apply { timeInMillis = n.reminderOneTimeDate; set(y, m, d) }
                            n = n.copy(
                                reminderOneTimeDate = c.timeInMillis,
                                reminderWeekDay = c.get(Calendar.DAY_OF_WEEK),
                                reminderMonthDay = d,
                                reminderYearMonth = m + 1,
                            )
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    }, modifier = Modifier.weight(1f)) {
                        Text(android.text.format.DateFormat.getDateFormat(context).format(cal.time))
                    }
                    OutlinedButton(onClick = {
                        TimePickerDialog(context, { _, h, min ->
                            val c = Calendar.getInstance().apply {
                                timeInMillis = n.reminderOneTimeDate
                                set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, min); set(Calendar.SECOND, 0)
                            }
                            n = n.copy(reminderOneTimeDate = c.timeInMillis)
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                            android.text.format.DateFormat.is24HourFormat(context)).show()
                    }) {
                        Text(android.text.format.DateFormat.getTimeFormat(context).format(cal.time))
                    }
                }
                Text(
                    if (n.reminderType == ReminderType.ONE_TIME) "Datum a čas připomínky" else "Datum = začátek opakování, čas = kdy připomenout",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline,
                )
                when (n.reminderType) {
                    ReminderType.WEEK, ReminderType.TWO_WEEKS, ReminderType.FOUR_WEEKS -> {
                        Spacer(Modifier.height(8.dp))
                        val names = DateFormatSymbols.getInstance().shortWeekdays
                        val first = Calendar.getInstance().firstDayOfWeek
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (0 until 7).map { (first - 1 + it) % 7 + 1 }.forEach { dow ->
                                FilterChip(selected = n.reminderWeekDay == dow, onClick = { n = n.copy(reminderWeekDay = dow) },
                                    label = { Text(names[dow]) })
                            }
                        }
                    }
                    ReminderType.DAY, ReminderType.ONE_TIME -> {}
                    else -> {
                        Spacer(Modifier.height(8.dp))
                        Text("Den v měsíci: ${n.reminderMonthDay}")
                        Slider(value = n.reminderMonthDay.toFloat(), onValueChange = { n = n.copy(reminderMonthDay = it.toInt()) },
                            valueRange = 1f..31f, steps = 29)
                        if (step) {
                            val months = DateFormatSymbols.getInstance().months
                            Text("Počínaje měsícem: ${months[(n.reminderYearMonth - 1).coerceIn(0, 11)]}")
                            Slider(value = n.reminderYearMonth.toFloat(), onValueChange = { n = n.copy(reminderYearMonth = it.toInt()) },
                                valueRange = 1f..12f, steps = 10)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Upozornění (notifikace)", Modifier.weight(1f))
                    Switch(n.reminderNotification, { n = n.copy(reminderNotification = it) })
                }
            }
        },
        confirmButton = { TextButton(onClick = { onApply(n.copy(reminderEnabled = true)) }) { Text("Uložit") } },
        dismissButton = {
            Row {
                if (note.reminderEnabled) TextButton(onClick = { onApply(note.copy(reminderEnabled = false, reminderNextDate = 0)) }) {
                    Text("Vypnout")
                }
                TextButton(onClick = onDismiss) { Text("Zrušit") }
            }
        },
    )
}
