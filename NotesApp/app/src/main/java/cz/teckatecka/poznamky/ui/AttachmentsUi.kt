package cz.teckatecka.poznamky.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.teckatecka.poznamky.data.Attachments
import cz.teckatecka.poznamky.data.NoteAttachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Zmenšený náhled obrázku (načítá se na pozadí). */
@Composable
fun rememberThumbnail(name: String, sizePx: Int = 256): ImageBitmap? {
    val context = LocalContext.current
    val state by produceState<ImageBitmap?>(null, name) {
        value = withContext(Dispatchers.IO) { loadThumbnail(context, name, sizePx)?.asImageBitmap() }
    }
    return state
}

private fun loadThumbnail(context: Context, name: String, sizePx: Int): Bitmap? = runCatching {
    val f = Attachments.file(context, name)
    if (!f.exists() || !Attachments.isImage(name)) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(f.path, bounds)
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= sizePx && bounds.outHeight / (sample * 2) >= sizePx) sample *= 2
    BitmapFactory.decodeFile(f.path, BitmapFactory.Options().apply { inSampleSize = sample })
}.getOrNull()

/** Ikona / náhled jedné přílohy. */
@Composable
fun AttachmentThumb(a: NoteAttachment, size: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier.size(size.dp).clip(shape).background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
        contentAlignment = Alignment.Center,
    ) {
        val exists = remember(a.name) { Attachments.exists(context, a.name) }
        val thumb = if (Attachments.isImage(a.name)) rememberThumbnail(a.name) else null
        when {
            thumb != null -> Image(thumb, a.title, contentScale = ContentScale.Crop, modifier = Modifier.size(size.dp))
            !exists -> Icon(Icons.Default.BrokenImage, "Soubor chybí", tint = MaterialTheme.colorScheme.outline)
            Attachments.isAudio(a.name) -> Icon(Icons.Default.AudioFile, null)
            Attachments.isVideo(a.name) -> Icon(Icons.Default.VideoFile, null)
            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.InsertDriveFile, null)
                Text(Attachments.extension(a.name).uppercase(), fontSize = 10.sp)
            }
        }
    }
}

fun openAttachment(context: Context, a: NoteAttachment) {
    if (!Attachments.exists(context, a.name)) {
        Toast.makeText(context, "Soubor přílohy chybí – obnovte zálohu příloh (…_attachments.zip).", Toast.LENGTH_LONG).show()
        return
    }
    runCatching { context.startActivity(Attachments.openIntent(context, a)) }
        .onFailure { Toast.makeText(context, "Žádná aplikace neumí tento soubor otevřít", Toast.LENGTH_SHORT).show() }
}

/** Pás příloh v editoru (attachments_frame_view v originále). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AttachmentStrip(list: List<NoteAttachment>, readOnly: Boolean, onChange: (List<NoteAttachment>) -> Unit) {
    val context = LocalContext.current
    var menuFor by remember { mutableStateOf<NoteAttachment?>(null) }
    var editFor by remember { mutableStateOf<NoteAttachment?>(null) }
    var deleteFor by remember { mutableStateOf<NoteAttachment?>(null) }
    LazyRow(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(list, key = { it.name }) { a ->
            Box {
                Column(Modifier.width(72.dp).combinedClickable(onClick = { openAttachment(context, a) }, onLongClick = { menuFor = a }),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    AttachmentThumb(a, 64)
                    if (a.title.isNotBlank()) Text(a.title, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                DropdownMenu(menuFor == a, { menuFor = null }) {
                    DropdownMenuItem(text = { Text("Otevřít") }, leadingIcon = { Icon(Icons.Default.OpenInNew, null) },
                        onClick = { menuFor = null; openAttachment(context, a) })
                    DropdownMenuItem(text = { Text("Sdílet") }, leadingIcon = { Icon(Icons.Default.Share, null) }, onClick = {
                        menuFor = null
                        if (Attachments.exists(context, a.name)) context.startActivity(Attachments.shareIntent(context, a))
                    })
                    if (!readOnly) {
                        DropdownMenuItem(text = { Text("Popis") }, leadingIcon = { Icon(Icons.Default.Edit, null) },
                            onClick = { menuFor = null; editFor = a })
                        DropdownMenuItem(text = { Text("Odstranit") }, leadingIcon = { Icon(Icons.Default.Delete, null) },
                            onClick = { menuFor = null; deleteFor = a })
                    }
                }
            }
        }
    }
    editFor?.let { a ->
        var title by remember(a) { mutableStateOf(a.title) }
        AlertDialog(
            onDismissRequest = { editFor = null },
            title = { Text("Popis") },
            text = { OutlinedTextField(title, { title = it }) },
            confirmButton = { TextButton(onClick = { onChange(list.map { if (it == a) it.copy(title = title) else it }); editFor = null }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { editFor = null }) { Text("zrušení") } },
        )
    }
    deleteFor?.let { a ->
        ConfirmDialog("Odstranit přílohu?", { deleteFor = null }) {
            onChange(list.filterNot { it == a }); Attachments.delete(context, a.name); deleteFor = null
        }
    }
}

/** Malý náhled první přílohy s počtem – na kartičce poznámky. */
@Composable
fun CardAttachmentBadge(list: List<NoteAttachment>) {
    if (list.isEmpty()) return
    val first = list.firstOrNull { Attachments.isImage(it.name) } ?: list.first()
    Box(Modifier.size(40.dp)) {
        AttachmentThumb(first, 40)
        if (list.size > 1) Text(
            "${list.size}", fontSize = 11.sp, color = Color.White,
            modifier = Modifier.align(Alignment.TopEnd).clip(CircleShape).background(Color(0xCC000000)).padding(horizontal = 4.dp),
        )
    }
}
