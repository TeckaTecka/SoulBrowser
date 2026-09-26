package cz.teckatecka.poznamky.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveDone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.teckatecka.poznamky.R
import cz.teckatecka.poznamky.data.Note
import cz.teckatecka.poznamky.data.NoteTab
import cz.teckatecka.poznamky.data.Repo
import cz.teckatecka.poznamky.data.Settings
import cz.teckatecka.poznamky.data.SortMode
import cz.teckatecka.poznamky.reminder.Reminders
import cz.teckatecka.poznamky.widget.WidgetUpdater
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

/** Akce z menu ⋮ na kartičce poznámky – stejné položky jako v původní appce. */
enum class CardAction { EDIT, LOCK, MOVE, CALENDAR, SHARE, SHORTCUT, REMOVE_CHECKED, CLEAR, DELETE }

private val contentModes = listOf(
    Triple("Plné zobrazení", Icons.Default.FormatAlignLeft, 0),
    Triple("Krátký náhled", Icons.AutoMirrored.Filled.Notes, 1),
    Triple("Pouze název", Icons.AutoMirrored.Filled.ShortText, 2),
)

private fun sortIcon(m: SortMode): ImageVector = when (m) {
    SortMode.MODIFIED -> Icons.Default.Schedule
    SortMode.MODIFIED_DESC -> Icons.AutoMirrored.Filled.Sort
    SortMode.TITLE, SortMode.TITLE_DESC -> Icons.Default.SortByAlpha
}

private val viewModes = listOf(
    Triple("Volný režim zobrazení", Icons.Default.Dashboard, 0),
    Triple("Režim zobrazení dvou sloupců", Icons.Default.GridView, 1),
    Triple("Režim zobrazení seznamu", Icons.AutoMirrored.Filled.ViewList, 2),
)

@Composable
private fun MainScreen(startTab: Long) {
    val context = LocalContext.current
    val version by Repo.version.collectAsState()
    val settings = remember { Settings(context) }
    val db = remember { Repo.db(context) }
    val tabs = remember(version) { db.tabs() }
    // Kalendář je v původní appce první kartou.
    val pages = remember(tabs) {
        (if (settings.showCalendarTab) listOf(NoteTab(NoteTab.CALENDAR_ID, "Kalendář")) else emptyList()) + tabs
    }
    val initial = if (startTab != Long.MIN_VALUE) startTab else settings.currentTab
    val pager = rememberPagerState(initialPage = pages.indexOfFirst { it.id == initial }.coerceAtLeast(0)) { pages.size }
    val scope = rememberCoroutineScope()
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val currentTabId = pages.getOrNull(pager.currentPage)?.id ?: NoteTab.COMMON_ID
    LaunchedEffect(currentTabId) { settings.currentTab = currentTabId }

    var sort by remember { mutableStateOf(settings.sortMode) }
    var viewMode by remember { mutableIntStateOf(settings.viewMode) }
    var contentMode by remember { mutableIntStateOf(settings.contentMode) }
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var menu by remember { mutableIntStateOf(0) } // 1 obsah, 2 řazení, 3 zobrazení
    var showTabs by remember { mutableStateOf(false) }
    var newTab by remember { mutableStateOf(false) }
    var about by remember { mutableStateOf(false) }
    var serviceOpen by remember { mutableStateOf(false) }
    var calendarDay by remember { mutableLongStateOf(startOfToday()) }
    var pending by remember { mutableStateOf<Pair<Note, CardAction>?>(null) }

    fun open(note: Note) = context.startActivity(EditorActivity.intent(context, note.id))
    fun newNote() {
        val onCalendar = currentTabId == NoteTab.CALENDAR_ID
        context.startActivity(EditorActivity.intent(context, Note.NEW_ID,
            tabId = if (onCalendar) settings.lastRealTab(tabs) else currentTabId,
            reminderDay = if (onCalendar) calendarDay else 0L))
    }
    fun goTo(i: Int) { scope.launch { pager.animateScrollToPage(i.coerceIn(0, pages.size - 1)) } }
    val onAction: (Note, CardAction) -> Unit = { n, a ->
        when (a) {
            CardAction.EDIT -> open(n)
            CardAction.LOCK -> Repo.saveNote(context, n.copy(readOnly = !n.readOnly), touch = false)
            CardAction.SHARE -> shareNote(context, n)
            CardAction.SHORTCUT -> createShortcut(context, n)
            CardAction.REMOVE_CHECKED -> Repo.saveNote(context, n.copy(items = n.items.filterNot { it.done }))
            CardAction.DELETE -> if (settings.askBeforeDelete) pending = n to a else Repo.trash(context, n.id)
            else -> pending = n to a
        }
    }

    ModalNavigationDrawer(drawerState = drawer, drawerContent = {
        ModalDrawerSheet {
            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(20.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(64.dp).clip(CircleShape)
                    .background(Color(0xFFF3DE8A)), tint = Color.Unspecified)
                Spacer(Modifier.width(16.dp))
                Text("Poznámky", fontSize = 28.sp)
            }
            fun close() = scope.launch { drawer.close() }
            @Composable
            fun Item(label: String, icon: ImageVector, indent: Boolean = false, trailing: (@Composable () -> Unit)? = null, onClick: () -> Unit) =
                NavigationDrawerItem(
                    label = { Text(label) }, selected = false, onClick = onClick,
                    icon = { Icon(icon, null) }, badge = trailing,
                    modifier = Modifier.padding(start = if (indent) 24.dp else 0.dp),
                )
            Column(Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                Item("Nová poznámka", Icons.Default.AddCircleOutline) { close(); newNote() }
                Item("Správa karet", Icons.Default.Tab) { close(); showTabs = true }
                if (settings.showCalendarTab) Item("Kalendář", Icons.Default.CalendarMonth) { close(); goTo(0) }
                Item("Odpadkový koš", Icons.Default.Delete) {
                    close(); context.startActivity(Intent(context, RecycleBinActivity::class.java))
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                Item("Nastavení", Icons.Default.Settings) {
                    close(); context.startActivity(Intent(context, SettingsActivity::class.java))
                }
                Item("Servisní funkce", Icons.Default.Build, trailing = {
                    Icon(if (serviceOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                }) { serviceOpen = !serviceOpen }
                if (serviceOpen) {
                    Item("Vytvořit zálohu", Icons.Default.CloudUpload, indent = true) {
                        close(); context.startActivity(SettingsActivity.intent(context, SettingsActivity.ACTION_BACKUP))
                    }
                    Item("Obnovit zálohu", Icons.Default.CloudDownload, indent = true) {
                        close(); context.startActivity(SettingsActivity.intent(context, SettingsActivity.ACTION_RESTORE))
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                Item("Obnovte všechny widgety", Icons.Default.Refresh) {
                    close(); WidgetUpdater.updateAll(context)
                    Toast.makeText(context, "Widgety obnoveny", Toast.LENGTH_SHORT).show()
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                Item("O aplikaci…", Icons.Default.Info) { close(); about = true }
            }
        }
    }) {
        Scaffold(
            floatingActionButton = {
                // Průhledné kulaté „+“ jako v originále.
                RoundButton(
                    onClick = ::newNote, size = 64,
                    background = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)),
                ) { Icon(Icons.Default.Add, "Nová poznámka", Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) }
            },
        ) { padding ->
            Column(Modifier.padding(bottom = padding.calculateBottomPadding()).fillMaxSize()) {
                // Horní lišta
                Row(
                    Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer).statusBarsPadding()
                        .height(64.dp).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { scope.launch { drawer.open() } }) { Icon(Icons.Default.Menu, "Menu") }
                    if (searching) {
                        TextField(
                            value = query, onValueChange = { query = it }, singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            trailingIcon = { IconButton(onClick = { searching = false; query = "" }) { Icon(Icons.Default.Close, "Zavřít") } },
                            placeholder = { Text("Hledejte text") },
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent),
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(Modifier.width(8.dp))
                        PagerCounter(pager.currentPage, pages.size, { goTo(pager.currentPage - 1) }, { goTo(pager.currentPage + 1) })
                        Spacer(Modifier.weight(1f))
                        Box {
                            IconButton(onClick = { menu = 1 }) { Icon(contentModes[contentMode].second, "Režim obsahu") }
                            DropdownMenu(menu == 1, { menu = 0 }) {
                                contentModes.forEach { (label, icon, v) ->
                                    DropdownMenuItem(text = { Text(label) }, trailingIcon = { Icon(icon, null) },
                                        onClick = { contentMode = v; settings.contentMode = v; menu = 0 })
                                }
                            }
                        }
                        Box {
                            IconButton(onClick = { menu = 2 }) { Icon(sortIcon(sort), "Řazení") }
                            DropdownMenu(menu == 2, { menu = 0 }) {
                                listOf(SortMode.MODIFIED, SortMode.MODIFIED_DESC, SortMode.TITLE, SortMode.TITLE_DESC).forEach { m ->
                                    DropdownMenuItem(text = { Text(m.label) }, trailingIcon = { Icon(sortIcon(m), null) },
                                        onClick = { sort = m; settings.sortMode = m; menu = 0 })
                                }
                            }
                        }
                        Box {
                            IconButton(onClick = { menu = 3 }) { Icon(viewModes[viewMode].second, "Zobrazení") }
                            DropdownMenu(menu == 3, { menu = 0 }) {
                                viewModes.forEach { (label, icon, v) ->
                                    DropdownMenuItem(text = { Text(label) }, trailingIcon = { Icon(icon, null) },
                                        onClick = { viewMode = v; settings.viewMode = v; menu = 0 })
                                }
                            }
                        }
                        IconButton(onClick = { searching = true }) { Icon(Icons.Default.Search, "Hledat") }
                    }
                }
                TabStrip(pages, pager.currentPage, ::goTo) { newTab = true }
                HorizontalPager(pager, Modifier.fillMaxSize(), beyondViewportPageCount = 1) { page ->
                    val tabId = pages[page].id
                    val q = query.trim().lowercase()
                    fun matches(n: Note) = q.isEmpty() || n.title.lowercase().contains(q) || n.body.lowercase().contains(q) ||
                        n.items.any { it.title.lowercase().contains(q) }
                    if (tabId == NoteTab.CALENDAR_ID) {
                        CalendarPane(version, calendarDay, { calendarDay = it }, ::open, onAction)
                    } else {
                        val notes = remember(version, tabId, sort, q) { db.notesInTab(tabId, sort).filter(::matches) }
                        if (notes.isEmpty() && q.isEmpty() && db.allActiveNotes().isEmpty()) ImportHint()
                        else NotesList(notes, viewMode, contentMode, ::open, onAction = onAction)
                    }
                }
            }
        }
    }

    if (showTabs) TabsDialog(tabs) { showTabs = false }
    if (newTab) TabsDialog(tabs, startWithNew = true) { newTab = false }
    if (about) AlertDialog(
        onDismissRequest = { about = false },
        title = { Text("Poznámky") },
        text = { Text("Bez reklam, bez internetu. Data zůstávají v telefonu.\nZálohy .bak jsou kompatibilní s aplikací My Notes.") },
        confirmButton = { TextButton(onClick = { about = false }) { Text("OK") } },
    )
    pending?.let { (n, a) ->
        val dismiss = { pending = null }
        when (a) {
            CardAction.MOVE -> TabPickerDialog(tabs, n.tabId, dismiss) { Repo.saveNote(context, n.copy(tabId = it), touch = false); dismiss() }
            CardAction.CALENDAR -> ReminderDialog(n, dismiss) { Repo.saveNote(context, it); dismiss() }
            CardAction.CLEAR -> ConfirmDialog("Vymazat obsah poznámky?", dismiss) {
                Repo.saveNote(context, n.copy(body = "", items = emptyList())); dismiss()
            }
            CardAction.DELETE -> ConfirmDialog("Odebrat poznámku do koše?", dismiss) { Repo.trash(context, n.id); dismiss() }
            else -> dismiss()
        }
    }
}

/** První skutečná karta – pro nové poznámky založené z Kalendáře. */
private fun Settings.lastRealTab(tabs: List<NoteTab>): Long = tabs.firstOrNull()?.id ?: NoteTab.COMMON_ID

/** Pás karet se zaoblenými horními rohy a tlačítkem „+“ vpravo. */
@Composable
private fun TabStrip(pages: List<NoteTab>, selected: Int, onSelect: (Int) -> Unit, onAdd: () -> Unit) {
    val state = rememberLazyListState()
    LaunchedEffect(selected) { if (pages.isNotEmpty()) state.animateScrollToItem((selected - 1).coerceAtLeast(0)) }
    Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically) {
        LazyRow(state = state, modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            itemsIndexed(pages, key = { _, t -> t.id }) { i, t ->
                val sel = i == selected
                val bg = if (t.color != 0) Color(t.color) else MaterialTheme.colorScheme.surfaceContainer
                val fg = when {
                    t.fontColor != 0 -> Color(t.fontColor)
                    t.color != 0 -> Color(cz.teckatecka.poznamky.data.contrastTextColor(t.color))
                    else -> MaterialTheme.colorScheme.onSurface
                }
                Box(
                    Modifier.clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                        .background(if (sel && t.color == 0) MaterialTheme.colorScheme.surfaceContainerHigh else bg)
                        .clickable { onSelect(i) }.padding(horizontal = 22.dp, vertical = 12.dp),
                ) {
                    Text(t.title, color = fg, fontSize = 17.sp, maxLines = 1,
                        textDecoration = if (sel) TextDecoration.Underline else null)
                }
            }
        }
        IconButton(onClick = onAdd) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, "Nová karta") }
        }
    }
}

@Composable
private fun ImportHint() {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Zatím tu nejsou žádné poznámky.", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Text(
            "Máte poznámky v původní aplikaci My Notes? Vytvořte v ní zálohu (Servisní funkce → Vytvořit zálohu) " +
                "a tady ji načtěte přes ☰ → Servisní funkce → Obnovit zálohu.",
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { context.startActivity(SettingsActivity.intent(context, SettingsActivity.ACTION_RESTORE)) }) {
            Text("Obnovit zálohu")
        }
    }
}

/** Seznam kartiček: 0 = volný (dlaždice), 1 = dva sloupce, 2 = seznam. */
@Composable
fun NotesList(
    notes: List<Note>,
    viewMode: Int,
    contentMode: Int,
    onOpen: (Note) -> Unit,
    header: (@Composable () -> Unit)? = null,
    onAction: ((Note, CardAction) -> Unit)? = null,
) {
    val padding = PaddingValues(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 120.dp)
    val empty: @Composable () -> Unit = {
        Text("Žádné poznámky", Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.outline)
    }
    if (viewMode == 1) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), contentPadding = padding, modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (header != null) item(span = { GridItemSpan(maxLineSpan) }) { header() }
            items(notes.size, key = { notes[it].id }) { i ->
                Box(Modifier.aspectRatio(0.9f)) { NoteCard(notes[i], contentMode, onOpen, onAction, Modifier.fillMaxHeight()) }
            }
            if (notes.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) { empty() }
        }
    } else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(if (viewMode == 0) 2 else 1), contentPadding = padding,
            verticalItemSpacing = 8.dp, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize(),
        ) {
            if (header != null) item(span = StaggeredGridItemSpan.FullLine) { header() }
            items(notes.size, key = { notes[it].id }, span = { i ->
                // Ve volném režimu zabírají dlouhé poznámky celou šířku, krátké se skládají vedle sebe.
                if (viewMode == 0 && notes[i].plainText().length > 160) StaggeredGridItemSpan.FullLine else StaggeredGridItemSpan.SingleLane
            }) { i -> NoteCard(notes[i], contentMode, onOpen, onAction) }
            if (notes.isEmpty()) item(span = StaggeredGridItemSpan.FullLine) { empty() }
        }
    }
}

@Composable
fun NoteCard(
    n: Note,
    contentMode: Int,
    onOpen: (Note) -> Unit,
    onAction: ((Note, CardAction) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val fg = noteFg(n.color, n.fontColor)
    val dim = fg.copy(alpha = 0.6f)
    var menu by remember { mutableStateOf(false) }
    val align = if (n.reverseAlignment) TextAlign.End else TextAlign.Start
    Card(
        colors = CardDefaults.cardColors(containerColor = noteBg(n.color)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { onOpen(n) },
    ) {
        Column(Modifier.padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onAction != null) {
                    IconButton(onClick = { Repo.saveNote(context, n.copy(pinned = !n.pinned), touch = false) }, Modifier.size(36.dp)) {
                        Icon(if (n.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin, "Připnout", Modifier.size(20.dp),
                            tint = if (n.pinned) fg else dim)
                    }
                }
                Text(
                    n.title, color = noteFg(n.color, n.fontColorTitle.takeIf { it != 0 } ?: n.fontColor),
                    fontSize = n.effectiveTitleFontSize.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = 4.dp, top = 6.dp, bottom = 6.dp),
                )
                if (n.readOnly) Icon(Icons.Default.Lock, "Zamčeno", Modifier.size(18.dp), tint = dim)
                if (onAction != null) Box {
                    IconButton(onClick = { menu = true }, Modifier.size(40.dp)) { Icon(Icons.Default.MoreVert, "Menu", tint = fg) }
                    CardMenu(n, menu, { menu = false }) { onAction(n, it) }
                }
            }
            Column(Modifier.padding(start = 4.dp, end = 8.dp)) {
                if (contentMode != 2) {
                    val short = contentMode == 1
                    if (n.isList) {
                        val shown = if (short) n.items.take(8) else n.items
                        shown.forEach { item ->
                            Text(
                                (if (item.done) "☑ " else "☐ ") + item.title, color = if (item.done) dim else fg,
                                fontSize = n.effectiveFontSize.sp, textAlign = align, modifier = Modifier.fillMaxWidth(),
                                textDecoration = if (item.done) TextDecoration.LineThrough else null,
                            )
                        }
                        if (short && n.items.size > 8) Text("…", color = fg)
                    } else if (n.body.isNotBlank()) {
                        Text(
                            linkified(n.body), color = fg, fontSize = n.effectiveFontSize.sp, textAlign = align,
                            maxLines = if (short) 10 else Int.MAX_VALUE, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                if (n.reminderEnabled && n.reminderNextDate > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                        Icon(Icons.Default.CalendarMonth, null, Modifier.size(14.dp), tint = dim)
                        Text(" " + formatDateTime(context, n.reminderNextDate), color = dim, fontSize = 12.sp)
                        if (n.reminderNotification) Icon(Icons.Default.Alarm, null, Modifier.padding(start = 4.dp).size(14.dp), tint = dim)
                    }
                }
            }
        }
    }
}

@Composable
fun CardMenu(n: Note, expanded: Boolean, onDismiss: () -> Unit, onPick: (CardAction) -> Unit) {
    DropdownMenu(expanded, onDismiss) {
        @Composable
        fun I(label: String, icon: ImageVector, a: CardAction) =
            DropdownMenuItem(text = { Text(label) }, leadingIcon = { Icon(icon, null) }, onClick = { onDismiss(); onPick(a) })
        I("Upravit…", Icons.Default.Edit, CardAction.EDIT)
        I(if (n.readOnly) "Odemknout" else "Zámek", if (n.readOnly) Icons.Default.LockOpen else Icons.Default.Lock, CardAction.LOCK)
        HorizontalDivider()
        I("Přesunout na jinou kartu…", Icons.Default.DriveFileMove, CardAction.MOVE)
        I("Nastavení kalendáře", Icons.Default.CalendarMonth, CardAction.CALENDAR)
        HorizontalDivider()
        I("Sdílet poznámku", Icons.Default.Share, CardAction.SHARE)
        I("Vytvořit zástupce", Icons.Default.AddToHomeScreen, CardAction.SHORTCUT)
        HorizontalDivider()
        if (n.isList) I("Odstraňte zaškrtnuté položky", Icons.Default.RemoveDone, CardAction.REMOVE_CHECKED)
        I("Vymazat obsah", Icons.Default.CleaningServices, CardAction.CLEAR)
        I("Odebrat poznámku", Icons.Default.Delete, CardAction.DELETE)
    }
}

fun formatDateTime(context: Context, ms: Long): String {
    val d = java.util.Date(ms)
    return android.text.format.DateFormat.getDateFormat(context).format(d) + " " +
        android.text.format.DateFormat.getTimeFormat(context).format(d)
}

// ---------- Kalendář ----------

@Composable
private fun CalendarPane(version: Int, day: Long, onDay: (Long) -> Unit, onOpen: (Note) -> Unit, onAction: (Note, CardAction) -> Unit) {
    val context = LocalContext.current
    var month by remember { mutableLongStateOf(firstOfMonth(day)) }
    val calNotes = remember(version) { Repo.db(context).activeCalendarNotes() }
    val dayNotes = remember(version, day) { Reminders.notesOn(context, day) }
    val settings = remember { Settings(context) }

    NotesList(dayNotes, 2, settings.contentMode, onOpen, onAction = onAction, header = {
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
        title = { Text("Přesunout na jinou kartu") },
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
