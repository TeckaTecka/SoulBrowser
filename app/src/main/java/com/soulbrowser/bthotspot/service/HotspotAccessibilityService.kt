package com.soulbrowser.bthotspot.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
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
private const val WAKE_DELAY_MS = 700L
private const val INITIAL_DELAY_MS = 500L
private const val DEBOUNCE_MS = 350L
private const val RETRY_MS = 700L
private const val CLOSE_MS = 600L
private const val WAKELOCK_TIMEOUT_MS = 20_000L

/**
 * Last-resort fallback: toggles the hotspot by tapping the Quick Settings tile.
 * Used only when the TetheringManager/ConnectivityManager path in HotspotController fails.
 */
class HotspotAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var pendingEnable: Boolean? = null
    private var attempts = 0
    private var wakeLock: PowerManager.WakeLock? = null

    // Named runnable so onAccessibilityEvent can debounce it independently.
    private val searchRunnable = Runnable { searchForTile() }
    private val openQsRunnable = Runnable {
        performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
        handler.postDelayed(searchRunnable, INITIAL_DELAY_MS)
    }

    companion object {
        private var instance: WeakReference<HotspotAccessibilityService>? = null

        fun toggleHotspot(enable: Boolean): Boolean {
            val svc = instance?.get() ?: return false
            svc.handler.post { svc.startAction(enable) }
            return true
        }

        fun isConnected(): Boolean = instance?.get() != null
    }

    override fun onServiceConnected() {
        instance = WeakReference(this)
        Log.i(TAG, "connected")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        cancelAll()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        cancelAll()
        super.onDestroy()
    }

    override fun onInterrupt() {
        pendingEnable = null
        cancelAll()
    }

    private fun cancelAll() {
        handler.removeCallbacks(searchRunnable)
        handler.removeCallbacks(openQsRunnable)
        releaseWakeLock()
    }

    private fun startAction(enable: Boolean) {
        pendingEnable = enable
        attempts = 0
        handler.removeCallbacks(searchRunnable)
        handler.removeCallbacks(openQsRunnable)
        // Wake the screen first — opening Quick Settings and clicking the tile
        // requires the display to be on (phone is usually in a pocket in the car).
        acquireScreenWake()
        Log.d(TAG, "Waking screen, then opening QS → ${if (enable) "ENABLE" else "DISABLE"} hotspot")
        handler.postDelayed(openQsRunnable, WAKE_DELAY_MS)
    }

    private fun acquireScreenWake() {
        releaseWakeLock()
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP or
                PowerManager.ON_AFTER_RELEASE,
            "BTHotspot::QSTile"
        ).also { it.acquire(WAKELOCK_TIMEOUT_MS) }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (pendingEnable == null) return
        val pkg = event.packageName?.toString() ?: return
        if (!pkg.contains("systemui", ignoreCase = true)) return
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                handler.removeCallbacks(searchRunnable)
                handler.postDelayed(searchRunnable, DEBOUNCE_MS)
            }
        }
    }

    private fun searchForTile() {
        if (pendingEnable == null) return

        if (tryClickInAllWindows()) {
            Log.i(TAG, "tile clicked (enable=${pendingEnable})")
            pendingEnable = null
            handler.removeCallbacks(searchRunnable)
            releaseWakeLock()
            handler.postDelayed({ performGlobalAction(GLOBAL_ACTION_BACK) }, CLOSE_MS)
            return
        }

        attempts++
        if (attempts < MAX_ATTEMPTS) {
            Log.d(TAG, "tile not found, retry $attempts/$MAX_ATTEMPTS")
            handler.postDelayed(searchRunnable, RETRY_MS)
        } else {
            Log.w(TAG, "gave up after $MAX_ATTEMPTS attempts")
            pendingEnable = null
            releaseWakeLock()
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    private fun tryClickInAllWindows(): Boolean {
        windows?.forEach { window ->
            val root = window.root ?: return@forEach
            if (tryClickHotspotNode(root)) return true
        }
        return rootInActiveWindow?.let { tryClickHotspotNode(it) } ?: false
    }

    private fun tryClickHotspotNode(root: AccessibilityNodeInfo): Boolean {
        val node = findHotspotNode(root) ?: return false
        val target = findClickableAncestor(node) ?: return false
        return try {
            target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } catch (_: Exception) {
            false
        }
    }

    // Iterative BFS — safe on deep MIUI QS trees (no StackOverflowError).
    private fun findHotspotNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            try {
                val text = node.text?.toString() ?: ""
                val desc = node.contentDescription?.toString() ?: ""
                if (text.contains("hotspot", ignoreCase = true) ||
                    desc.contains("hotspot", ignoreCase = true)
                ) return node
                for (i in 0 until node.childCount) {
                    node.getChild(i)?.let { queue.add(it) }
                }
            } catch (_: Exception) {}
        }
        return null
    }

    // Iterative parent walk with depth cap — safe with recycled nodes.
    private fun findClickableAncestor(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node
        repeat(20) {
            val n = current ?: return null
            try {
                if (n.isClickable) return n
                current = n.parent
            } catch (_: Exception) {
                return null
            }
        }
        return null
    }
}
