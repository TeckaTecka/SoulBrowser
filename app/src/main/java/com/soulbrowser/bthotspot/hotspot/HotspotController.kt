package com.soulbrowser.bthotspot.hotspot

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.soulbrowser.bthotspot.service.HotspotAccessibilityService
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private const val TAG = "HotspotController"
private const val TETHERING_WIFI = 0

/**
 * Toggles the WiFi hotspot the same way "auto hotspot" apps from Play Store do:
 *
 * 1. TetheringManager.startTethering/stopTethering via reflection (Android 11+).
 *    TetheringService accepts callers holding TETHER_PRIVILEGED **or** WRITE_SETTINGS
 *    ("Modify system settings"), which the user grants once in the app.
 *    Hidden-API restrictions are lifted in App.onCreate via HiddenApiBypass.
 * 2. ConnectivityManager.startTethering/stopTethering via reflection (Android 10).
 * 3. Root shell command (rooted devices).
 * 4. Accessibility service clicking the QS tile (last-resort fallback).
 */
object HotspotController {

    private val executor: Executor = Executors.newSingleThreadExecutor()

    fun enable(context: Context) = toggle(context, true)
    fun disable(context: Context) = toggle(context, false)

    /** True when the user has granted "Modify system settings". */
    fun hasWriteSettings(context: Context): Boolean = Settings.System.canWrite(context)

    private fun toggle(context: Context, enable: Boolean) {
        Log.d(TAG, "toggle(enable=$enable)")

        if (!hasWriteSettings(context)) {
            Log.w(TAG, "WRITE_SETTINGS not granted — tethering API will be rejected")
        } else {
            if (tryTetheringManager(context, enable)) {
                Log.i(TAG, "TetheringManager OK (enable=$enable)")
                return
            }
            if (tryConnectivityManager(context, enable)) {
                Log.i(TAG, "ConnectivityManager OK (enable=$enable)")
                return
            }
        }

        if (tryRootShell(enable)) {
            Log.i(TAG, "Root shell OK (enable=$enable)")
            return
        }

        if (HotspotAccessibilityService.toggleHotspot(enable)) {
            Log.i(TAG, "Dispatched to AccessibilityService fallback")
            return
        }

        Log.e(TAG, "All methods failed — grant \"Modify system settings\" or enable the Accessibility Service")
    }

    /**
     * Android 11+ (API 30+): TetheringManager, obtained via Context.getSystemService("tethering").
     * startTethering(TetheringRequest, Executor, StartTetheringCallback) — the callback must be
     * non-null (requireNonNull inside), so we build one with a dynamic Proxy.
     */
    private fun tryTetheringManager(context: Context, enable: Boolean): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
        return try {
            val tm = context.getSystemService("tethering") ?: return false
            val tmClass = tm.javaClass

            if (enable) {
                val requestClass = Class.forName("android.net.TetheringManager\$TetheringRequest")
                val builderClass = Class.forName("android.net.TetheringManager\$TetheringRequest\$Builder")
                val callbackClass = Class.forName("android.net.TetheringManager\$StartTetheringCallback")

                val builder = builderClass.getConstructor(Int::class.javaPrimitiveType).newInstance(TETHERING_WIFI)
                val request = builderClass.getMethod("build").invoke(builder)

                val callback = Proxy.newProxyInstance(
                    callbackClass.classLoader,
                    arrayOf(callbackClass),
                    InvocationHandler { _, method, args ->
                        when (method.name) {
                            "onTetheringStarted" -> Log.i(TAG, "onTetheringStarted")
                            "onTetheringFailed" -> Log.w(TAG, "onTetheringFailed error=${args?.getOrNull(0)}")
                            "hashCode" -> return@InvocationHandler System.identityHashCode(this)
                            "equals" -> return@InvocationHandler false
                            "toString" -> return@InvocationHandler "StartTetheringCallbackProxy"
                        }
                        null
                    }
                )

                tmClass.getMethod(
                    "startTethering",
                    requestClass,
                    Executor::class.java,
                    callbackClass
                ).invoke(tm, request, executor, callback)
            } else {
                tmClass.getMethod("stopTethering", Int::class.javaPrimitiveType)
                    .invoke(tm, TETHERING_WIFI)
            }
            true
        } catch (e: Exception) {
            Log.w(TAG, "TetheringManager failed: ${rootCause(e)}")
            false
        }
    }

    /**
     * Android 10 (API 29) fallback: ConnectivityManager.startTethering(int, boolean,
     * OnStartTetheringCallback, Handler). The callback is a hidden abstract class that
     * cannot be instantiated via reflection, so enabling only works on API 30+ via
     * [tryTetheringManager]. On API 29 this returns false and we fall through to the
     * accessibility tile method. stopTethering(int) has no callback and works on API 29.
     */
    private fun tryConnectivityManager(context: Context, enable: Boolean): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return false
        if (enable) return false
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            cm.javaClass.getMethod("stopTethering", Int::class.javaPrimitiveType)
                .invoke(cm, TETHERING_WIFI)
            true
        } catch (e: Exception) {
            Log.w(TAG, "ConnectivityManager failed: ${rootCause(e)}")
            false
        }
    }

    private fun tryRootShell(enable: Boolean): Boolean {
        return try {
            val cmd = if (enable) "start" else "stop"
            val proc = Runtime.getRuntime().exec(
                arrayOf("su", "-c", "cmd connectivity tethering $cmd wifi")
            )
            if (proc.waitFor() == 0) true
            else {
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

    private fun rootCause(e: Throwable): String {
        var t = e
        while (t.cause != null && t.cause !== t) t = t.cause!!
        return "${t.javaClass.simpleName}: ${t.message}"
    }
}
