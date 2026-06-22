package com.soulbrowser.bthotspot.ui

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulbrowser.bthotspot.data.DeviceInfo
import com.soulbrowser.bthotspot.data.PrefsRepository
import com.soulbrowser.bthotspot.service.BluetoothMonitorService
import com.soulbrowser.bthotspot.service.HotspotAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val pairedDevices: List<DeviceInfo> = emptyList(),
    val accessibilityEnabled: Boolean = false,
    val serviceRunning: Boolean = false,
    val btEnabled: Boolean = false,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PrefsRepository(application)
    private val btAdapter: BluetoothAdapter? =
        (application.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    val selectedDevice: StateFlow<DeviceInfo?> = prefs.selectedDevice
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun refresh(accessibilityEnabled: Boolean) {
        _uiState.update {
            it.copy(
                pairedDevices = getPairedDevices(),
                accessibilityEnabled = accessibilityEnabled,
                serviceRunning = true, // Service is kept alive by the system
                btEnabled = btAdapter?.isEnabled == true,
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun getPairedDevices(): List<DeviceInfo> {
        return try {
            btAdapter?.bondedDevices
                ?.map { DeviceInfo(address = it.address, name = it.name ?: it.address) }
                ?.sortedBy { it.name }
                ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    fun selectDevice(device: DeviceInfo?) {
        viewModelScope.launch {
            prefs.saveSelectedDevice(device)
        }
    }

    fun startService(context: Context) {
        val intent = Intent(context, BluetoothMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
