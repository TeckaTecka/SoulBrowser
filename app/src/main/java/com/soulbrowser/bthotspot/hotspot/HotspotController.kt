package com.soulbrowser.bthotspot.hotspot

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import com.soulbrowser.bthotspot.service.HotspotAccessibilityService
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "HotspotController"
private const val TETHERING_WIFI = 0
private const val TETHER_TIMEOUT_S = 6L

/**
 * Toggles the WiFi hotspot the way non-root "auto hotspot" apps do: via the hidden
 * TetheringManager.startTethering/stopTethering API, authorised by the WRITE_SETTINGS
 * ("Modify system settings") permission. Hidden-API restrictions are lifted in
 * App.onCreate via HiddenApiBypass.
 *
 * All work runs on a background executor because startTethering reports its real result
 * asynchronously via a callback — we block briefly on that callback so we actually know
 * whether tethering succeeded (and can fall back if not), instead of assuming success.
 */
object HotspotController {

    private val bg: Executor = Executors.newSingleThreadExecutor()

    fun enable(context: Context) {
        val app = context.applicationContext
        bg.execute {
            val log = StringBuilder()
            val ok = enableBlocking(app, log)
            Log.i(TAG, "enable() -> $ok\n$log")
        }
    }

    fun disable(context: Context) {
        val app = context.applicationContext
        bg.execute {
            val log = StringBuilder()
            val ok = disableBlocking(app, log)
            Log.i(TAG, "disable() -> $ok\n$log")
        }
    }

    fun hasWriteSettings(context: Context): Boolean = Settings.System.canWrite(context)

    /**
     * Diagnostic entry point for the in-app "Test" button. Runs the same enable flow but
     * returns a human-readable report (which method, success/failure + error code, WiFi
     * state) on the main thread so the UI can show it.
     */
    fun test(context: Context, onResult: (String) -> Unit) {
        val app = context.applicationContext
        bg.execute {
            val log = StringBuilder()
            log.append("WRITE_SETTINGS: ${if (hasWriteSettings(app)) "povoleno ✓" else "NENÍ povoleno ✗"}\n")
            log.append("WiFi zapnutá: ${wifiState(app)}\n")
            log.append("Aktivní síť je WiFi: ${if (activeNetworkIsWifi(app)) "ANO (může blokovat hotspot)" else "ne"}\n")
            log.append("——————\n")
            val ok = enableBlocking(app, log)
            log.append("——————\n")
            log.append(if (ok) "VÝSLEDEK: hotspot zapnut ✓" else "VÝSLEDEK: nepodařilo se ✗")
            val text = log.toString()
            Handler(Looper.getMainLooper()).post { onResult(text) }
        }
    }

    // -------- core flow --------

    private fun enableBlocking(context: Context, log: StringBuilder): Boolean {
        if (hasWriteSettings(context)) {
            if (tetherStartBlocking(context, log)) return true
        } else {
            log.append("Tethering API přeskočeno (chybí WRITE_SETTINGS)\n")
        }
        if (tryRootShell(true)) {
            log.append("Root shell: OK\n")
            return true
        }
        if (HotspotAccessibilityService.toggleHotspot(true)) {
            log.append("Accessibility: příkaz odeslán (výsledek nelze změřit)\n")
            return true
        }
        log.append("Accessibility: služba není připojená\n")
        return false
    }

    private fun disableBlocking(context: Context, log: StringBuilder): Boolean {
        if (hasWriteSettings(context) && tetherStop(context, log)) return true
        if (tryRootShell(false)) {
            log.append("Root shell: OK\n")
            return true
        }
        if (HotspotAccessibilityService.toggleHotspot(false)) {
            log.append("Accessibility: příkaz odeslán\n")
            return true
        }
        return false
    }

    // -------- TetheringManager (API 30+) --------

    private fun tetherStartBlocking(context: Context, log: StringBuilder): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            log.append("Tethering API: vyžaduje Android 11+\n")
            return false
        }
        return try {
            val tm = context.getSystemService("tethering")
            if (tm == null) {
                log.append("Tethering API: služba není dostupná\n")
                return false
            }
            val requestClass = Class.forName("android.net.TetheringManager\$TetheringRequest")
            val builderClass = Class.forName("android.net.TetheringManager\$TetheringRequest\$Builder")
            val callbackClass = Class.forName("android.net.TetheringManager\$StartTetheringCallback")

            val builder = builderClass.getConstructor(Int::class.javaPrimitiveType).newInstance(TETHERING_WIFI)
            val request = builderClass.getMethod("build").invoke(builder)

            val latch = CountDownLatch(1)
            val result = AtomicReference("časový limit vypršel (bez odpovědi)")
            val callback = Proxy.newProxyInstance(
                callbackClass.classLoader,
                arrayOf(callbackClass),
                InvocationHandler { proxy, method, args ->
                    when (method.name) {
                        "onTetheringStarted" -> { result.set("OK"); latch.countDown(); null }
                        "onTetheringFailed" -> {
                            result.set("CHYBA (kód ${args?.getOrNull(0)})")
                            latch.countDown(); null
                        }
                        "hashCode" -> System.identityHashCode(proxy)
                        "equals" -> proxy === args?.getOrNull(0)
                        "toString" -> "StartTetheringCallbackProxy"
                        else -> null
                    }
                }
            )

            tm.javaClass.getMethod(
                "startTethering",
                requestClass,
                Executor::class.java,
                callbackClass
            ).invoke(tm, request, bg, callback)

            latch.await(TETHER_TIMEOUT_S, TimeUnit.SECONDS)
            val r = result.get()
            log.append("Tethering API výsledek: $r\n")
            r == "OK"
        } catch (e: Exception) {
            log.append("Tethering API výjimka: ${rootCause(e)}\n")
            false
        }
    }

    private fun tetherStop(context: Context, log: StringBuilder): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
        return try {
            val tm = context.getSystemService("tethering") ?: return false
            tm.javaClass.getMethod("stopTethering", Int::class.javaPrimitiveType)
                .invoke(tm, TETHERING_WIFI)
            log.append("Tethering API: stopTethering odesláno\n")
            true
        } catch (e: Exception) {
            log.append("Tethering API stop výjimka: ${rootCause(e)}\n")
            false
        }
    }

    // -------- root fallback --------

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
            false
        }
    }

    // -------- diagnostics helpers --------

    private fun wifiState(context: Context): String {
        return try {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (wm.isWifiEnabled) "ano" else "ne"
        } catch (e: Exception) {
            "neznámé"
        }
    }

    private fun activeNetworkIsWifi(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(ConnectivityManager::class.java)
            val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } catch (e: Exception) {
            false
        }
    }

    private fun rootCause(e: Throwable): String {
        var t = e
        while (t.cause != null && t.cause !== t) t = t.cause!!
        return "${t.javaClass.simpleName}: ${t.message}"
    }
}
