package cz.teckatecka.poznamky.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import cz.teckatecka.poznamky.data.Note
import cz.teckatecka.poznamky.data.NoteItem
import cz.teckatecka.poznamky.data.NoteTab
import cz.teckatecka.poznamky.data.NoteType
import cz.teckatecka.poznamky.data.ReminderType
import cz.teckatecka.poznamky.data.Repo
import cz.teckatecka.poznamky.data.Settings
import cz.teckatecka.poznamky.widget.WidgetPrefs
import cz.teckatecka.poznamky.widget.WidgetUpdater
import org.json.JSONArray
import java.util.Calendar

class EditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settings = Settings(this)
        val db = Repo.db(this)
        val noteId = intent.getLongExtra(EXTRA_NOTE, Note.NEW_ID)
        val widgetId = intent.getIntExtra(EXTRA_WIDGET, AppWidgetManager.INVALID_APPWIDGET_ID)
        val existing = if (noteId != Note.NEW_ID) db.note(noteId) else null

        val pages: List<Note>
        val start: Int
        if (existing != null) {
            // Listování mezi poznámkami stejné záložky (jako v původní appce).
            val inTab = if (existing.deleted) emptyList() else db.notesInTab(existing.tabId, settings.sortMode)
            pages = inTab.ifEmpty { listOf(existing) }
            start = pages.indexOfFirst { it.id == existing.id }.coerceAtLeast(0)
        } else {
            val tabId = intent.getLongExtra(EXTRA_TAB, settings.currentTab).let { if (it == NoteTab.CALENDAR_ID) NoteTab.COMMON_ID else it }
            val day = intent.getLongExtra(EXTRA_DAY, 0L)
            var n = Note(
                tabId = tabId,
                type = if (intent.getBooleanExtra(EXTRA_LIST, false)) NoteType.LIST else NoteType.TEXT,
                color = settings.defaultColor,
            )
            if (day != 0L) {
                val now = Calendar.getInstance()
                val c = Calendar.getInstance().apply {
                    timeInMillis = day
                    set(Calendar.HOUR_OF_DAY, (now.get(Calendar.HOUR_OF_DAY) + 1).coerceAtMost(23)); set(Calendar.MINUTE, 0)
                }
                n = n.copy(
                    reminderEnabled = true, reminderType = ReminderType.ONE_TIME, reminderOneTimeDate = c.timeInMillis,
                    reminderWeekDay = c.get(Calendar.DAY_OF_WEEK), reminderMonthDay = c.get(Calendar.DAY_OF_MONTH),
                    reminderYearMonth = c.get(Calendar.MONTH) + 1,
                )
            }
            pages = listOf(n)
            start = 0
        }
        setContent {
            AppTheme {
                val pager = rememberPagerState(initialPage = start) { pages.size }
                HorizontalPager(pager, beyondViewportPageCount = 0) { i ->
                    NoteEditor(pages[i], widgetId, isNew = existing == null) { finish() }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_NOTE = "note_id"
        private const val EXTRA_TAB = "tab_id"
        private const val EXTRA_WIDGET = "widget_id"
        private const val EXTRA_DAY = "reminder_day"
        private const val EXTRA_LIST = "list"

        fun intent(
            context: Context,
            noteId: Long,
            tabId: Long? = null,
            widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
            reminderDay: Long = 0L,
            list: Boolean = false,
        ) = Intent(context, EditorActivity::class.java).apply {
            putExtra(EXTRA_NOTE, noteId)
            if (tabId != null) putExtra(EXTRA_TAB, tabId)
            putExtra(EXTRA_WIDGET, widgetId)
            putExtra(EXTRA_DAY, reminderDay)
            putExtra(EXTRA_LIST, list)
            // Jedinečná data, aby se PendingIntenty z různých widgetů nepřepisovaly.
            data = android.net.Uri.parse("poznamky://note/$noteId/$widgetId/$reminderDay/$list")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditor(initial: Note, widgetId: Int, isNew: Boolean, onClose: () -> Unit) {
    val context = LocalContext.current
    val settings = remember { Settings(context) }
    var note by remember { mutableStateOf(initial) }
    var saved by remember { mutableStateOf(initial) }
    var menu by remember { mutableStateOf(false) }
    var dialog by remember { mutableIntStateOf(0) } // 1 barva, 2 připomínka, 3 písmo, 4 záložka, 5 smazat

    fun save() {
        if (note == saved) return
        if (note.id == Note.NEW_ID && note.isEmpty()) return
        val result = Repo.saveNote(context, note)
        if (isNew && widgetId != AppWidgetManager.INVALID_APPWIDGET_ID && saved.id == Note.NEW_ID) {
            WidgetPrefs.setNoteId(context, widgetId, result.id)
            WidgetUpdater.updateAll(context)
        }
        saved = result
        note = result
    }
    val saveNow by rememberUpdatedState(::save)

    // Automatické ukládání při odchodu ze stránky / aplikace.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_PAUSE) saveNow() }
        lifecycle.addObserver(obs)
        onDispose { lifecycle.removeObserver(obs); saveNow() }
    }
    BackHandler { save(); onClose() }

    val bg = if (note.color == 0) MaterialTheme.colorScheme.surface else Color(note.color)
    val fg = if (note.color == 0 && note.fontColor == 0) MaterialTheme.colorScheme.onSurface else noteFg(note.color, note.fontColor)
    val tabs = remember { Repo.db(context).tabs() }

    when (dialog) {
        1 -> ColorPickerDialog(note.color, { dialog = 0 }) { note = note.copy(color = it); dialog = 0 }
        2 -> ReminderDialog(note, { dialog = 0 }) { note = it; dialog = 0; save() }
        3 -> FontDialog(note, { dialog = 0 }) { note = it; dialog = 0 }
        4 -> TabPickerDialog(tabs, note.tabId, { dialog = 0 }) { note = note.copy(tabId = it); dialog = 0 }
        5 -> ConfirmDialog("Přesunout poznámku do koše?", { dialog = 0 }) {
            dialog = 0
            if (note.id != Note.NEW_ID) { save(); Repo.trash(context, note.id) }
            saved = note // nic dalšího neukládat
            onClose()
        }
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bg, navigationIconContentColor = fg, actionIconContentColor = fg, titleContentColor = fg),
                title = { Text(tabs.firstOrNull { it.id == note.tabId }?.title ?: "", style = MaterialTheme.typography.titleSmall) },
                navigationIcon = { IconButton(onClick = { save(); onClose() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zpět") } },
                actions = {
                    IconButton(onClick = { note = note.copy(pinned = !note.pinned) }) {
                        Icon(if (note.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin, "Připnout")
                    }
                    IconButton(onClick = { dialog = 1 }) { Icon(Icons.Default.Palette, "Barva") }
                    IconButton(onClick = { dialog = 2 }) {
                        Icon(Icons.Default.Alarm, "Připomínka", tint = if (note.reminderEnabled) MaterialTheme.colorScheme.primary else fg)
                    }
                    IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Další") }
                    DropdownMenu(menu, { menu = false }) {
                        DropdownMenuItem(text = { Text(if (note.isList) "Převést na text" else "Převést na seznam") },
                            onClick = { note = note.toggledType(); menu = false })
                        if (note.isList) {
                            DropdownMenuItem(text = { Text("Odškrtnout vše") }, onClick = {
                                note = note.copy(items = note.items.map { it.copy(done = false) }); menu = false
                            })
                            DropdownMenuItem(text = { Text("Smazat hotové položky") }, onClick = {
                                note = note.copy(items = note.items.filterNot { it.done }); menu = false
                            })
                        }
                        DropdownMenuItem(text = { Text("Písmo…") }, onClick = { dialog = 3; menu = false })
                        DropdownMenuItem(text = { Text((if (note.readOnly) "✓ " else "") + "Jen pro čtení") },
                            onClick = { note = note.copy(readOnly = !note.readOnly); menu = false })
                        DropdownMenuItem(text = { Text("Přesunout do záložky…") }, onClick = { dialog = 4; menu = false })
                        DropdownMenuItem(text = { Text("Sdílet") }, onClick = { shareNote(context, note); menu = false })
                        DropdownMenuItem(text = { Text("Smazat") }, onClick = {
                            menu = false
                            if (settings.askBeforeDelete) dialog = 5 else {
                                if (note.id != Note.NEW_ID) { save(); Repo.trash(context, note.id) }
                                saved = note; onClose()
                            }
                        })
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 4.dp),
        ) {
            val fieldColors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = fg, unfocusedTextColor = fg, disabledTextColor = fg,
            )
            val titleColor = if (note.fontColorTitle != 0) Color(note.fontColorTitle) else fg
            TextField(
                value = note.title, onValueChange = { note = note.copy(title = it) },
                readOnly = note.readOnly, placeholder = { Text("Název", color = fg.copy(alpha = 0.5f)) },
                textStyle = TextStyle(fontSize = note.effectiveTitleFontSize.sp, fontWeight = FontWeight.Bold, color = titleColor),
                colors = fieldColors, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            if (note.isList) {
                ChecklistEditor(note, fg, fieldColors, settings.doneItemsBottom) { note = note.copy(items = it) }
            } else {
                TextField(
                    value = note.body, onValueChange = { note = note.copy(body = it) },
                    readOnly = note.readOnly, placeholder = { Text("Poznámka", color = fg.copy(alpha = 0.5f)) },
                    textStyle = TextStyle(fontSize = note.effectiveFontSize.sp, color = fg),
                    colors = fieldColors, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
            }
            Spacer(Modifier.height(24.dp))
            val attachments = remember(note.attachmentsJson) {
                try { JSONArray(note.attachmentsJson ?: "[]").length() } catch (e: Exception) { 0 }
            }
            val small = MaterialTheme.typography.bodySmall
            if (attachments > 0) Text("📎 Příloh: $attachments (zobrazení příloh zatím není hotové)", color = fg.copy(alpha = 0.6f), style = small,
                modifier = Modifier.padding(horizontal = 16.dp))
            if (note.reminderEnabled && note.reminderNextDate > 0) {
                Text("⏰ " + formatDateTime(context, note.reminderNextDate), color = fg.copy(alpha = 0.7f), style = small,
                    modifier = Modifier.padding(horizontal = 16.dp).clickable { dialog = 2 })
            }
            if (note.createdTimeStamp > 0) Text("Vytvořeno " + formatDateTime(context, note.createdTimeStamp), color = fg.copy(alpha = 0.6f),
                style = small, modifier = Modifier.padding(horizontal = 16.dp))
            if (note.timeStamp > 0) Text("Upraveno " + formatDateTime(context, note.timeStamp), color = fg.copy(alpha = 0.6f),
                style = small, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun ChecklistEditor(
    note: Note,
    fg: Color,
    colors: androidx.compose.material3.TextFieldColors,
    doneBottom: Boolean,
    onChange: (List<NoteItem>) -> Unit,
) {
    val items = note.items
    val order = remember(items, doneBottom) {
        items.indices.let { idx -> if (doneBottom) idx.sortedBy { items[it].done } else idx.toList() }
    }
    val requesters = remember { ArrayList<FocusRequester>() }
    while (requesters.size < items.size + 1) requesters.add(FocusRequester())
    var focusIndex by remember { mutableStateOf(-1) }
    LaunchedEffect(focusIndex, items.size) {
        if (focusIndex in items.indices) {
            runCatching { requesters[focusIndex].requestFocus() }
            focusIndex = -1
        }
    }

    order.forEach { i ->
        val item = items[i]
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = item.done,
                onCheckedChange = { c -> onChange(items.toMutableList().also { it[i] = item.copy(done = c) }) },
                colors = CheckboxDefaults.colors(uncheckedColor = fg, checkedColor = fg, checkmarkColor = if (note.color == 0) MaterialTheme.colorScheme.surface else Color(note.color)),
            )
            TextField(
                value = item.title,
                onValueChange = { v ->
                    if (v.contains('\n')) {
                        // Enter = nová položka pod aktuální.
                        val parts = v.split('\n')
                        val list = items.toMutableList()
                        list[i] = item.copy(title = parts.first())
                        list.addAll(i + 1, parts.drop(1).map { NoteItem(it) })
                        onChange(list)
                        focusIndex = i + parts.size - 1
                    } else {
                        onChange(items.toMutableList().also { it[i] = item.copy(title = v) })
                    }
                },
                readOnly = note.readOnly,
                textStyle = TextStyle(
                    fontSize = note.effectiveFontSize.sp, color = if (item.done) fg.copy(alpha = 0.6f) else fg,
                    textDecoration = if (item.done) TextDecoration.LineThrough else null,
                ),
                colors = colors,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.None),
                modifier = Modifier.weight(1f).focusRequester(requesters[i]),
            )
            if (!note.readOnly) IconButton(onClick = { onChange(items.toMutableList().also { it.removeAt(i) }) }) {
                Icon(Icons.Default.Close, "Odstranit položku", tint = fg.copy(alpha = 0.6f))
            }
        }
    }
    if (!note.readOnly) {
        TextButton(onClick = {
            val insertAt = if (doneBottom) items.indexOfFirst { it.done }.let { if (it < 0) items.size else it } else items.size
            onChange(items.toMutableList().also { it.add(insertAt, NoteItem("")) })
            focusIndex = insertAt
        }, modifier = Modifier.padding(start = 4.dp)) {
            Icon(Icons.Default.Add, null, tint = fg)
            Text(" Přidat položku", color = fg)
        }
    }
}
