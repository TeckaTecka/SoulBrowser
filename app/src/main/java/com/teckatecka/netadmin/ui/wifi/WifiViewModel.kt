package com.teckatecka.netadmin.ui.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teckatecka.netadmin.data.model.WifiNetwork
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class WifiUiState {
    object Idle                                          : WifiUiState()
    object Scanning                                      : WifiUiState()
    data class Results(val networks: List<WifiNetwork>)  : WifiUiState()
    data class Error(val message: String)                : WifiUiState()
}

class WifiViewModel : ViewModel() {

    private val _state = MutableStateFlow<WifiUiState>(WifiUiState.Idle)
    val state: StateFlow<WifiUiState> = _state

    fun scan(context: Context) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        _state.value = WifiUiState.Scanning

        viewModelScope.launch {
            // Registrace receiveru pro výsledky
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    context.unregisterReceiver(this)
                    val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                    val raw     = wifiManager.scanResults
                    processResults(raw)
                }
            }

            val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            context.registerReceiver(receiver, filter)

            val started = wifiManager.startScan()
            if (!started) {
                // Od Android 9+ může být throttling — použijeme cached výsledky
                context.unregisterReceiver(receiver)
                processResults(wifiManager.scanResults)
            }
        }
    }

    private fun processResults(raw: List<ScanResult>) {
        val networks = raw.map { sr ->
            val freq    = sr.frequency
            val channel = frequencyToChannel(freq)
            val band    = when {
                freq < 3000 -> WifiNetwork.Band.GHZ_2_4
                freq < 5925 -> WifiNetwork.Band.GHZ_5
                else        -> WifiNetwork.Band.GHZ_6
            }
            val security = parseSecurity(sr.capabilities)

            WifiNetwork(
                ssid      = sr.SSID.ifEmpty { "<hidden>" },
                bssid     = sr.BSSID,
                rssi      = sr.level,
                channel   = channel,
                frequency = freq,
                security  = security,
                band      = band
            )
        }.sortedByDescending { it.rssi }

        _state.value = WifiUiState.Results(networks)
    }

    private fun frequencyToChannel(freq: Int): Int = when {
        freq in 2412..2484 -> (freq - 2412) / 5 + 1
        freq == 2484       -> 14
        freq in 5170..5825 -> (freq - 5000) / 5
        freq in 5955..7115 -> (freq - 5950) / 5    // 6 GHz
        else               -> 0
    }

    private fun parseSecurity(capabilities: String): String = when {
        capabilities.contains("WPA3")  -> "WPA3"
        capabilities.contains("WPA2")  -> "WPA2"
        capabilities.contains("WPA")   -> "WPA"
        capabilities.contains("WEP")   -> "WEP"
        capabilities.contains("[ESS]") && !capabilities.contains("WPA") -> "Open"
        else                           -> capabilities.take(40)
    }
}
