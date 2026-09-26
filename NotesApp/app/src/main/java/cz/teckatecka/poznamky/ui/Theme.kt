package cz.teckatecka.poznamky.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
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
    val scheme = when {
        Build.VERSION.SDK_INT >= 31 && dark -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= 31 -> dynamicLightColorScheme(context)
        dark -> darkColorScheme()
        else -> lightColorScheme()
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
