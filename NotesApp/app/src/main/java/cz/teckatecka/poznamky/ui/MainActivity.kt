package cz.teckatecka.poznamky.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.teckatecka.poznamky.data.Note
import cz.teckatecka.poznamky.data.NoteTab
import cz.teckatecka.poznamky.data.Repo
import cz.teckatecka.poznamky.data.Settings
import cz.teckatecka.poznamky.data.SortMode
import cz.teckatecka.poznamky.reminder.Reminders
import cz.teckatecka.poznamky.widget.dayTitle
import cz.teckatecka.poznamky.widget.startOfDay
import cz.teckatecka.poznamky.widget.startOfToday
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        val startTab = intent.getLongExtra(EXTRA_TAB, Long.MIN_VALUE)
        setContent { AppTheme { MainScreen(startTab) } }
    }

    companion object {
        private const val EXTRA_TAB = "tab"
        fun intent(context: Context, tabId: Long) = Intent(context, MainActivity::class.java).putExtra(EXTRA_TAB, tabId)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(startTab: Long) {
    val context = LocalContext.current
    val version by Repo.version.collectAsState()
    val settings = remember { Settings(context) }
    val db = remember { Repo.db(context) }
    val tabs = remember(version) { db.tabs() }
    val pages = remember(tabs) {
        tabs.map { it.id to it.title } + if (settings.showCalendarTab) listOf(NoteTab.CALENDAR_ID to "Kalendář") else emptyList()
    }
    val initial = if (startTab != Long.MIN_VALUE) startTab else settings.currentTab
    val pager = rememberPagerState(initialPage = pages.indexOfFirst { it.first == initial }.coerceAtLeast(0)) { pages.size }
    val scope = rememberCoroutineScope()
    val currentTabId = pages.getOrNull(pager.currentPage)?.first ?: NoteTab.COMMON_ID
    LaunchedEffect(currentTabId) { settings.currentTab = currentTabId }

    var sort by remember { mutableStateOf(settings.sortMode) }
    var viewMode by remember { mutableIntStateOf(settings.viewMode) }
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }
    var sortMenu by remember { mutableStateOf(false) }
    var showTabs by remember { mutableStateOf(false) }
    var actionsFor by remember { mutableStateOf<Note?>(null) }

    fun open(note: Note) = context.startActivity(EditorActivity.intent(context, note.id, tabId = note.tabId))
    fun newNote(list: Boolean, day: Long = 0L) {
        val tab = if (currentTabId == NoteTab.CALENDAR_ID) NoteTab.COMMON_ID else currentTabId
        context.startActivity(EditorActivity.intent(context, Note.NEW_ID, tabId = tab, list = list,
            reminderDay = if (currentTabId == NoteTab.CALENDAR_ID) (if (day != 0L) day else startOfToday()) else 0L))
    }
    var calendarDay by remember { mutableLongStateOf(startOfToday()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (searching) {
                        TextField(
                            value = query, onValueChange = { query = it }, singleLine = true,
                            placeholder = { Text("Hledat v poznámkách") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else Text("Poznámky")
                },
                navigationIcon = {
                    if (searching) IconButton(onClick = { searching = false; query = "" }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zpět")
                    }
                },
                actions = {
                    if (!searching) IconButton(onClick = { searching = true }) { Icon(Icons.Default.Search, "Hledat") }
                    else if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, "Smazat") }
                    IconButton(onClick = {
                        viewMode = 1 - viewMode; settings.viewMode = viewMode
                    }) { Icon(if (viewMode == 0) Icons.Default.ViewAgenda else Icons.Default.GridView, "Zobrazení") }
                    Box {
                        IconButton(onClick = { sortMenu = true }) { Icon(Icons.AutoMirrored.Filled.Sort, "Řazení") }
                        DropdownMenu(sortMenu, { sortMenu = false }) {
                            SortMode.entries.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text((if (m == sort) "✓ " else "") + m.label) },
                                    onClick = { sort = m; settings.sortMode = m; sortMenu = false },
                                )
                            }
                        }
                    }
                    Box {
                        IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Menu") }
                        DropdownMenu(menu, { menu = false }) {
                            DropdownMenuItem(text = { Text("Záložky…") }, onClick = { menu = false; showTabs = true })
                            DropdownMenuItem(text = { Text("Koš") }, onClick = {
                                menu = false; context.startActivity(Intent(context, RecycleBinActivity::class.java))
                            })
                            DropdownMenuItem(text = { Text("Nastavení a zálohy") }, onClick = {
                                menu = false; context.startActivity(Intent(context, SettingsActivity::class.java))
                            })
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (!searching) Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallFloatingActionButton(onClick = { newNote(true, calendarDay) }) { Icon(Icons.Default.Checklist, "Nový seznam") }
                FloatingActionButton(onClick = { newNote(false, calendarDay) }) { Icon(Icons.Default.Add, "Nová poznámka") }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (searching && query.isNotBlank()) {
                val q = query.trim().lowercase()
                val found = remember(version, q, sort) {
                    db.allActiveNotes(sort).filter { n ->
                        n.title.lowercase().contains(q) || n.body.lowercase().contains(q) ||
                            n.items.any { it.title.lowercase().contains(q) }
                    }
                }
                NotesList(found, viewMode, settings.contentMode, ::open) { actionsFor = it }
                return@Column
            }
            if (pages.size > 1) {
                PrimaryScrollableTabRow(selectedTabIndex = pager.currentPage, edgePadding = 8.dp) {
                    pages.forEachIndexed { i, (id, title) ->
                        val color = tabs.firstOrNull { it.id == id }?.color ?: 0
                        Tab(
                            selected = pager.currentPage == i,
                            onClick = { scope.launch { pager.animateScrollToPage(i) } },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (color != 0) Box(Modifier.size(10.dp).clip(CircleShape).background(Color(color)))
                                    if (color != 0) Spacer(Modifier.width(6.dp))
                                    Text(title, maxLines = 1)
                                }
                            },
                        )
                    }
                }
            }
            HorizontalPager(pager, Modifier.fillMaxSize(), beyondViewportPageCount = 1) { page ->
                val tabId = pages[page].first
                if (tabId == NoteTab.CALENDAR_ID) {
                    CalendarPane(version, calendarDay, { calendarDay = it }, ::open) { actionsFor = it }
                } else {
                    val notes = remember(version, tabId, sort) { db.notesInTab(tabId, sort) }
                    if (notes.isEmpty() && db.allActiveNotes().isEmpty()) {
                        ImportHint()
                    } else {
                        NotesList(notes, viewMode, settings.contentMode, ::open) { actionsFor = it }
                    }
                }
            }
        }
    }

    if (showTabs) TabsDialog(tabs) { showTabs = false }
    actionsFor?.let { n -> NoteActionsDialog(n, tabs) { actionsFor = null } }
}

@Composable
private fun ImportHint() {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Zatím tu nejsou žádné poznámky.", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Text(
            "Máte poznámky v původní aplikaci My Notes? Vytvořte v ní zálohu (Nastavení → Zálohovat) " +
                "a tady ji načtěte přes Nastavení a zálohy → Obnovit ze zálohy.",
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { context.startActivity(Intent(context, SettingsActivity::class.java)) }) {
            Text("Otevřít zálohy")
        }
    }
}

@Composable
fun NotesList(
    notes: List<Note>,
    viewMode: Int,
    contentMode: Int,
    onOpen: (Note) -> Unit,
    header: (@Composable () -> Unit)? = null,
    onLongPress: (Note) -> Unit,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(if (viewMode == 0) 2 else 1),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 160.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (header != null) item(span = StaggeredGridItemSpan.FullLine) { header() }
        items(notes, key = { it.id }) { n -> NoteCard(n, contentMode, onOpen, onLongPress) }
        if (notes.isEmpty()) item(span = StaggeredGridItemSpan.FullLine) {
            Text("Žádné poznámky", Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(n: Note, contentMode: Int, onOpen: (Note) -> Unit, onLongPress: (Note) -> Unit) {
    val fg = noteFg(n.color, n.fontColor)
    Card(
        colors = CardDefaults.cardColors(containerColor = noteBg(n.color)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = { onOpen(n) }, onLongClick = { onLongPress(n) }),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (n.title.isNotBlank()) {
                    Text(
                        n.title, color = noteFg(n.color, n.fontColorTitle.takeIf { it != 0 } ?: n.fontColor),
                        fontWeight = FontWeight.Bold, fontSize = (n.effectiveTitleFontSize - 2).sp,
                        maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                    )
                } else Spacer(Modifier.weight(1f))
                if (n.reminderEnabled) Icon(Icons.Default.Alarm, null, Modifier.size(16.dp), tint = fg)
                if (n.pinned) Icon(Icons.Default.PushPin, null, Modifier.size(16.dp), tint = fg)
            }
            if (contentMode != 2) {
                val maxLines = if (contentMode == 1) 8 else Int.MAX_VALUE
                if (n.isList) {
                    n.items.take(if (contentMode == 1) 8 else n.items.size).forEach { item ->
                        Text(
                            (if (item.done) "☑ " else "☐ ") + item.title, color = fg, fontSize = (n.effectiveFontSize - 1).sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            textDecoration = if (item.done) TextDecoration.LineThrough else null,
                        )
                    }
                    if (contentMode == 1 && n.items.size > 8) Text("…", color = fg)
                } else if (n.body.isNotBlank()) {
                    Text(n.body, color = fg, fontSize = (n.effectiveFontSize - 1).sp, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
                }
            }
            if (n.reminderEnabled && n.reminderNextDate > 0) {
                Text(formatDateTime(LocalContext.current, n.reminderNextDate), color = fg.copy(alpha = 0.7f), fontSize = 11.sp)
            }
        }
    }
}

fun formatDateTime(context: Context, ms: Long): String {
    val d = java.util.Date(ms)
    return android.text.format.DateFormat.getDateFormat(context).format(d) + " " +
        android.text.format.DateFormat.getTimeFormat(context).format(d)
}

// ---------- Kalendář ----------

@Composable
private fun CalendarPane(version: Int, day: Long, onDay: (Long) -> Unit, onOpen: (Note) -> Unit, onLongPress: (Note) -> Unit) {
    val context = LocalContext.current
    var month by remember { mutableLongStateOf(firstOfMonth(day)) }
    val calNotes = remember(version) { Repo.db(context).activeCalendarNotes() }
    val dayNotes = remember(version, day) { Reminders.notesOn(context, day) }
    val settings = remember { Settings(context) }

    NotesList(dayNotes, 1, settings.contentMode, onOpen, onLongPress = onLongPress, header = {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { month = addMonths(month, -1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Předchozí měsíc") }
                Text(
                    monthTitle(month),
                    Modifier.weight(1f).clickable { month = firstOfMonth(startOfToday()); onDay(startOfToday()) },
                    textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium,
                )
                IconButton(onClick = { month = addMonths(month, 1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Další měsíc") }
            }
            MonthGrid(month, day, calNotes, onDay)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text(dayTitle(context, day), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp))
        }
    })
}

private fun monthTitle(ms: Long): String =
    java.text.SimpleDateFormat("LLLL yyyy", java.util.Locale.getDefault()).format(java.util.Date(ms))
        .replaceFirstChar { it.uppercase() }

private fun firstOfMonth(ms: Long) = Calendar.getInstance().apply {
    timeInMillis = startOfDay(ms); set(Calendar.DAY_OF_MONTH, 1)
}.timeInMillis

private fun addMonths(ms: Long, d: Int) = Calendar.getInstance().apply { timeInMillis = ms; add(Calendar.MONTH, d) }.timeInMillis

@Composable
private fun MonthGrid(month: Long, selected: Long, notes: List<Note>, onDay: (Long) -> Unit) {
    val cal = Calendar.getInstance().apply { timeInMillis = month }
    val firstDow = cal.firstDayOfWeek
    val offset = Math.floorMod(cal.get(Calendar.DAY_OF_WEEK) - firstDow, 7)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = startOfToday()
    val names = DateFormatSymbols.getInstance().shortWeekdays
    Column {
        Row {
            (0 until 7).forEach { i ->
                val dow = (firstDow - 1 + i) % 7 + 1
                Text(names[dow], Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
        val cells = offset + daysInMonth
        val rows = (cells + 6) / 7
        for (r in 0 until rows) {
            Row {
                for (col in 0 until 7) {
                    val dayNum = r * 7 + col - offset + 1
                    if (dayNum !in 1..daysInMonth) {
                        Spacer(Modifier.weight(1f)); continue
                    }
                    val dayMs = Calendar.getInstance().apply { timeInMillis = month; set(Calendar.DAY_OF_MONTH, dayNum) }.timeInMillis
                    val has = notes.any { Reminders.occursOn(it, dayMs) }
                    val isSel = dayMs == selected
                    Box(
                        Modifier.weight(1f).aspectRatio(1.2f).padding(2.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable { onDay(dayMs) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "$dayNum",
                            fontWeight = if (dayMs == today) FontWeight.Bold else FontWeight.Normal,
                            color = if (dayMs == today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                        if (has) Box(
                            Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp).size(5.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            }
        }
    }
}

// ---------- Dialogy ----------

@Composable
private fun NoteActionsDialog(n: Note, tabs: List<NoteTab>, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(0) } // 0 akce, 1 barva, 2 záložka, 3 potvrzení smazání
    when (mode) {
        1 -> ColorPickerDialog(n.color, onDismiss) { Repo.saveNote(context, n.copy(color = it), touch = false); onDismiss() }
        2 -> TabPickerDialog(tabs, n.tabId, onDismiss) { Repo.saveNote(context, n.copy(tabId = it), touch = false); onDismiss() }
        3 -> ConfirmDialog("Přesunout poznámku do koše?", onDismiss) { Repo.trash(context, n.id); onDismiss() }
        else -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(n.title.ifBlank { "Poznámka" }, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            text = {
                LazyColumn {
                    val actions = listOf<Pair<String, () -> Unit>>(
                        (if (n.pinned) "Odepnout" else "Připnout nahoru") to {
                            Repo.saveNote(context, n.copy(pinned = !n.pinned), touch = false); onDismiss()
                        },
                        "Barva…" to { mode = 1 },
                        "Přesunout do záložky…" to { mode = 2 },
                        "Duplikovat" to {
                            Repo.saveNote(context, n.copy(id = Note.NEW_ID, createdTimeStamp = System.currentTimeMillis())); onDismiss()
                        },
                        "Sdílet" to { shareNote(context, n); onDismiss() },
                        "Smazat" to {
                            if (Settings(context).askBeforeDelete) mode = 3 else { Repo.trash(context, n.id); onDismiss() }
                        },
                    )
                    items(actions) { (label, action) ->
                        Text(label, Modifier.fillMaxWidth().clickable { action() }.padding(vertical = 14.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Zavřít") } },
        )
    }
}

fun shareNote(context: Context, n: Note) {
    val text = listOf(n.title, n.plainText()).filter { it.isNotBlank() }.joinToString("\n\n")
    context.startActivity(
        Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), "Sdílet"),
    )
}

@Composable
fun ConfirmDialog(text: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Ano") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Zrušit") } },
    )
}

@Composable
fun TabPickerDialog(tabs: List<NoteTab>, current: Long, onDismiss: () -> Unit, onPick: (Long) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Záložka") },
        text = {
            LazyColumn {
                items(tabs) { t ->
                    Text(
                        (if (t.id == current) "✓ " else "") + t.title,
                        Modifier.fillMaxWidth().clickable { onPick(t.id) }.padding(vertical = 14.dp),
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Zrušit") } },
    )
}
