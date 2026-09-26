package cz.teckatecka.poznamky.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cz.teckatecka.poznamky.data.Note
import cz.teckatecka.poznamky.data.NotesDb
import cz.teckatecka.poznamky.data.Repo
import cz.teckatecka.poznamky.data.Settings
import cz.teckatecka.poznamky.widget.WidgetPrefs
import cz.teckatecka.poznamky.widget.WidgetUpdater
import java.io.File
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleScaffold(title: String, onBack: () -> Unit, actions: @Composable () -> Unit = {}, content: @Composable (Modifier) -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zpět") } },
            actions = { actions() },
        )
    }) { padding -> content(Modifier.padding(padding)) }
}

// ---------- Koš ----------

class RecycleBinActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppTheme { RecycleBin { finish() } } }
    }
}

@Composable
private fun RecycleBin(onBack: () -> Unit) {
    val context = LocalContext.current
    val version by Repo.version.collectAsState()
    val notes = remember(version) { Repo.db(context).deletedNotes() }
    var selected by remember { mutableStateOf<Note?>(null) }
    var confirmEmpty by remember { mutableStateOf(false) }
    SimpleScaffold("Koš", onBack, actions = {
        if (notes.isNotEmpty()) TextButton(onClick = { confirmEmpty = true }) { Text("Vysypat") }
    }) { m ->
        Column(m) { NotesList(notes, 2, 1, { selected = it }) }
    }
    selected?.let { n ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(n.title.ifBlank { "Poznámka" }) },
            text = { Text(n.plainText().take(400)) },
            confirmButton = { TextButton(onClick = { Repo.restore(context, n.id); selected = null }) { Text("Obnovit") } },
            dismissButton = { TextButton(onClick = { Repo.deleteForever(context, n.id); selected = null }) { Text("Smazat navždy") } },
        )
    }
    if (confirmEmpty) ConfirmDialog("Trvale smazat všechny poznámky v koši?", { confirmEmpty = false }) {
        Repo.emptyTrash(context); confirmEmpty = false
    }
}

// ---------- Nastavení a zálohy ----------

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val action = intent.getStringExtra(EXTRA_ACTION)
        setContent { AppTheme { SettingsScreen(action) { finish() } } }
    }

    companion object {
        private const val EXTRA_ACTION = "action"
        const val ACTION_BACKUP = "backup"
        const val ACTION_RESTORE = "restore"
        fun intent(context: Context, action: String? = null) =
            Intent(context, SettingsActivity::class.java).putExtra(EXTRA_ACTION, action)
    }
}

object Backup {
    /** Záloha = kopie SQLite databáze, stejný formát (.bak) jako původní My Notes. */
    fun export(context: Context, uri: Uri) {
        val db = NotesDb.get(context)
        db.writableDatabase.rawQuery("PRAGMA wal_checkpoint(FULL)", null).use { it.moveToFirst() }
        val file = context.getDatabasePath(NotesDb.NAME)
        context.contentResolver.openOutputStream(uri)?.use { out -> file.inputStream().use { it.copyTo(out) } }
    }

    fun import(context: Context, uri: Uri) {
        val tmp = File(context.cacheDir, "import.bak")
        context.contentResolver.openInputStream(uri)?.use { input -> tmp.outputStream().use { input.copyTo(it) } }
        try {
            NotesDb.get(context).replaceWithBackup(tmp)
        } finally {
            tmp.delete()
        }
        Repo.changed(context)
    }

    /** Název jako v původní appce: RRRRMMDD_HHMMSSmmm.bak */
    fun fileName(): String = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US).format(Date()) + ".bak"
    fun zipNameFor(bakName: String) = bakName.removeSuffix(".bak") + "_attachments.zip"

    // ---------- složka se zálohami (jako složka Notes v originále) ----------
    private const val PREF_FOLDER = "backup_folder"

    fun folder(context: Context): androidx.documentfile.provider.DocumentFile? {
        val uri = context.getSharedPreferences("settings", Context.MODE_PRIVATE).getString(PREF_FOLDER, null)?.let(Uri::parse) ?: return null
        val granted = context.contentResolver.persistedUriPermissions.any { it.uri == uri && it.isWritePermission }
        if (!granted) return null
        return androidx.documentfile.provider.DocumentFile.fromTreeUri(context, uri)?.takeIf { it.canWrite() }
    }

    fun setFolder(context: Context, uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        )
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putString(PREF_FOLDER, uri.toString()).apply()
    }

    fun listBackups(folder: androidx.documentfile.provider.DocumentFile) =
        folder.listFiles().filter { it.isFile && it.name?.endsWith(".bak", ignoreCase = true) == true }.sortedByDescending { it.name }

    /** Plná záloha = .bak + …_attachments.zip vedle sebe; lehká jen .bak. */
    fun backupToFolder(context: Context, folder: androidx.documentfile.provider.DocumentFile, full: Boolean): String {
        val name = fileName()
        val bak = folder.createFile("application/octet-stream", name) ?: error("Nelze vytvořit soubor")
        export(context, bak.uri)
        var extra = ""
        if (full) {
            val zip = folder.createFile("application/zip", zipNameFor(name)) ?: error("Nelze vytvořit soubor příloh")
            val n = cz.teckatecka.poznamky.data.Attachments.exportZip(context, zip.uri)
            extra = "\n+ přílohy: $n souborů"
        }
        return "Záložní soubor byl úspěšně vytvořen\n${bak.name}$extra"
    }

    /** Obnoví .bak a automaticky i přílohy ze stejnojmenného …_attachments.zip ve stejné složce. */
    fun restoreFromFolder(context: Context, folder: androidx.documentfile.provider.DocumentFile, bak: androidx.documentfile.provider.DocumentFile): String {
        import(context, bak.uri)
        val notes = Repo.db(context).allActiveNotes().size
        val zip = folder.findFile(zipNameFor(bak.name ?: ""))
        val att = if (zip != null) "\nNačteno příloh: ${cz.teckatecka.poznamky.data.Attachments.importZip(context, zip.uri)}" else ""
        return "Operace obnovení byla úspěšně dokončena\nPoznámek: $notes$att"
    }
}

@Composable
private fun SettingsScreen(startAction: String?, onBack: () -> Unit) {
    val context = LocalContext.current
    val s = remember { Settings(context) }
    var tick by remember { mutableStateOf(0) }
    var pendingImport by remember { mutableStateOf<Uri?>(null) }
    var askFull by remember { mutableStateOf(false) }
    var askZip by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf<String?>(null) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    fun toast(t: String) = Toast.makeText(context, t, Toast.LENGTH_LONG).show()
    /** Práce se soubory na pozadí s oknem „Čekejte…“ (zip může mít desítky MB). */
    fun work(label: String, job: () -> String) {
        busy = label
        scope.launch {
            val msg = runCatching { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { job() } }
                .getOrElse { "Chyba: ${it.message}" }
            busy = null
            toast(msg)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> pendingImport = uri }
    var backupFolder by remember { mutableStateOf(Backup.folder(context)) }
    var afterFolder by remember { mutableStateOf<String?>(null) }
    var pickBackup by remember { mutableStateOf(false) }
    var confirmBak by remember { mutableStateOf<androidx.documentfile.provider.DocumentFile?>(null) }
    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            runCatching { Backup.setFolder(context, uri) }
            backupFolder = Backup.folder(context)
            when (afterFolder) {
                SettingsActivity.ACTION_BACKUP -> askFull = true
                SettingsActivity.ACTION_RESTORE -> pickBackup = true
            }
        }
        afterFolder = null
    }
    /** Záloha / obnova vždy přes složku – napoprvé si ji appka vyžádá. */
    fun withFolder(action: String) {
        if (backupFolder == null) { afterFolder = action; folderLauncher.launch(null) }
        else if (action == SettingsActivity.ACTION_BACKUP) askFull = true else pickBackup = true
    }
    fun startBackup(full: Boolean) {
        val folder = backupFolder
        if (folder == null) { withFolder(SettingsActivity.ACTION_BACKUP); return }
        work(if (full) "Vytvářím plnou zálohu…" else "Vytvářím zálohu…") { Backup.backupToFolder(context, folder, full) }
    }
    val zipImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) work("Načítám přílohy…") {
            val n = cz.teckatecka.poznamky.data.Attachments.importZip(context, uri)
            "Načteno příloh: $n"
        }
    }
    // Spuštěno z bočního menu „Servisní funkce“.
    var launched by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (launched) return@LaunchedEffect
        launched = true
        when (startAction) {
            SettingsActivity.ACTION_BACKUP -> withFolder(SettingsActivity.ACTION_BACKUP)
            SettingsActivity.ACTION_RESTORE -> withFolder(SettingsActivity.ACTION_RESTORE)
        }
    }

    pendingImport?.let { uri ->
        ConfirmDialog("Obnovit ze zálohy? Všechna místní data budou přepsána údaji z tohoto souboru.", { pendingImport = null }) {
            pendingImport = null
            runCatching { Backup.import(context, uri) }
                .onSuccess {
                    toast("Operace obnovení byla úspěšně dokončena: ${Repo.db(context).allActiveNotes().size} poznámek")
                    // Pokud poznámky odkazují na přílohy, které tu ještě nejsou, nabídneme načíst zip.
                    val missing = Repo.db(context).allNotesIncludingDeleted().flatMap { n ->
                        cz.teckatecka.poznamky.data.NoteAttachment.listFromJson(n.attachmentsJson)
                    }.count { !cz.teckatecka.poznamky.data.Attachments.exists(context, it.name) }
                    if (missing > 0) askZip = true
                }
                .onFailure { toast("Operace obnovení se nezdařila! ${it.message}") }
        }
    }
    if (pickBackup) {
        val folder = backupFolder
        val files = remember(folder) { folder?.let { Backup.listBackups(it) } ?: emptyList() }
        AlertDialog(
            onDismissRequest = { pickBackup = false },
            title = { Text("Vyberte název záložního souboru, který chcete obnovit") },
            text = {
                androidx.compose.foundation.lazy.LazyColumn {
                    if (files.isEmpty()) item { Text("Ve složce „${folder?.name ?: ""}“ nejsou žádné soubory .bak.") }
                    items(files.size) { i ->
                        val f = files[i]
                        val hasZip = folder?.findFile(Backup.zipNameFor(f.name ?: "")) != null
                        Column(Modifier.fillMaxWidth().clickable { pickBackup = false; confirmBak = f }.padding(vertical = 10.dp)) {
                            Text(f.name ?: "")
                            Text(if (hasZip) "+ přílohy (${Backup.zipNameFor(f.name ?: "")})" else "bez příloh",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { pickBackup = false }) { Text("zrušení") } },
            dismissButton = {
                TextButton(onClick = { pickBackup = false; afterFolder = SettingsActivity.ACTION_RESTORE; folderLauncher.launch(null) }) {
                    Text("Jiná složka…")
                }
            },
        )
    }
    confirmBak?.let { f ->
        ConfirmDialog("Jste si jisti, že chcete obnovit soubor databáze:\n\n${f.name}\n\nVšechna místní data budou přepsána údaji z tohoto souboru.", { confirmBak = null }) {
            confirmBak = null
            val folder = backupFolder ?: return@ConfirmDialog
            work("Obnovuji zálohu…") { Backup.restoreFromFolder(context, folder, f) }
        }
    }
    if (askFull) AlertDialog(
        onDismissRequest = { askFull = false },
        title = { Text("Vytvořte záložní soubor") },
        text = { Text("Chcete vytvořit úplnou zálohu?\n\nV úplném archivu bude text všech poznámek a všechny soubory připojené k poznámkám (druhý soubor …_attachments.zip).") },
        confirmButton = { TextButton(onClick = { askFull = false; startBackup(true) }) { Text("Plná záloha") } },
        dismissButton = {
            Row {
                TextButton(onClick = { askFull = false }) { Text("zrušení") }
                TextButton(onClick = { askFull = false; startBackup(false) }) { Text("Lehká záloha") }
            }
        },
    )
    if (askZip) AlertDialog(
        onDismissRequest = { askZip = false },
        title = { Text("Obnovit i přílohy?") },
        text = { Text("Poznámky mají přílohy. Vyberte soubor …_attachments.zip, který patří k této záloze.") },
        confirmButton = { TextButton(onClick = { askZip = false; zipImportLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "*/*")) }) { Text("Vybrat zip") } },
        dismissButton = { TextButton(onClick = { askZip = false }) { Text("Teď ne") } },
    )
    busy?.let { label ->
        AlertDialog(onDismissRequest = {}, confirmButton = {}, text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.CircularProgressIndicator()
                Text(label, Modifier.padding(start = 16.dp))
            }
        })
    }

    @Composable
    fun Toggle(label: String, value: Boolean, hint: String? = null, set: (Boolean) -> Unit) {
        Row(Modifier.fillMaxWidth().clickable { set(!value); tick++ }.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label)
                if (hint != null) Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            Switch(value, { set(it); tick++ })
        }
    }

    @Composable
    fun Choice(label: String, options: List<String>, value: Int, hint: String? = null, set: (Int) -> Unit) {
        var open by remember { mutableStateOf(false) }
        Column(Modifier.fillMaxWidth().clickable { open = true }.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(label)
            Text(options.getOrElse(value) { "" }, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            if (hint != null) Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        if (open) AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(label) },
            text = {
                Column { options.forEachIndexed { i, o ->
                    Text((if (i == value) "✓ " else "") + o, Modifier.fillMaxWidth().clickable { set(i); tick++; open = false }.padding(vertical = 12.dp))
                } }
            },
            confirmButton = { TextButton(onClick = { open = false }) { Text("zrušení") } },
        )
    }

    @Composable
    fun Header(t: String) = Text(t, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp))

    @Composable
    fun Action(label: String, hint: String, onClick: () -> Unit) {
        Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(label)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }

    val refresh = { Repo.changed(context) }
    // Kategorie a volby podle preferences.xml původní appky (jen ty, které tato verze umí).
    SimpleScaffold("Nastavení", onBack) { m ->
        androidx.compose.runtime.key(tick) { Column(m.verticalScroll(rememberScrollState())) {
            Header("Všeobecné")
            Choice("První den v týdnu", listOf("Neděle", "Pondělí"), if (s.firstDayOfWeek == 1) 0 else 1,
                "V aplikaci vyberte první den v týdnu, který upřednostňujete") { s.firstDayOfWeek = if (it == 0) 1 else 2; refresh() }
            HorizontalDivider()
            Header("Vzhled")
            Choice("Téma", listOf("Systémové výchozí", "Světlé", "Tmavé"), s.theme,
                "Světlé, tmavé nebo systémové výchozí téma") { s.theme = it; (context as? Activity)?.recreate() }
            Toggle("Hotové položky dole", s.doneItemsBottom, "Hotové položky seznamu se zobrazí dole") { s.doneItemsBottom = it; refresh() }
            Toggle("Barevné pozadí celé karty", s.colorFullTab,
                "Zapnuto: barva vyplní celé pozadí karty. Vypnuto: obarví se jen název karty") { s.colorFullTab = it; refresh() }
            Toggle("Datum / čas kalendáře", s.showReminderTime, "Zobrazit kalendářní datum a čas poznámky") { s.showReminderTime = it; refresh() }
            Toggle("Vytvořeno datum / čas", s.showCreatedTime, "Zobrazit datum a čas vytvoření poznámky") { s.showCreatedTime = it; refresh() }
            Toggle("Poslední změněné datum / čas", s.showModifiedTime, "Zobrazit datum a čas poslední změny poznámky") { s.showModifiedTime = it; refresh() }
            Toggle("Zpětné seřízení", s.reverseAlignment, "Text zarovnaný zprava doleva") { s.reverseAlignment = it; refresh() }
            HorizontalDivider()
            Header("Karta Kalendář")
            Toggle("Karta Kalendář", s.showCalendarTab, "Zobrazit první kartu kalendáře") { s.showCalendarTab = it; refresh() }
            Toggle("Dnešní datum", s.calendarToday, "Při otevírání vždy vybrat v kalendáři dnešní datum") { s.calendarToday = it }
            Toggle("Text názvu kalendáře", s.calendarTitleText, "Zobrazit na kartě kalendáře text „Kalendář“") { s.calendarTitleText = it; refresh() }
            HorizontalDivider()
            Header("Připomínka")
            Toggle("Připomínka", s.remindersOn, "Vypněte, chcete-li skrýt oznámení z připomenutí") { s.remindersOn = it }
            Toggle("Vibrační signál", s.reminderVibrate, "Vibrace zařízení při připomenutí") { s.reminderVibrate = it }
            HorizontalDivider()
            Header("Servisní funkce")
            Action("Vytvořit zálohu", "Soubor .bak (+ přílohy v …_attachments.zip) do složky záloh – stejný formát jako My Notes") {
                withFolder(SettingsActivity.ACTION_BACKUP)
            }
            Action("Obnovit zálohu", "Vyberte .bak ze složky záloh – přílohy (…_attachments.zip) se načtou automaticky") {
                withFolder(SettingsActivity.ACTION_RESTORE)
            }
            Action("Složka záloh", backupFolder?.name ?: "Nevybráno – klepnutím vyberte (např. Stažené nebo Documents/Notes)") {
                afterFolder = null; folderLauncher.launch(null)
            }
            Action("Obnovit ze souboru…", "Ruční výběr jednoho souboru .bak odkudkoli") { importLauncher.launch(arrayOf("*/*")) }
            Action("Obnovit přílohy", "Načte soubor …_attachments.zip se soubory příloh") {
                zipImportLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "*/*"))
            }
            HorizontalDivider()
            Header("Pokročilé nastavení")
            Toggle("Spustit editor poznámek na widgetu", s.runEditorFromWidget,
                "Zapnuto: klepnutí na widget otevře editor. Vypnuto: úvodní obrazovka s kartami") { s.runEditorFromWidget = it; WidgetUpdater.updateAll(context) }
            Toggle("Potvrzení o vymazání poznámky", s.askBeforeDelete) { s.askBeforeDelete = it }
            Toggle("Backspace – automatické odebrání položky seznamu", s.backspaceRemovesItem,
                "Klávesa Zpět v prázdné položce seznamu ji odebere") { s.backspaceRemovesItem = it }
            Toggle("Zvýrazněte webové odkazy", s.highlightLinks) { s.highlightLinks = it; refresh() }
            Toggle("Zvýrazněte e-mailové adresy", s.highlightEmails) { s.highlightEmails = it; refresh() }
            Toggle("Zvýrazněte telefonní čísla", s.highlightPhones) { s.highlightPhones = it; refresh() }
        } }
    }
}

// ---------- Výběr poznámky pro widget ----------

class SelectNoteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }
        // Při zrušení konfigurace se nový widget nepřidá.
        setResult(Activity.RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        setContent {
            AppTheme {
                SelectNote(onBack = { finish() }) { note ->
                    WidgetPrefs.setNoteId(this, widgetId, note.id)
                    WidgetUpdater.updateAll(this)
                    setResult(Activity.RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
                    finish()
                }
            }
        }
    }

    companion object {
        fun intent(context: Context, widgetId: Int) = Intent(context, SelectNoteActivity::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            .setData(Uri.parse("poznamky://select/$widgetId"))
    }
}

@Composable
private fun SelectNote(onBack: () -> Unit, onPick: (Note) -> Unit) {
    val context = LocalContext.current
    val db = remember { Repo.db(context) }
    val tabs = remember { db.tabs() }
    val settings = remember { Settings(context) }
    SimpleScaffold("Vyberte poznámku pro widget", onBack) { m ->
        Column(m.fillMaxSize()) {
            val notes = remember { tabs.flatMap { t -> db.notesInTab(t.id, settings.sortMode) } }
            NotesList(notes, settings.viewMode, 1, onPick)
        }
    }
}
