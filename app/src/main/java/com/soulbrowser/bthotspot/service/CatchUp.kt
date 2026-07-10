package com.soulbrowser.bthotspot.service

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.soulbrowser.bthotspot.data.EventLog
import com.soulbrowser.bthotspot.data.PrefsRepository
import com.soulbrowser.bthotspot.hotspot.HotspotController
import com.soulbrowser.bthotspot.hotspot.HotspotState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

private const val TAG = "CatchUp"

/**
 * Safety net that survives OEM process kills: run periodically by WatchdogWorker.
 * If the selected car is currently connected but the hotspot is off, turn it on.
 * Only ever *enables* — never disables — so it can't turn off a hotspot the user wants.
 */
object CatchUp {

    private val eventScope = CoroutineScope(Dispatchers.Default)

    suspend fun enableIfCarConnected(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) return

        val target = PrefsRepository(context).selectedDevice.first() ?: return
        // Only act when the hotspot is *definitely* off. If it's on or the state is
        // unknown/transitioning, do nothing — this avoids re-announcing an already-on
        // hotspot and avoids blindly toggling when we can't read the state.
        if (HotspotState.isOn(context) != false) return

        if (!isDeviceConnected(context, target.address)) return

        Log.i(TAG, "Catch-up: car connected but hotspot off — enabling")
        EventLog.log(context, "Záchrana na pozadí: auto připojené, hotspot vypnutý → zapínám")
        HotspotController.setState(context, true) { ok ->
            if (ok) eventScope.launch { HotspotEvents.onEnabled(context) }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun isDeviceConnected(context: Context, address: String): Boolean {
        val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter ?: return false
        return withTimeoutOrNull(4_000) {
            suspendCancellableCoroutine { cont ->
                val profiles = intArrayOf(BluetoothProfile.A2DP, BluetoothProfile.HEADSET)
                val remaining = AtomicInteger(profiles.size)
                val found = AtomicBoolean(false)

                fun finishOne() {
                    if (remaining.decrementAndGet() == 0 && cont.isActive) cont.resume(found.get())
                }

                for (p in profiles) {
                    val started = try {
                        adapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
                            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                                try {
                                    if (proxy.connectedDevices.any { it.address.equals(address, ignoreCase = true) }) {
                                        found.set(true)
                                    }
                                } catch (_: Exception) {
                                } finally {
                                    try { adapter.closeProfileProxy(profile, proxy) } catch (_: Exception) {}
                                    finishOne()
                                }
                            }
                            override fun onServiceDisconnected(profile: Int) {}
                        }, p)
                    } catch (_: Exception) {
                        false
                    }
                    if (!started) finishOne()
                }
            }
        } ?: false
    }
}
