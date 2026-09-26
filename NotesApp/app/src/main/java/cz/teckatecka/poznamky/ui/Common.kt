package cz.teckatecka.poznamky.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import cz.teckatecka.poznamky.R
import cz.teckatecka.poznamky.data.Note
import cz.teckatecka.poznamky.widget.NoteWidgetBlack
import cz.teckatecka.poznamky.widget.NoteWidgetTransparent
import cz.teckatecka.poznamky.widget.NoteWidgetWhite
import cz.teckatecka.poznamky.widget.WidgetPrefs

/** Zástupce poznámky na ploše (jako „Vytvořit zástupce“ v původní appce). */
fun createShortcut(context: Context, n: Note) {
    if (n.id == Note.NEW_ID) return
    if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
        Toast.makeText(context, "Spouštěč nepodporuje zástupce", Toast.LENGTH_SHORT).show()
        return
    }
    val info = ShortcutInfoCompat.Builder(context, "note_${n.id}")
        .setShortLabel(n.title.ifBlank { n.plainText().take(20) }.ifBlank { context.getString(R.string.app_name) })
        .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
        .setIntent(EditorActivity.intent(context, n.id).setAction(Intent.ACTION_VIEW))
        .build()
    ShortcutManagerCompat.requestPinShortcut(context, info, null)
}

/** Všechny widgety „Poznámka“ na ploše. */
fun allNoteWidgets(context: Context): List<Int> {
    val m = AppWidgetManager.getInstance(context)
    return listOf(NoteWidgetWhite::class.java, NoteWidgetBlack::class.java, NoteWidgetTransparent::class.java)
        .flatMap { m.getAppWidgetIds(ComponentName(context, it)).toList() }
}

/** ID widgetů „Poznámka“, které zobrazují danou poznámku. */
fun widgetsShowing(context: Context, noteId: Long): List<Int> =
    if (noteId == Note.NEW_ID) emptyList() else allNoteWidgets(context).filter { WidgetPrefs.noteId(context, it) == noteId }

/** Text s klikacími odkazy (web, e-mail, telefon) podle nastavení – podtržené tyrkysově jako v originále. */
@Composable
fun linkified(text: String): AnnotatedString {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings = androidx.compose.runtime.remember { cz.teckatecka.poznamky.data.Settings(context) }
    val linkColor = MaterialTheme.colorScheme.primary
    val styles = TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline))
    return buildAnnotatedString {
        append(text)
        val taken = mutableListOf<IntRange>()
        fun link(start: Int, end: Int, url: String) {
            if (taken.any { start < it.last + 1 && end > it.first }) return
            taken.add(start until end)
            addLink(LinkAnnotation.Url(url, styles), start, end)
        }
        if (settings.highlightEmails) {
            val e = Patterns.EMAIL_ADDRESS.matcher(text)
            while (e.find()) link(e.start(), e.end(), "mailto:" + e.group())
        }
        if (settings.highlightLinks) {
            val m = Patterns.WEB_URL.matcher(text)
            while (m.find()) {
                val raw = m.group()
                if (!raw.contains('.')) continue
                link(m.start(), m.end(), if (raw.contains("://")) raw else "https://$raw")
            }
        }
        if (settings.highlightPhones) {
            val ph = Patterns.PHONE.matcher(text)
            while (ph.find()) {
                val raw = ph.group()
                if (raw.count { it.isDigit() } >= 9) link(ph.start(), ph.end(), "tel:" + raw.filter { it.isDigit() || it == '+' })
            }
        }
    }
}

/** Kulaté tlačítko se šipkou (32 dp, rámeček) a počítadlo „3 / 10“ – přepínání karet i poznámek. */
@Composable
fun PagerCounter(index: Int, count: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    val c = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircleArrow(Icons.AutoMirrored.Filled.ArrowBack, index > 0, onPrev)
        Text("${if (count == 0) 0 else index + 1} / $count", fontSize = 22.sp, color = c, modifier = Modifier.padding(horizontal = 4.dp))
        CircleArrow(Icons.AutoMirrored.Filled.ArrowForward, index < count - 1, onNext)
    }
}

@Composable
private fun CircleArrow(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.padding(2.dp).size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clickable(enabled = enabled, onClick = onClick).alpha(if (enabled) 0.8f else 0.3f),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurface) }
}

/** Kulaté tlačítko ve spodní liště editoru. */
@Composable
fun RoundButton(
    onClick: () -> Unit,
    background: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    size: Int = 48,
    border: BorderStroke? = null,
    content: @Composable () -> Unit,
) {
    var m = Modifier.size(size.dp).clip(CircleShape).background(background)
    if (border != null) m = m.border(border, CircleShape)
    Box(m.clickable(onClick = onClick), contentAlignment = Alignment.Center) { content() }
}
