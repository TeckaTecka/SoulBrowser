package com.soulbrowser.bthotspot

import android.app.Application
import android.content.Intent
import android.os.Build
import com.soulbrowser.bthotspot.service.ServiceWatchdog
import org.lsposed.hiddenapibypass.HiddenApiBypass

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Unlock reflection on hidden tethering APIs (TetheringManager / ConnectivityManager).
        // This is how "auto hotspot" apps toggle the hotspot programmatically.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("Landroid/net/")
        }

        // Auto-start the monitor service when the app process starts
        try {
            val intent = Intent(this, com.soulbrowser.bthotspot.service.BluetoothMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            // Background FGS start can be blocked on some OEMs — the watchdog retries later.
        }

        // Periodic watchdog — restarts the service and warns if accessibility is disabled.
        try {
            ServiceWatchdog.schedule(this)
        } catch (e: Exception) {
        }
    }
}
