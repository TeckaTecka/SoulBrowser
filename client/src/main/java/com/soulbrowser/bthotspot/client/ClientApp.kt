package com.soulbrowser.bthotspot.client

import android.app.Application
import android.content.Intent
import android.os.Build

class ClientApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val intent = Intent(this, WifiMonitorService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            // ignore — MainActivity/BootReceiver will start it too
        }
    }
}
