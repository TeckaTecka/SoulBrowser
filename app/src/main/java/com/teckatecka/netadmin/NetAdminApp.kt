package com.teckatecka.netadmin

import android.app.Application
import com.teckatecka.netadmin.utils.ThemeManager

class NetAdminApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        ThemeManager.applyFromPrefs(this)
    }

    companion object {
        lateinit var instance: NetAdminApp
            private set
    }
}
