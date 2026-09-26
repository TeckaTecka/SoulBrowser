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

    fun fileName(): String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".bak"
}

@Composable
private fun SettingsScreen(startAction: String?, onBack: () -> Unit) {
    val context = LocalContext.current
    val s = remember { Settings(context) }
    var tick by remember { mutableStateOf(0) }
    var pendingImport by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) runCatching { Backup.export(context, uri) }
            .onSuccess { Toast.makeText(context, "Záloha uložena", Toast.LENGTH_SHORT).show() }
            .onFailure { Toast.makeText(context, "Záloha selhala: ${it.message}", Toast.LENGTH_LONG).show() }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> pendingImport = uri }
    // Spuštěno z bočního menu „Servisní funkce“.
    var launched by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (launched) return@LaunchedEffect
        launched = true
        when (startAction) {
            SettingsActivity.ACTION_BACKUP -> exportLauncher.launch(Backup.fileName())
            SettingsActivity.ACTION_RESTORE -> importLauncher.launch(arrayOf("*/*"))
        }
    }

    pendingImport?.let { uri ->
        ConfirmDialog("Obnovit ze zálohy? Současné poznámky budou nahrazeny obsahem zálohy.", { pendingImport = null }) {
            pendingImport = null
            runCatching { Backup.import(context, uri) }
                .onSuccess { Toast.makeText(context, "Obnoveno: ${Repo.db(context).allActiveNotes().size} poznámek", Toast.LENGTH_LONG).show() }
                .onFailure { Toast.makeText(context, "Soubor se nepodařilo načíst: ${it.message}", Toast.LENGTH_LONG).show() }
        }
    }

    @Composable
    fun Toggle(label: String, value: Boolean, set: (Boolean) -> Unit) {
        Row(Modifier.fillMaxWidth().clickable { set(!value); tick++ }.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, Modifier.weight(1f))
            Switch(value, { set(it); tick++ })
        }
    }

    @Composable
    fun Choice(label: String, options: List<String>, value: Int, set: (Int) -> Unit) {
        var open by remember { mutableStateOf(false) }
        Column(Modifier.fillMaxWidth().clickable { open = true }.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(label)
            Text(options.getOrElse(value) { "" }, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        }
        if (open) AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(label) },
            text = {
                Column { options.forEachIndexed { i, o ->
                    Text((if (i == value) "✓ " else "") + o, Modifier.fillMaxWidth().clickable { set(i); tick++; open = false }.padding(vertical = 12.dp))
                } }
            },
            confirmButton = { TextButton(onClick = { open = false }) { Text("Zavřít") } },
        )
    }

    @Composable
    fun Header(t: String) = Text(t, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp))

    SimpleScaffold("Nastavení", onBack) { m ->
        androidx.compose.runtime.key(tick) { Column(m.verticalScroll(rememberScrollState())) {
            Header("Zálohy")
            Column(Modifier.fillMaxWidth().clickable { exportLauncher.launch(Backup.fileName()) }.padding(16.dp)) {
                Text("Zálohovat do souboru")
                Text("Soubor .bak – kompatibilní s původní aplikací My Notes", style = MaterialTheme.typography.bodySmall)
            }
            Column(Modifier.fillMaxWidth().clickable { importLauncher.launch(arrayOf("*/*")) }.padding(16.dp)) {
                Text("Obnovit ze zálohy")
                Text("Načte .bak z této aplikace i z My Notes (Pro)", style = MaterialTheme.typography.bodySmall)
            }
            HorizontalDivider()
            Header("Vzhled")
            Choice("Motiv", listOf("Podle systému", "Světlý", "Tmavý"), s.theme) { s.theme = it; (context as? Activity)?.recreate() }
            Choice("Náhled poznámek v seznamu", listOf("Celý obsah", "Krátký náhled", "Jen název"), s.contentMode) {
                s.contentMode = it; Repo.changed(context)
            }
            Toggle("Karta Kalendář", s.showCalendarTab) { s.showCalendarTab = it; Repo.changed(context) }
            HorizontalDivider()
            Header("Poznámky")
            Toggle("Hotové položky seznamu přesouvat dolů", s.doneItemsBottom) { s.doneItemsBottom = it; Repo.changed(context) }
            Toggle("Potvrzovat mazání", s.askBeforeDelete) { s.askBeforeDelete = it }
            HorizontalDivider()
            Header("Widgety")
            Toggle("Zobrazovat čas připomínky", s.widgetShowReminder) { s.widgetShowReminder = it; WidgetUpdater.updateAll(context) }
            Toggle("Zobrazovat datum vytvoření", s.widgetShowCreated) { s.widgetShowCreated = it; WidgetUpdater.updateAll(context) }
            Toggle("Zobrazovat datum úpravy", s.widgetShowModified) { s.widgetShowModified = it; WidgetUpdater.updateAll(context) }
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
