package com.teckatecka.netadmin.ui.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teckatecka.netadmin.data.model.RdpProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Socket

sealed class TerminalUiState {
    data class ProfileList(val profiles: List<RdpProfile>) : TerminalUiState()
    data class WinRmResult(val host: String, val ok: Boolean, val message: String) : TerminalUiState()
}

class TerminalServerViewModel : ViewModel() {

    // V paměti — v produkci by se profily ukládaly do Room DB
    private val profiles = mutableListOf<RdpProfile>()

    private val _state = MutableStateFlow<TerminalUiState>(TerminalUiState.ProfileList(emptyList()))
    val state: StateFlow<TerminalUiState> = _state

    fun addProfile(profile: RdpProfile) {
        profiles.add(profile.copy(id = System.currentTimeMillis()))
        _state.value = TerminalUiState.ProfileList(profiles.toList())
    }

    fun deleteProfile(profile: RdpProfile) {
        profiles.removeAll { it.id == profile.id }
        _state.value = TerminalUiState.ProfileList(profiles.toList())
    }

    /** TCP connect test na WinRM port 5985. */
    fun checkWinRm(host: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Socket().use { sock ->
                    sock.connect(InetSocketAddress(host, 5985), 3000)
                }
                _state.value = TerminalUiState.WinRmResult(host, true, "OK")
            } catch (e: Exception) {
                _state.value = TerminalUiState.WinRmResult(host, false, e.message ?: "Unreachable")
            }
        }
    }

    fun refreshProfiles() {
        _state.value = TerminalUiState.ProfileList(profiles.toList())
    }
}
