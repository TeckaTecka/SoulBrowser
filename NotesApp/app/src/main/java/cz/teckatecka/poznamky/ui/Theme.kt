package cz.teckatecka.poznamky.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import cz.teckatecka.poznamky.data.Settings
import cz.teckatecka.poznamky.data.contrastTextColor

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dark = when (Settings(context).theme) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    // Barvy podle původní appky: tmavě šedé plochy, tyrkysové odkazy a tlačítka.
    val scheme = if (dark) {
        darkColorScheme(
            primary = Color(0xFF80CBC4), onPrimary = Color(0xFF00201D),
            background = Color(0xFF121212), onBackground = Color(0xFFE6E6E6),
            surface = Color(0xFF1B1B1B), onSurface = Color(0xFFE6E6E6),
            surfaceVariant = Color(0xFF2A2A2A), onSurfaceVariant = Color(0xFFE0E0E0),
            surfaceContainer = Color(0xFF232323), surfaceContainerHigh = Color(0xFF2C2C2C),
            outline = Color(0xFF8A8A8A),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF00897B), onPrimary = Color.White,
            background = Color(0xFFEFEFEF), onBackground = Color(0xFF202124),
            surface = Color(0xFFF7F7F7), onSurface = Color(0xFF202124),
            surfaceVariant = Color(0xFFFFFFFF), onSurfaceVariant = Color(0xFF202124),
            surfaceContainer = Color(0xFFF2F2F2), surfaceContainerHigh = Color(0xFFFFFFFF),
            outline = Color(0xFF8A8A8A),
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

/** Barva poznámky (0 = barva karty z tématu). */
@Composable
fun noteBg(color: Int): Color = if (color == 0) MaterialTheme.colorScheme.surfaceVariant else Color(color)

@Composable
fun noteFg(color: Int, fontColor: Int = 0): Color = when {
    fontColor != 0 -> Color(fontColor)
    color == 0 -> MaterialTheme.colorScheme.onSurfaceVariant
    else -> Color(contrastTextColor(color))
}
