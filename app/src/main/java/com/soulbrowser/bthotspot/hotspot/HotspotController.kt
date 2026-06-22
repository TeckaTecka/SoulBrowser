package com.soulbrowser.bthotspot.hotspot

import android.content.Context
import android.os.Build
import android.util.Log
import com.soulbrowser.bthotspot.service.HotspotAccessibilityService
import java.util.concurrent.Executors

private const val TAG = "HotspotController"

object HotspotController {

    fun enable(context: Context) = toggle(context, true)
    fun disable(context: Context) = toggle(context, false)

    private fun toggle(context: Context, enable: Boolean) {
        Log.d(TAG, "toggle(enable=$enable)")

        // 1. Try TetheringManager — works on rooted devices or system apps
        if (tryTetheringManager(context, enable)) {
            Log.i(TAG, "TetheringManager succeeded")
            return
        }

        // 2. Try root shell command
        if (tryRootShell(enable)) {
            Log.i(TAG, "Root shell command succeeded")
            return
        }

        // 3. Accessibility Service — primary method for non-root devices
        if (HotspotAccessibilityService.toggleHotspot(enable)) {
            Log.i(TAG, "Dispatched to AccessibilityService")
            return
        }

        Log.e(TAG, "All methods failed — is the Accessibility Service enabled?")
    }

    /**
     * Uses the hidden TetheringManager API via reflection.
     * On stock non-rooted devices this will throw SecurityException (TETHER_PRIVILEGED required).
     * On rooted devices or custom ROMs that grant the permission it works reliably.
     */
    private fun tryTetheringManager(context: Context, enable: Boolean): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
        return try {
            val tm = context.getSystemService("tethering") ?: return false
            val tmClass = Class.forName("android.net.TetheringManager")
            if (enable) {
                val requestBuilderClass =
                    Class.forName("android.net.TetheringManager\$TetheringRequest\$Builder")
                val requestClass =
                    Class.forName("android.net.TetheringManager\$TetheringRequest")
                val callbackClass =
                    Class.forName("android.net.TetheringManager\$StartTetheringCallback")

                // TETHERING_WIFI = 0
                val builder = requestBuilderClass.getConstructor(Int::class.java).newInstance(0)
                val request = requestBuilderClass.getMethod("build").invoke(builder)

                tmClass.getMethod(
                    "startTethering",
                    requestClass,
                    java.util.concurrent.Executor::class.java,
                    callbackClass
                ).invoke(
                    tm,
                    request,
                    Executors.newSingleThreadExecutor(),
                    null
                )
            } else {
                // TETHERING_WIFI = 0
                tmClass.getMethod("stopTethering", Int::class.java).invoke(tm, 0)
            }
            true
        } catch (e: SecurityException) {
            Log.d(TAG, "TetheringManager: no TETHER_PRIVILEGED permission")
            false
        } catch (e: Exception) {
            Log.d(TAG, "TetheringManager reflection failed: ${e.javaClass.simpleName}")
            false
        }
    }

    /**
     * Sends a shell command via 'su'. Only works on rooted devices.
     * Android 10+: cmd connectivity tethering start/stop wifi
     */
    private fun tryRootShell(enable: Boolean): Boolean {
        return try {
            val cmd = if (enable) "start" else "stop"
            val proc = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "cmd connectivity tethering $cmd wifi")
            )
            val exit = proc.waitFor()
            if (exit == 0) true
            else {
                // Fallback legacy command
                val proc2 = Runtime.getRuntime().exec(
                    arrayOf("su", "-c", if (enable) "svc wifi hotspot enable" else "svc wifi hotspot disable")
                )
                proc2.waitFor() == 0
            }
        } catch (e: Exception) {
            Log.d(TAG, "Root shell unavailable: ${e.message}")
            false
        }
    }
}
