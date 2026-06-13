package com.teckatecka.netadmin.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {

    private const val PREF_NAME  = "app_prefs"
    private const val KEY_NIGHT  = "night_mode"

    fun applyFromPrefs(context: Context) {
        val mode = context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_NIGHT, AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun setNightMode(context: Context, enabled: Boolean) {
        val mode = if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_NIGHT, mode).apply()
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun isNightMode(context: Context): Boolean {
        val saved = context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_NIGHT, AppCompatDelegate.MODE_NIGHT_NO)
        return saved == AppCompatDelegate.MODE_NIGHT_YES
    }
}
