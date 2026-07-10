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
import com.soulbrowser.bthotspot.hotspot.HotspotState
import java.lang.ref.WeakReference

private const val TAG = "HotspotA11y"
private const val MAX_ATTEMPTS = 8
private const val WAKE_DELAY_MS = 700L
private const val INITIAL_DELAY_MS = 500L
private const val DEBOUNCE_MS = 350L
private const val RETRY_MS = 700L
private const val VERIFY_MS = 1500L
private const val CLOSE_MS = 600L
private const val WAKELOCK_TIMEOUT_MS = 20_000L

/**
 * Fallback hotspot control by tapping the Quick Settings tile — but *state-aware*:
 * it reads the real SoftAP state (HotspotState), only clicks the tile when the current
 * state differs from the target, verifies the result after clicking, and reports the
 * actual outcome via a callback. This avoids the blind-toggle bug where the tile was
 * flipped the wrong way when the app's assumed state diverged from reality.
 */
class HotspotAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var target: Boolean? = null
    private var resultCallback: ((Boolean) -> Unit)? = null
    private var attempts = 0
    private var clicked = false
    private var qsOpened = false
    private var wakeLock: PowerManager.WakeLock? = null

    private val searchRunnable = Runnable { searchForTile() }
    private val openQsRunnable = Runnable {
        qsOpened = true
        performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
        handler.postDelayed(searchRunnable, INITIAL_DELAY_MS)
    }

    companion object {
        private var instance: WeakReference<HotspotAccessibilityService>? = null

        /** Drive the hotspot to [enable]; [onResult] gets the actual verified outcome. */
        fun setHotspot(enable: Boolean, onResult: ((Boolean) -> Unit)?): Boolean {
            val svc = instance?.get() ?: return false
            svc.handler.post { svc.startAction(enable, onResult) }
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
        abort()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        abort()
        super.onDestroy()
    }

    override fun onInterrupt() {
        abort()
    }

    private fun abort() {
        val cb = resultCallback
        target = null
        resultCallback = null
        handler.removeCallbacks(searchRunnable)
        handler.removeCallbacks(openQsRunnable)
        releaseWakeLock()
        cb?.invoke(false)
    }

    private fun startAction(enable: Boolean, onResult: ((Boolean) -> Unit)?) {
        // A new request supersedes any in-flight one — report the old one as failed.
        resultCallback?.invoke(false)

        target = enable
        resultCallback = onResult
        attempts = 0
        clicked = false
        qsOpened = false
        handler.removeCallbacks(searchRunnable)
        handler.removeCallbacks(openQsRunnable)

        // Fast path: already in the desired state — do nothing (this is the key fix that
        // stops the tile being toggled the wrong way).
        if (HotspotState.isOn(this) == enable) {
            Log.i(TAG, "Already ${if (enable) "ON" else "OFF"} — no action")
            finish(true)
            return
        }

        acquireScreenWake()
        Log.d(TAG, "Waking screen, then opening QS → ${if (enable) "ENABLE" else "DISABLE"}")
        handler.postDelayed(openQsRunnable, WAKE_DELAY_MS)
    }

    private fun searchForTile() {
        val t = target ?: return

        // Reached the target (either already, or from our click landing)?
        if (HotspotState.isOn(this) == t) {
            Log.i(TAG, "Verified target reached (${if (t) "ON" else "OFF"})")
            finish(true)
            return
        }

        // Unknown/mid-transition — wait for it to settle, don't click blindly.
        if (HotspotState.isOn(this) == null && clicked) {
            reschedule(VERIFY_MS)
            return
        }

        // State differs from target → click the tile once, then verify on later ticks.
        if (!clicked) {
            if (tryClickInAllWindows()) {
                clicked = true
                Log.i(TAG, "Tile clicked — will verify")
                reschedule(VERIFY_MS)
                return
            }
            // Tile not found yet — keep looking.
            reschedule(RETRY_MS)
            return
        }

        // Already clicked but not yet at target — give it more time, then give up.
        reschedule(VERIFY_MS)
    }

    private fun reschedule(delay: Long) {
        attempts++
        if (attempts < MAX_ATTEMPTS) {
            handler.postDelayed(searchRunnable, delay)
        } else {
            val actual = HotspotState.isOn(this)
            Log.w(TAG, "Gave up after $MAX_ATTEMPTS attempts (state=$actual)")
            // If we can't read state at all, assume the click worked (legacy behaviour).
            finish(actual == target || actual == null)
        }
    }

    private fun finish(success: Boolean) {
        val cb = resultCallback
        target = null
        resultCallback = null
        handler.removeCallbacks(searchRunnable)
        handler.removeCallbacks(openQsRunnable)
        releaseWakeLock()
        if (qsOpened) {
            handler.postDelayed({ performGlobalAction(GLOBAL_ACTION_BACK) }, CLOSE_MS)
        }
        cb?.invoke(success)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (target == null) return
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

    private fun tryClickInAllWindows(): Boolean {
        windows?.forEach { window ->
            val root = window.root ?: return@forEach
            if (tryClickHotspotNode(root)) return true
        }
        return rootInActiveWindow?.let { tryClickHotspotNode(it) } ?: false
    }

    private fun tryClickHotspotNode(root: AccessibilityNodeInfo): Boolean {
        val node = findHotspotNode(root) ?: return false
        val clickable = findClickableAncestor(node) ?: return false
        return try {
            clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK)
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
