package com.soulbrowser.bthotspot.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.lang.ref.WeakReference

private const val TAG = "HotspotA11y"
private const val MAX_ATTEMPTS = 6
private const val WAKE_DELAY_MS = 600L      // let screen turn on fully before opening QS
private const val SEARCH_DELAY_MS = 800L    // let QS panel load after opening
private const val RETRY_DELAY_MS = 700L
private const val CLOSE_DELAY_MS = 800L
private const val WAKELOCK_TIMEOUT_MS = 25_000L

/**
 * Accessibility service that controls the WiFi hotspot by tapping the Quick Settings tile.
 *
 * Flow:
 *  1. Caller invokes [toggleHotspot] from any thread.
 *  2. Service opens the Quick Settings panel via GLOBAL_ACTION_QUICK_SETTINGS.
 *  3. On window-state/content events from SystemUI, we search the accessibility tree for
 *     any node whose text or content-description contains "hotspot" (case-insensitive).
 *     This works across languages and OEM skins because Android localises the tile text.
 *  4. We walk up to the nearest clickable ancestor and perform ACTION_CLICK.
 *  5. After clicking we press HOME to close the panel.
 *
 * Why this approach is more reliable than the original app:
 *  - We listen to BOTH typeWindowStateChanged AND typeWindowContentChanged so we catch
 *    both the initial panel open and any subsequent tile load/refresh.
 *  - We retry up to MAX_ATTEMPTS times with increasing delays.
 *  - We fall back to scanning all windows (not just rootInActiveWindow) on Android 12+
 *    where QS lives in a separate window.
 */
class HotspotAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var pendingEnable: Boolean? = null
    private var attempts = 0
    private var wakeLock: PowerManager.WakeLock? = null

    // -------- companion: static singleton handle --------

    companion object {
        private var instance: WeakReference<HotspotAccessibilityService>? = null

        /** Returns false if the service is not connected. */
        fun toggleHotspot(enable: Boolean): Boolean {
            val svc = instance?.get() ?: return false
            svc.handler.post { svc.startAction(enable) }
            return true
        }

        fun isConnected(): Boolean = instance?.get() != null
    }

    // -------- lifecycle --------

    override fun onServiceConnected() {
        instance = WeakReference(this)
        Log.i(TAG, "Service connected")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        handler.removeCallbacksAndMessages(null)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        handler.removeCallbacksAndMessages(null)
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onInterrupt() {
        pendingEnable = null
        handler.removeCallbacksAndMessages(null)
        releaseWakeLock()
    }

    // -------- action entry point --------

    private fun startAction(enable: Boolean) {
        // Wake the screen so performGlobalAction works even when phone is in pocket/screen off
        @Suppress("DEPRECATION")
        val pm = getSystemService(PowerManager::class.java)
        releaseWakeLock()
        @Suppress("DEPRECATION")
        wakeLock = pm.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "BTHotspot::QSTile"
        ).also { it.acquire(WAKELOCK_TIMEOUT_MS) }

        pendingEnable = enable
        attempts = 0
        handler.removeCallbacksAndMessages(null)
        Log.d(TAG, "Waking screen, then opening QS to ${if (enable) "enable" else "disable"} hotspot")
        handler.postDelayed({
            performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
            handler.postDelayed({ searchForTile() }, SEARCH_DELAY_MS)
        }, WAKE_DELAY_MS)
    }

    // -------- accessibility event handler --------

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (pendingEnable == null) return

        val pkg = event.packageName?.toString() ?: return
        val isSystemUi = pkg.contains("systemui", ignoreCase = true)
        if (!isSystemUi) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // Debounce: cancel any pending search and reschedule
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({ searchForTile() }, SEARCH_DELAY_MS)
            }
        }
    }

    // -------- tile search & click --------

    private fun searchForTile() {
        if (pendingEnable == null) return

        val clicked = tryClickInAllWindows()
        if (clicked) {
            Log.i(TAG, "Hotspot tile clicked (enable=${pendingEnable})")
            pendingEnable = null
            handler.removeCallbacksAndMessages(null)
            releaseWakeLock()
            handler.postDelayed({ performGlobalAction(GLOBAL_ACTION_HOME) }, CLOSE_DELAY_MS)
            return
        }

        attempts++
        if (attempts < MAX_ATTEMPTS) {
            Log.d(TAG, "Tile not found, retry $attempts/$MAX_ATTEMPTS")
            handler.postDelayed({ searchForTile() }, RETRY_DELAY_MS)
        } else {
            Log.w(TAG, "Gave up finding hotspot tile after $MAX_ATTEMPTS attempts")
            pendingEnable = null
            releaseWakeLock()
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    /**
     * Searches all available windows (needed on Android 12+ where Quick Settings
     * is rendered in its own window, separate from rootInActiveWindow).
     */
    private fun tryClickInAllWindows(): Boolean {
        // Try all windows first (Android 12+ multi-window accessibility)
        val windows = windows
        if (windows != null) {
            for (window in windows) {
                val root = window.root ?: continue
                if (tryClickHotspotNode(root)) return true
            }
        }
        // Fallback for older API / single-window QS
        val root = rootInActiveWindow ?: return false
        return tryClickHotspotNode(root)
    }

    private fun tryClickHotspotNode(root: AccessibilityNodeInfo): Boolean {
        val node = findHotspotNode(root) ?: return false
        val target = findClickableAncestor(node) ?: return false
        return target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    /** DFS search for any node whose visible text or content-description contains "hotspot". */
    private fun findHotspotNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val text = node.text?.toString() ?: ""
        val desc = node.contentDescription?.toString() ?: ""
        if (text.contains("hotspot", ignoreCase = true) ||
            desc.contains("hotspot", ignoreCase = true)
        ) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findHotspotNode(child)
            if (found != null) return found
        }
        return null
    }

    /** Walks up the tree to find the nearest clickable ancestor (or self). */
    private fun findClickableAncestor(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isClickable) return node
        return node.parent?.let { findClickableAncestor(it) }
    }
}
