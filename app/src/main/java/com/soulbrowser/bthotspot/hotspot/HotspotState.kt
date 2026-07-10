package com.soulbrowser.bthotspot.hotspot

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log

private const val TAG = "HotspotState"

// WifiManager.WIFI_AP_STATE_* (hidden constants)
private const val AP_STATE_DISABLING = 10
private const val AP_STATE_DISABLED = 11
private const val AP_STATE_ENABLING = 12
private const val AP_STATE_ENABLED = 13

/**
 * Reads the *actual* WiFi hotspot (SoftAP) state via the hidden
 * WifiManager.getWifiApState() / isWifiApEnabled() APIs. Reflection is allowed because
 * App.onCreate lifts hidden-API restrictions for "Landroid/net/".
 */
object HotspotState {

    /**
     * @return true = on, false = off, null = unknown or mid-transition (enabling/disabling)
     * so callers can wait instead of toggling blindly.
     */
    fun isOn(context: Context): Boolean? {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            ?: return null
        // Prefer getWifiApState — it distinguishes the transitional states.
        try {
            val state = wm.javaClass.getMethod("getWifiApState").invoke(wm) as Int
            return when (state) {
                AP_STATE_ENABLED -> true
                AP_STATE_DISABLED -> false
                AP_STATE_ENABLING, AP_STATE_DISABLING -> null
                else -> null
            }
        } catch (e: Exception) {
            Log.d(TAG, "getWifiApState failed: ${e.message}")
        }
        // Fallback: isWifiApEnabled (boolean, no transitional info).
        return try {
            wm.javaClass.getMethod("isWifiApEnabled").invoke(wm) as Boolean
        } catch (e: Exception) {
            Log.d(TAG, "isWifiApEnabled failed: ${e.message}")
            null
        }
    }
}
