package cz.teckatecka.poznamky.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RemoveDone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.PhotoCamera
import cz.teckatecka.poznamky.data.Attachments
import cz.teckatecka.poznamky.data.NoteAttachment
import cz.teckatecka.poznamky.data.attachments
import cz.teckatecka.poznamky.data.withAttachments
import cz.teckatecka.poznamky.data.Note
import cz.teckatecka.poznamky.data.NoteItem
import cz.teckatecka.poznamky.data.NoteTab
import cz.teckatecka.poznamky.data.NoteType
import cz.teckatecka.poznamky.data.ReminderType
import cz.teckatecka.poznamky.data.Repo
import cz.teckatecka.poznamky.data.Settings
import cz.teckatecka.poznamky.widget.WidgetPrefs
import cz.teckatecka.poznamky.widget.WidgetUpdater
import kotlinx.coroutines.launch
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
                val scope = rememberCoroutineScope()
                // Listování mezi poznámkami karty swipem i šipkami „1 / 25“ jako v originále.
                HorizontalPager(pager, beyondViewportPageCount = 0) { i ->
                    NoteEditor(pages[i], widgetId, isNew = existing == null, index = i, count = pages.size,
                        onPage = { p -> scope.launch { pager.animateScrollToPage(p.coerceIn(0, pages.size - 1)) } }) { finish() }
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

@Composable
private fun NoteEditor(
    initial: Note,
    widgetId: Int,
    isNew: Boolean,
    index: Int,
    count: Int,
    onPage: (Int) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val settings = remember { Settings(context) }
    var note by remember { mutableStateOf(initial) }
    var saved by remember { mutableStateOf(initial) }
    var menu by remember { mutableStateOf(false) }
    // 1 barva, 2 kalendář, 3 písmo, 4 karta, 5 odebrat, 6 písmo hlavy, 7 převod, 8 vymazat obsah, 9 výběr widgetu
    var dialog by remember { mutableIntStateOf(0) }
    var inWidget by remember { mutableStateOf(widgetsShowing(context, initial.id).isNotEmpty()) }

    // Zpět / znovu: historie stavů, psaní se slučuje do kroků po ~0,8 s.
    val history = remember { mutableListOf(initial) }
    var histIndex by remember { mutableIntStateOf(0) }
    var lastPush by remember { mutableLongStateOf(0L) }
    fun update(n: Note) {
        val now = android.os.SystemClock.uptimeMillis()
        if (now - lastPush > 800) {
            while (history.size > histIndex + 1) history.removeAt(history.lastIndex)
            history.add(n); histIndex = history.lastIndex
        } else {
            history[histIndex] = n
        }
        lastPush = now
        note = n
    }
    fun undo() { if (histIndex > 0) { histIndex--; note = history[histIndex]; lastPush = 0 } }
    fun redo() { if (histIndex < history.lastIndex) { histIndex++; note = history[histIndex]; lastPush = 0 } }

    fun save() {
        if (note == saved) return
        if (note.id == Note.NEW_ID && note.isEmpty()) return
        val result = Repo.saveNote(context, note)
        if (isNew && widgetId != AppWidgetManager.INVALID_APPWIDGET_ID && saved.id == Note.NEW_ID) {
            WidgetPrefs.setNoteId(context, widgetId, result.id)
            WidgetUpdater.updateAll(context)
            inWidget = true
        }
        saved = result
        note = result
    }
    val saveNow by rememberUpdatedState(::save)
    fun remove() {
        if (note.id != Note.NEW_ID) { save(); Repo.trash(context, note.id) }
        saved = note // nic dalšího neukládat
        onClose()
    }

    // Automatické ukládání při odchodu ze stránky / aplikace.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_PAUSE) saveNow() }
        lifecycle.addObserver(obs)
        onDispose { lifecycle.removeObserver(obs); saveNow() }
    }
    BackHandler { save(); onClose() }

    val bg = if (note.color == 0) MaterialTheme.colorScheme.background else Color(note.color)
    val fg = if (note.color == 0 && note.fontColor == 0) MaterialTheme.colorScheme.onSurface else noteFg(note.color, note.fontColor)
    val dim = fg.copy(alpha = 0.6f)
    val tabs = remember { Repo.db(context).tabs() }
    val align = if (note.reverseAlignment || settings.reverseAlignment) TextAlign.End else TextAlign.Start
    var attachMenu by remember { mutableStateOf(false) }
    val pickFiles = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        val added = uris.mapNotNull { runCatching { Attachments.addFromUri(context, it) }.getOrNull() }
        if (added.isNotEmpty()) update(note.withAttachments(note.attachments + added))
    }
    var photoName by remember { mutableStateOf<String?>(null) }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val n = photoName
        if (n != null) {
            if (ok) update(note.withAttachments(note.attachments + NoteAttachment(n, ""))) else Attachments.file(context, n).delete()
        }
        photoName = null
    }
    fun placeInWidget(w: Int) {
        save()
        if (note.id == Note.NEW_ID) return
        WidgetPrefs.setNoteId(context, w, note.id); WidgetUpdater.updateAll(context); inWidget = true
        Toast.makeText(context, "Poznámka byla umístěna do widgetu", Toast.LENGTH_SHORT).show()
    }

    when (dialog) {
        1 -> ColorPickerDialog(note.color, { dialog = 0 }) { update(note.copy(color = it)); dialog = 0 }
        2 -> ReminderDialog(note, { dialog = 0 }) { update(it); dialog = 0; save() }
        3 -> FontDialog(note, onDismiss = { dialog = 0 }) { update(it); dialog = 0 }
        4 -> TabPickerDialog(tabs, note.tabId, { dialog = 0 }) { update(note.copy(tabId = it)); dialog = 0 }
        5 -> ConfirmDialog("Odebrat poznámku do koše?", { dialog = 0 }) { dialog = 0; remove() }
        6 -> FontDialog(note, forTitle = true, onDismiss = { dialog = 0 }) { update(it); dialog = 0 }
        7 -> AlertDialog(
            onDismissRequest = { dialog = 0 },
            icon = { Icon(if (note.isList) Icons.AutoMirrored.Filled.Notes else Icons.Default.Checklist, null) },
            title = { Text(if (note.isList) "Převést na text?" else "Převést na seznam?") },
            text = { Text(if (note.isList) "Chcete převést tělo poznámky na text?" else "Chcete převést tělo poznámky na seznam?") },
            confirmButton = { TextButton(onClick = { update(note.toggledType()); dialog = 0 }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { dialog = 0 }) { Text("ZRUŠENÍ") } },
        )
        8 -> ConfirmDialog("Vymazat obsah poznámky?", { dialog = 0 }) { update(note.copy(body = "", items = emptyList())); dialog = 0 }
        9 -> {
            val widgets = remember { allNoteWidgets(context) }
            val db = remember { Repo.db(context) }
            AlertDialog(
                onDismissRequest = { dialog = 0 },
                title = { Text("Chcete-li poznámku umístit, vyberte widget") },
                text = {
                    Column {
                        widgets.forEachIndexed { i, w ->
                            val current = db.note(WidgetPrefs.noteId(context, w))?.let { it.title.ifBlank { it.plainText().take(30) } }
                            Text("Widget ${i + 1}: " + (current ?: "– prázdný –"),
                                Modifier.fillMaxWidth().clickable { dialog = 0; placeInWidget(w) }.padding(vertical = 12.dp))
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { dialog = 0 }) { Text("zrušení") } },
            )
        }
    }

    Scaffold(
        containerColor = bg,
        topBar = {
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).statusBarsPadding()
                    .height(64.dp).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { save(); onClose() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zpět") }
                PagerCounter(index, count, { save(); onPage(index - 1) }, { save(); onPage(index + 1) })
                Spacer(Modifier.weight(1f))
                IconButton(onClick = ::undo, enabled = histIndex > 0) { Icon(Icons.AutoMirrored.Filled.Undo, "Zpět o krok") }
                IconButton(onClick = ::redo, enabled = histIndex < history.lastIndex) { Icon(Icons.AutoMirrored.Filled.Redo, "Znovu") }
                IconButton(onClick = { update(note.copy(readOnly = !note.readOnly)) }) {
                    Icon(if (note.readOnly) Icons.Default.Lock else Icons.Default.LockOpen, if (note.readOnly) "Odemknout" else "Zámek")
                }
                IconButton(onClick = { dialog = 7 }) {
                    Icon(if (note.isList) Icons.AutoMirrored.Filled.Notes else Icons.Default.Checklist, "Text / seznam")
                }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Menu") }
                    DropdownMenu(menu, { menu = false }) {
                        @Composable
                        fun I(label: String, icon: ImageVector, trailing: (@Composable () -> Unit)? = null, action: () -> Unit) =
                            DropdownMenuItem(text = { Text(label) }, leadingIcon = { Icon(icon, null) }, trailingIcon = trailing,
                                onClick = { menu = false; action() })
                        I("Písmo hlavy", Icons.Default.Title) { dialog = 6 }
                        I("Zpětné seřízení", Icons.Default.FormatAlignRight, trailing = {
                            Checkbox(note.reverseAlignment, null)
                        }) { update(note.copy(reverseAlignment = !note.reverseAlignment)) }
                        if (note.isList) {
                            I("Seřadit vzestupně", Icons.Default.SortByAlpha) {
                                update(note.copy(items = note.items.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })))
                            }
                            I("Seřadit sestupně", Icons.Default.SortByAlpha) {
                                update(note.copy(items = note.items.sortedWith(compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.title })))
                            }
                        }
                        HorizontalDivider()
                        I("Přesunout na jinou kartu…", Icons.Default.DriveFileMove) { dialog = 4 }
                        I("Nastavení kalendáře", Icons.Default.CalendarMonth) { dialog = 2 }
                        HorizontalDivider()
                        I("Sdílet poznámku", Icons.Default.Share) { shareNote(context, note) }
                        I("Vytvořit zástupce", Icons.Default.AddToHomeScreen) { save(); createShortcut(context, note) }
                        HorizontalDivider()
                        if (note.isList) I("Odstraňte zaškrtnuté položky", Icons.Default.RemoveDone) {
                            update(note.copy(items = note.items.filterNot { it.done }))
                        }
                        I("Vymazat obsah", Icons.Default.CleaningServices) { dialog = 8 }
                        I("Odebrat poznámku", Icons.Default.Delete) { if (settings.askBeforeDelete) dialog = 5 else remove() }
                    }
                }
            }
        },
        bottomBar = { Column {
            if (note.attachments.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Box(Modifier.background(MaterialTheme.colorScheme.surfaceContainer)) {
                    AttachmentStrip(note.attachments, note.readOnly) { update(note.withAttachments(it)) }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).navigationBarsPadding()
                    .padding(start = 16.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(Modifier.weight(1f)) {
                    if (note.reminderEnabled && note.reminderNextDate > 0) Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { dialog = 2 }.padding(vertical = 2.dp),
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, Modifier.size(16.dp), tint = dim)
                        if (note.reminderNotification) Icon(Icons.Default.Alarm, null, Modifier.size(16.dp), tint = dim)
                        Text(" " + formatDateTime(context, note.reminderNextDate), color = dim, fontSize = 14.sp)
                        Icon(Icons.Default.Close, "Zrušit připomínku", Modifier.padding(start = 6.dp).size(16.dp)
                            .clickable { update(note.copy(reminderEnabled = false, reminderNextDate = 0)) }, tint = dim)
                    }
                    if (note.createdTimeStamp > 0) Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFC9A227)))
                        Text("  " + formatShort(context, note.createdTimeStamp), color = dim, fontSize = 14.sp)
                    }
                    if (note.timeStamp > 0) Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sync, null, Modifier.size(14.dp), tint = dim)
                        Text(" " + formatShort(context, note.timeStamp), color = dim, fontSize = 14.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    val white = Color(0xFFF5F5F5)
                    // W – zobrazit / odebrat poznámku ve widgetu
                    RoundButton(onClick = {
                        when {
                            inWidget -> {
                                widgetsShowing(context, note.id).forEach { WidgetPrefs.setNoteId(context, it, Note.NEW_ID) }
                                WidgetUpdater.updateAll(context); inWidget = false
                                Toast.makeText(context, "Poznámka byla odstraněna z widgetu", Toast.LENGTH_SHORT).show()
                            }
                            widgetId != AppWidgetManager.INVALID_APPWIDGET_ID -> placeInWidget(widgetId)
                            allNoteWidgets(context).size == 1 -> placeInWidget(allNoteWidgets(context).first())
                            allNoteWidgets(context).isNotEmpty() -> dialog = 9
                            else -> Toast.makeText(context, "Nejdřív přidejte na plochu widget Poznámky.", Toast.LENGTH_LONG).show()
                        }
                    }, background = if (inWidget) MaterialTheme.colorScheme.primary else white) {
                        Text("W", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = if (inWidget) MaterialTheme.colorScheme.onPrimary else Color(0xFF3D5272))
                    }
                    RoundButton(onClick = { dialog = 3 }, background = white) {
                        Text("T", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFFB71C1C), fontFamily = FontFamily.Serif)
                    }
                    RoundButton(onClick = { dialog = 1 }, background = white) {
                        Icon(Icons.Default.Palette, "Barva", tint = if (note.color != 0) Color(note.color) else Color(0xFFE53935))
                    }
                    Box {
                        RoundButton(onClick = { if (!note.readOnly) attachMenu = true }, background = white) {
                            Icon(Icons.Default.AttachFile, "Příloha", tint = Color(0xFF616161))
                        }
                        DropdownMenu(attachMenu, { attachMenu = false }) {
                            DropdownMenuItem(text = { Text("Soubor…") }, leadingIcon = { Icon(Icons.Default.AttachFile, null) },
                                onClick = { attachMenu = false; pickFiles.launch(arrayOf("*/*")) })
                            DropdownMenuItem(text = { Text("Fotoaparát") }, leadingIcon = { Icon(Icons.Default.PhotoCamera, null) }, onClick = {
                                attachMenu = false
                                val name = "photo_${System.currentTimeMillis()}.jpg"
                                Attachments.file(context, name).createNewFile()
                                photoName = name
                                runCatching { takePhoto.launch(Attachments.uri(context, name)) }
                                    .onFailure { Toast.makeText(context, "Fotoaparát není k dispozici", Toast.LENGTH_SHORT).show() }
                            })
                        }
                    }
                    RoundButton(onClick = { save(); onClose() }, background = Color(0xFF2E7D32), size = 60,
                        border = BorderStroke(3.dp, Color.White)) {
                        Icon(Icons.Default.Check, "Uložit", Modifier.size(36.dp), tint = Color.White)
                    }
                }
            }
        } },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().imePadding().verticalScroll(rememberScrollState())) {
            val fieldColors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = fg, unfocusedTextColor = fg, disabledTextColor = fg,
            )
            val titleColor = if (note.fontColorTitle != 0) Color(note.fontColorTitle) else fg
            // Štítek karty pod lištou vpravo (note_tab_frame_view) – klepnutím přesun na jinou kartu.
            val tabTitle = tabs.firstOrNull { it.id == note.tabId }?.title ?: ""
            val chipShape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
            Row(
                Modifier.align(Alignment.End).padding(end = 4.dp).clip(chipShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, chipShape).clickable { dialog = 4 }
                    .padding(start = 10.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (note.reminderEnabled) Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp).alpha(0.7f))
                Text(tabTitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontSize = 15.sp)
            }
            Row(verticalAlignment = Alignment.Top) {
                IconButton(onClick = { update(note.copy(pinned = !note.pinned)) }) {
                    Icon(if (note.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin, "Připnout", tint = if (note.pinned) fg else dim)
                }
                TextField(
                    value = note.title, onValueChange = { update(note.copy(title = it)) },
                    readOnly = note.readOnly, placeholder = { Text("Titul", color = dim, fontSize = note.effectiveTitleFontSize.sp) },
                    textStyle = TextStyle(fontSize = note.effectiveTitleFontSize.sp, color = titleColor, textAlign = align),
                    colors = fieldColors, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
            }
            if (note.isList) {
                ChecklistEditor(note, fg, fieldColors, settings.doneItemsBottom, settings.backspaceRemovesItem, align) { update(note.copy(items = it)) }
            } else if (note.readOnly) {
                // Zamčená poznámka: odkazy jsou klikací.
                Text(linkified(note.body), color = fg, fontSize = note.effectiveFontSize.sp, textAlign = align,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
            } else {
                TextField(
                    value = note.body, onValueChange = { update(note.copy(body = it)) },
                    placeholder = { Text("Napište text…", color = dim) },
                    textStyle = TextStyle(fontSize = note.effectiveFontSize.sp, color = fg, textAlign = align),
                    colors = fieldColors, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun formatShort(context: Context, ms: Long): String =
    java.text.SimpleDateFormat("dd.MM.yy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ms))

@Composable
private fun ChecklistEditor(
    note: Note,
    fg: Color,
    colors: androidx.compose.material3.TextFieldColors,
    doneBottom: Boolean,
    backspaceRemoves: Boolean,
    align: TextAlign,
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
                    textAlign = align,
                    textDecoration = if (item.done) TextDecoration.LineThrough else null,
                ),
                colors = colors,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.None),
                modifier = Modifier.weight(1f).focusRequester(requesters[i]).onPreviewKeyEvent { e ->
                    // Backspace v prázdné položce ji odstraní a skočí na předchozí (jako v originále).
                    if (backspaceRemoves && e.type == KeyEventType.KeyDown && e.key == Key.Backspace && item.title.isEmpty() && items.size > 1) {
                        onChange(items.toMutableList().also { it.removeAt(i) })
                        focusIndex = (i - 1).coerceAtLeast(0)
                        true
                    } else false
                },
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
