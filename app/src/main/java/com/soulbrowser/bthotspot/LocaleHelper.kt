package com.soulbrowser.bthotspot

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Applies the user-selected UI language (Czech / English / Russian) on top of any Context.
 *
 * The choice is stored in a small synchronous SharedPreferences file so it can be read from
 * [android.content.ContextWrapper.attachBaseContext] before the app's DataStore is available.
 *
 * - "system" (default): follow the device language. If the device language isn't one of the
 *   supported ones, Android falls back to the default resources, which are English.
 * - "cs" / "en" / "ru": force that language regardless of the device setting.
 */
object LocaleHelper {

    const val LANG_SYSTEM = "system"
    const val LANG_CS = "cs"
    const val LANG_EN = "en"
    const val LANG_RU = "ru"

    /** Languages we ship translations for; anything else falls back to English. */
    val SUPPORTED = listOf(LANG_SYSTEM, LANG_CS, LANG_EN, LANG_RU)

    private const val PREFS = "ui_prefs"
    private const val KEY_LANG = "ui_lang"

    fun getLanguage(context: Context): String {
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANG, LANG_SYSTEM) ?: LANG_SYSTEM
        return if (stored in SUPPORTED) stored else LANG_SYSTEM
    }

    fun setLanguage(context: Context, lang: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, if (lang in SUPPORTED) lang else LANG_SYSTEM)
            .apply()
    }

    /**
     * Returns a Context whose resources use the selected language. For "system" the original
     * context is returned unchanged so the device language (or the English fallback) applies.
     */
    fun wrap(context: Context): Context {
        val lang = getLanguage(context)
        if (lang == LANG_SYSTEM) return context

        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}
