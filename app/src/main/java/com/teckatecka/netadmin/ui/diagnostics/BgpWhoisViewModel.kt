package com.teckatecka.netadmin.ui.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teckatecka.netadmin.network.bgp.BgpWhoisClient
import com.teckatecka.netadmin.network.bgp.IpInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BgpUiState {
    object Idle                                              : BgpUiState()
    object Loading                                           : BgpUiState()
    data class IpResult(val info: IpInfo, val whois: String) : BgpUiState()
    data class AsnResult(val text: String)                   : BgpUiState()
    data class Error(val message: String)                    : BgpUiState()
}

class BgpWhoisViewModel : ViewModel() {

    private val client = BgpWhoisClient()

    private val _state = MutableStateFlow<BgpUiState>(BgpUiState.Idle)
    val state: StateFlow<BgpUiState> = _state

    /** Autodetekuje typ vstupu — IP, ASN (AS12345) nebo doménu. */
    fun lookup(query: String) {
        val q = query.trim()
        _state.value = BgpUiState.Loading

        viewModelScope.launch {
            try {
                when {
                    q.matches(Regex("^(AS|as)?\\d+$")) -> {
                        val text = client.lookupAsn(q)
                        _state.value = BgpUiState.AsnResult(text)
                    }
                    q.matches(Regex("^\\d{1,3}(\\.\\d{1,3}){3}$")) -> {
                        val info  = client.lookupIp(q)
                        val whois = client.rdapIp(q)
                        _state.value = BgpUiState.IpResult(info, whois.raw)
                    }
                    else -> {
                        // Zkusíme jako doménu — přeložíme na IP a pak lookup
                        val ip = java.net.InetAddress.getByName(q).hostAddress ?: throw Exception("Cannot resolve $q")
                        val info  = client.lookupIp(ip)
                        val whois = client.rdapIp(ip)
                        _state.value = BgpUiState.IpResult(info.copy(ip = "$q → $ip"), whois.raw)
                    }
                }
            } catch (e: Exception) {
                _state.value = BgpUiState.Error(e.message ?: "Lookup failed")
            }
        }
    }

    fun reset() { _state.value = BgpUiState.Idle }
}
