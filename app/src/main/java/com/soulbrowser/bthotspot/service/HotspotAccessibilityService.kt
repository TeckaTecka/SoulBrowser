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
private const val WAKE_DELAY_MS = 600L
private const val SEARCH_DELAY_MS = 800L
private const val RETRY_DELAY_MS = 700L
private const val CLOSE_DELAY_MS = 800L
private const val WAKELOCK_TIMEOUT_MS = 25_000L

class HotspotAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var pendingEnable: Boolean? = null
    private var attempts = 0
    private var wakeLock: PowerManager.WakeLock? = null

    // Named Runnable objects — allows cancelling each independently without
    // removeCallbacksAndMessages(null) which would nuke unrelated pending work.
    private val searchRunnable = Runnable { searchForTile() }
    private val openQsRunnable = Runnable {
        performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
        handler.postDelayed(searchRunnable, SEARCH_DELAY_MS)
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

    // -------- lifecycle --------

    override fun onServiceConnected() {
        instance = WeakReference(this)
        Log.i(TAG, "Service connected")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        handler.removeCallbacks(openQsRunnable)
        handler.removeCallbacks(searchRunnable)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        handler.removeCallbacks(openQsRunnable)
        handler.removeCallbacks(searchRunnable)
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onInterrupt() {
        pendingEnable = null
        handler.removeCallbacks(openQsRunnable)
        handler.removeCallbacks(searchRunnable)
        releaseWakeLock()
    }

    // -------- action entry point --------

    private fun startAction(enable: Boolean) {
        val pm = getSystemService(PowerManager::class.java)
        releaseWakeLock()
        // PARTIAL_WAKE_LOCK + ACQUIRE_CAUSES_WAKEUP turns on screen without deprecated FULL_WAKE_LOCK.
        @Suppress("DEPRECATION")
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "BTHotspot::QSTile"
        ).also { it.acquire(WAKELOCK_TIMEOUT_MS) }

        pendingEnable = enable
        attempts = 0
        handler.removeCallbacks(openQsRunnable)
        handler.removeCallbacks(searchRunnable)
        Log.d(TAG, "Waking screen, then opening QS to ${if (enable) "enable" else "disable"} hotspot")
        handler.postDelayed(openQsRunnable, WAKE_DELAY_MS)
    }

    // -------- accessibility event handler --------

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (pendingEnable == null) return

        val pkg = event.packageName?.toString() ?: return
        if (!pkg.contains("systemui", ignoreCase = true)) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // Debounce the tile search only — openQsRunnable must NOT be cancelled here,
                // because SystemUI events fire continuously and would prevent QS from ever opening.
                handler.removeCallbacks(searchRunnable)
                handler.postDelayed(searchRunnable, SEARCH_DELAY_MS)
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
            handler.removeCallbacks(searchRunnable)
            releaseWakeLock()
            handler.postDelayed({ performGlobalAction(GLOBAL_ACTION_HOME) }, CLOSE_DELAY_MS)
            return
        }

        attempts++
        if (attempts < MAX_ATTEMPTS) {
            Log.d(TAG, "Tile not found, retry $attempts/$MAX_ATTEMPTS")
            handler.postDelayed(searchRunnable, RETRY_DELAY_MS)
        } else {
            Log.w(TAG, "Gave up finding hotspot tile after $MAX_ATTEMPTS attempts")
            pendingEnable = null
            handler.removeCallbacks(searchRunnable)
            releaseWakeLock()
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    // Searches all windows — needed on Android 12+ where QS lives in its own window.
    private fun tryClickInAllWindows(): Boolean {
        val windows = windows
        if (windows != null) {
            for (window in windows) {
                val root = window.root ?: continue
                if (tryClickHotspotNode(root)) return true
            }
        }
        val root = rootInActiveWindow ?: return false
        return tryClickHotspotNode(root)
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

    // Iterative BFS — avoids StackOverflowError on deep MIUI QS tree.
    // try-catch per node handles recycled AccessibilityNodeInfo (IllegalStateException).
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
            } catch (_: Exception) { /* node recycled — skip */ }
        }
        return null
    }

    // Iterative parent-walk with depth cap — avoids StackOverflowError and
    // IllegalStateException from stale/recycled nodes.
    private fun findClickableAncestor(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node
        repeat(30) {
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
