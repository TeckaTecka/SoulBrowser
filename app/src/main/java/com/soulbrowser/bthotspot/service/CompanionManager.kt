package com.soulbrowser.bthotspot.service

import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.util.Log

private const val TAG = "CompanionManager"

/**
 * Wraps CompanionDeviceManager so the system wakes the app when the car connects — even if
 * the process was killed by the OEM. Requires a one-time association (a system dialog), then
 * observes device presence; HotspotCompanionService receives appear/disappear callbacks.
 */
object CompanionManager {

    fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    /** MAC of the first associated device, or null if none / unsupported. */
    fun associatedMac(context: Context): String? {
        if (!isSupported()) return null
        return try {
            val cdm = context.getSystemService(CompanionDeviceManager::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                cdm.myAssociations.firstOrNull()?.deviceMacAddress?.toString()
            } else {
                @Suppress("DEPRECATION")
                cdm.associations.firstOrNull()
            }
        } catch (e: Exception) {
            Log.w(TAG, "associatedMac failed: ${e.message}")
            null
        }
    }

    fun isAssociated(context: Context): Boolean = associatedMac(context) != null

    /** (Re)start presence observation for an already-associated device. Idempotent, safe to call often. */
    fun startObserving(context: Context, mac: String?) {
        if (!isSupported() || mac.isNullOrEmpty()) return
        try {
            val cdm = context.getSystemService(CompanionDeviceManager::class.java)
            @Suppress("DEPRECATION")
            cdm.startObservingDevicePresence(mac)
            Log.i(TAG, "Observing presence of $mac")
        } catch (e: Exception) {
            Log.w(TAG, "startObserving failed: ${e.message}")
        }
    }

    /**
     * Kicks off the association flow for [mac]. When the system produces the chooser
     * IntentSender, [launchChooser] is invoked so the Activity can show the confirm dialog.
     */
    fun associate(
        context: Context,
        mac: String,
        launchChooser: (IntentSender) -> Unit,
        onError: (String) -> Unit,
    ) {
        if (!isSupported()) {
            onError("Nepodporováno na tomto Androidu")
            return
        }
        try {
            val cdm = context.getSystemService(CompanionDeviceManager::class.java)
            // BluetoothDeviceFilter matches the address case-sensitively; normalise to uppercase.
            val filter = BluetoothDeviceFilter.Builder().setAddress(mac.uppercase()).build()
            val request = AssociationRequest.Builder()
                .addDeviceFilter(filter)
                .setSingleDevice(true)
                .build()
            val callback = object : CompanionDeviceManager.Callback() {
                @Suppress("OVERRIDE_DEPRECATION")
                override fun onDeviceFound(chooserLauncher: IntentSender) {
                    launchChooser(chooserLauncher)
                }
                override fun onAssociationPending(intentSender: IntentSender) {
                    launchChooser(intentSender)
                }
                override fun onFailure(error: CharSequence?) {
                    Log.w(TAG, "Association failed: $error")
                    onError(error?.toString() ?: "nenalezeno")
                }
            }
            cdm.associate(request, callback, null)
        } catch (e: Exception) {
            Log.w(TAG, "associate failed: ${e.message}")
            onError(e.message ?: e.javaClass.simpleName)
        }
    }
}
