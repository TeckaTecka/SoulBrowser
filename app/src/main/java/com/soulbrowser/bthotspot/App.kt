package com.soulbrowser.bthotspot

import android.app.Application
import android.content.Intent
import android.os.Build

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Auto-start the monitor service when the app process starts
        val intent = Intent(this, com.soulbrowser.bthotspot.service.BluetoothMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
