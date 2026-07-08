package com.soulbrowser.bthotspot.hotspot

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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

    // Separate executor for the startTethering callback. It MUST NOT be the same
    // thread that blocks on the latch below, otherwise the callback is queued behind
    // the blocked task and can never run (self-deadlock → timeout).
    private val callbackExecutor: Executor = Executors.newCachedThreadPool()

    fun enable(context: Context) = setState(context, true, null)
    fun disable(context: Context) = setState(context, false, null)

    fun hasWriteSettings(context: Context): Boolean = Settings.System.canWrite(context)

    /**
     * Drives the hotspot to [target] and verifies the *actual* SoftAP state afterwards.
     * [onResult] (on the main thread) reports whether the real state reached the target —
     * so callers only fire "hotspot on/off" feedback when it genuinely happened.
     */
    fun setState(context: Context, target: Boolean, onResult: ((Boolean) -> Unit)?) {
        val app = context.applicationContext
        bg.execute {
            val log = StringBuilder()
            ensureState(app, target, log) { ok ->
                Log.i(TAG, "setState($target) -> $ok\n$log")
                onResult?.let { cb -> Handler(Looper.getMainLooper()).post { cb(ok) } }
            }
        }
    }

    /**
     * Diagnostic entry point for the in-app "Test" button. Reports the real hotspot state
     * before and after, plus which method was used.
     */
    fun test(context: Context, onResult: (String) -> Unit) {
        val app = context.applicationContext
        bg.execute {
            val log = StringBuilder()
            log.append("WRITE_SETTINGS: ${if (hasWriteSettings(app)) "povoleno ✓" else "NENÍ povoleno ✗"}\n")
            log.append("Aktivní síť je WiFi: ${if (activeNetworkIsWifi(app)) "ANO (může blokovat hotspot)" else "ne"}\n")
            log.append("Skutečný stav hotspotu: ${stateStr(HotspotState.isOn(app))}\n")
            log.append("——————\n")
            ensureState(app, true, log) { ok ->
                log.append("——————\n")
                log.append("Skutečný stav po akci: ${stateStr(HotspotState.isOn(app))}\n")
                log.append(if (ok) "VÝSLEDEK: hotspot zapnut ✓" else "VÝSLEDEK: nepodařilo se ✗")
                Handler(Looper.getMainLooper()).post { onResult(log.toString()) }
            }
        }
    }

    /**
     * Runs 3 on/off cycles (finishing OFF), verifying the real state at each step.
     * Useful to check reliability of enabling/disabling in one go.
     */
    fun testCycle(context: Context, onResult: (String) -> Unit) {
        val app = context.applicationContext
        bg.execute {
            val log = StringBuilder("Test 3× zapnout / vypnout\n")
            val scratch = StringBuilder()
            for (i in 1..3) {
                val on = setStateBlocking(app, true, scratch)
                log.append("Cyklus $i — zapnout: ${mark(on)} (${stateStr(HotspotState.isOn(app))})\n")
                Thread.sleep(1500)
                val off = setStateBlocking(app, false, scratch)
                log.append("Cyklus $i — vypnout: ${mark(off)} (${stateStr(HotspotState.isOn(app))})\n")
                Thread.sleep(1500)
            }
            // Guarantee we end in the OFF state.
            setStateBlocking(app, false, scratch)
            log.append("——————\n")
            log.append("Ukončeno ve stavu: ${stateStr(HotspotState.isOn(app))}")
            Handler(Looper.getMainLooper()).post { onResult(log.toString()) }
        }
    }

    /** Blocking variant used by testCycle — waits for the (possibly async) result. */
    private fun setStateBlocking(context: Context, target: Boolean, log: StringBuilder): Boolean {
        val latch = CountDownLatch(1)
        val result = AtomicReference(false)
        ensureState(context, target, log) { ok -> result.set(ok); latch.countDown() }
        latch.await(30, TimeUnit.SECONDS)
        return result.get()
    }

    private fun mark(b: Boolean): String = if (b) "✓" else "✗"

    // -------- core flow (runs on bg thread; deliver may be called later by a11y callback) --------

    private fun ensureState(context: Context, target: Boolean, log: StringBuilder, deliver: (Boolean) -> Unit) {
        val cur = HotspotState.isOn(context)
        log.append("Stav před akcí: ${stateStr(cur)}\n")
        if (cur == target) {
            log.append("Už ve správném stavu — nic nedělám\n")
            deliver(true)
            return
        }

        // 1. TetheringManager (silent) — verify the real state afterwards.
        if (hasWriteSettings(context)) {
            if (tetherToState(context, target, log) && waitForState(context, target, 5_000, log)) {
                deliver(true)
                return
            }
        } else {
            log.append("Tethering přeskočeno (chybí WRITE_SETTINGS)\n")
        }

        // 2. Root shell.
        if (tryRootShell(target) && waitForState(context, target, 4_000, log)) {
            log.append("Root shell: OK\n")
            deliver(true)
            return
        }

        // 3. Accessibility tile click — state-aware and self-verifying (async).
        val dispatched = HotspotAccessibilityService.setHotspot(target) { success ->
            log.append("Accessibility výsledek: ${if (success) "OK" else "nepotvrzeno"}\n")
            deliver(success)
        }
        if (!dispatched) {
            log.append("Accessibility: služba není připojená\n")
            deliver(false)
        }
    }

    private fun tetherToState(context: Context, target: Boolean, log: StringBuilder): Boolean =
        if (target) tetherStartBlocking(context, log) else tetherStop(context, log)

    /** Polls the real SoftAP state until it matches [target] or the timeout elapses. */
    private fun waitForState(context: Context, target: Boolean, timeoutMs: Long, log: StringBuilder): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (HotspotState.isOn(context) == target) {
                log.append("Stav potvrzen: ${stateStr(target)}\n")
                return true
            }
            try { Thread.sleep(300) } catch (e: InterruptedException) { return false }
        }
        log.append("Stav se do limitu nepotvrdil\n")
        return false
    }

    private fun stateStr(s: Boolean?): String = when (s) {
        true -> "zapnuto"
        false -> "vypnuto"
        null -> "neznámý/přechod"
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
            ).invoke(tm, request, callbackExecutor, callback)

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
