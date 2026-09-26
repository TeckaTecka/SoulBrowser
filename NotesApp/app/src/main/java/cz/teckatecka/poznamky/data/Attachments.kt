package cz.teckatecka.poznamky.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Soubory příloh v úložišti aplikace + import/export zipu kompatibilního s My Notes. */
object Attachments {
    fun dir(context: Context) = File(context.filesDir, "attachments").apply { mkdirs() }
    fun file(context: Context, name: String) = File(dir(context), File(name).name)
    fun exists(context: Context, name: String) = file(context, name).exists()

    fun extension(name: String) = name.substringAfterLast('.', "").lowercase()
    fun mime(name: String): String =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension(name)) ?: "application/octet-stream"
    fun isImage(name: String) = mime(name).startsWith("image/")
    fun isAudio(name: String) = mime(name).startsWith("audio/")
    fun isVideo(name: String) = mime(name).startsWith("video/")

    /** Rozbalí …_attachments.zip z původní appky (soubory v kořeni zipu). Vrací počet souborů. */
    fun importZip(context: Context, uri: Uri): Int {
        var count = 0
        context.contentResolver.openInputStream(uri)?.use { raw ->
            ZipInputStream(raw.buffered()).use { zip ->
                while (true) {
                    val e = zip.nextEntry ?: break
                    if (!e.isDirectory) {
                        // Jen název souboru – ochrana proti cestám typu ../ v zipu.
                        val target = file(context, e.name)
                        target.outputStream().use { zip.copyTo(it) }
                        count++
                    }
                    zip.closeEntry()
                }
            }
        }
        return count
    }

    /** Zabalí všechny přílohy do zipu ve stejném tvaru, jaký čte původní appka. */
    fun exportZip(context: Context, uri: Uri): Int {
        val files = dir(context).listFiles()?.filter { it.isFile } ?: emptyList()
        context.contentResolver.openOutputStream(uri)?.use { out ->
            ZipOutputStream(out.buffered()).use { zip ->
                files.forEach { f ->
                    zip.putNextEntry(ZipEntry(f.name).apply { time = f.lastModified() })
                    f.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        }
        return files.size
    }

    /** Zkopíruje vybraný soubor do příloh; název souboru je jedinečný, popis = původní název. */
    fun addFromUri(context: Context, uri: Uri): NoteAttachment? {
        val display = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { c -> if (c.moveToFirst()) c.getString(0) else null } ?: "soubor"
        var ext = extension(display)
        if (ext.isEmpty()) ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.contentResolver.getType(uri)) ?: "bin"
        val name = "att_${System.currentTimeMillis()}_${(1000..9999).random()}.$ext"
        val target = file(context, name)
        val ok = context.contentResolver.openInputStream(uri)?.use { input -> target.outputStream().use { input.copyTo(it) }; true } ?: false
        return if (ok) NoteAttachment(name, display.substringBeforeLast('.')) else null
    }

    fun delete(context: Context, name: String) {
        // Soubor mažeme jen pokud na něj neodkazuje žádná jiná poznámka.
        val used = NotesDb.get(context).allNotesIncludingDeleted().count { n -> n.attachments.any { it.name == name } }
        if (used <= 1) file(context, name).delete()
    }

    fun uri(context: Context, name: String): Uri =
        FileProvider.getUriForFile(context, context.packageName + ".files", file(context, name))

    fun openIntent(context: Context, a: NoteAttachment): Intent =
        Intent(Intent.ACTION_VIEW).setDataAndType(uri(context, a.name), mime(a.name))
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)

    fun shareIntent(context: Context, a: NoteAttachment): Intent = Intent.createChooser(
        Intent(Intent.ACTION_SEND).setType(mime(a.name)).putExtra(Intent.EXTRA_STREAM, uri(context, a.name))
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
        "Sdílet",
    )
}
