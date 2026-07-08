package com.soulbrowser.bthotspot.service

import android.companion.AssociationInfo
import android.companion.CompanionDeviceService
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.soulbrowser.bthotspot.data.PrefsRepository
import com.soulbrowser.bthotspot.hotspot.HotspotController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "CompanionSvc"

/**
 * System-driven trigger: the OS binds this service and calls onDeviceAppeared/Disappeared
 * when the associated device (the car) comes/goes — even if the app process was killed.
 * This is the robust path around aggressive OEM battery management.
 */
@RequiresApi(Build.VERSION_CODES.S)
class HotspotCompanionService : CompanionDeviceService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Android 12 (deprecated in 33)
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onDeviceAppeared(address: String) = handle(address, true)

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onDeviceDisappeared(address: String) = handle(address, false)

    // Android 13+
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onDeviceAppeared(associationInfo: AssociationInfo) =
        handle(associationInfo.deviceMacAddress?.toString(), true)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onDeviceDisappeared(associationInfo: AssociationInfo) =
        handle(associationInfo.deviceMacAddress?.toString(), false)

    private fun handle(address: String?, appeared: Boolean) {
        Log.i(TAG, "Device ${if (appeared) "appeared" else "disappeared"}: $address")
        val app = applicationContext
        // Also bring up the normal monitor service (heartbeat, watchdog, notification).
        ServiceWatchdog.ensureServiceRunning(app)
        scope.launch {
            val target = PrefsRepository(app).selectedDevice.first() ?: return@launch
            // A null MAC (some OEMs) still refers to our single associated device — accept it.
            if (address != null && !address.equals(target.address, ignoreCase = true)) return@launch

            if (appeared) {
                HotspotController.setState(app, true) { ok ->
                    if (ok) scope.launch { HotspotEvents.onEnabled(app) }
                }
            } else if (PrefsRepository(app).autoDisableOnDisconnect.first()) {
                HotspotController.setState(app, false) { ok ->
                    if (ok) scope.launch { HotspotEvents.onDisabled(app) }
                }
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
