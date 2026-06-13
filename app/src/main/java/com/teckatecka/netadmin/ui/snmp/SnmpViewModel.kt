package com.teckatecka.netadmin.ui.snmp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teckatecka.netadmin.network.snmp.SnmpClient
import com.teckatecka.netadmin.network.snmp.SnmpVarBind
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SnmpUiState {
    object Idle                              : SnmpUiState()
    object Loading                           : SnmpUiState()
    data class Success(val items: List<SnmpVarBind>) : SnmpUiState()
    data class Error(val message: String)    : SnmpUiState()
}

class SnmpViewModel : ViewModel() {

    private val client = SnmpClient()

    private val _state = MutableStateFlow<SnmpUiState>(SnmpUiState.Idle)
    val state: StateFlow<SnmpUiState> = _state

    private var activeJob: Job? = null

    fun walk(host: String, port: Int, community: String, baseOid: String) {
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            _state.value = SnmpUiState.Loading
            try {
                val results = client.walk(host, port, community, baseOid)
                _state.value = if (results.isEmpty()) SnmpUiState.Error("No results") else SnmpUiState.Success(results)
            } catch (e: Exception) {
                _state.value = SnmpUiState.Error(e.message ?: "SNMP error")
            }
        }
    }

    fun getOids(host: String, port: Int, community: String, oids: List<String>) {
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            _state.value = SnmpUiState.Loading
            try {
                val results = client.get(host, port, community, oids)
                _state.value = if (results.isEmpty()) SnmpUiState.Error("No response") else SnmpUiState.Success(results)
            } catch (e: Exception) {
                _state.value = SnmpUiState.Error(e.message ?: "SNMP error")
            }
        }
    }

    fun cancel() {
        activeJob?.cancel()
        _state.value = SnmpUiState.Idle
    }

    // Přednastavené OID pro systémové info
    val SYSTEM_OIDS = listOf(
        "1.3.6.1.2.1.1.1.0",  // sysDescr
        "1.3.6.1.2.1.1.3.0",  // sysUpTime
        "1.3.6.1.2.1.1.4.0",  // sysContact
        "1.3.6.1.2.1.1.5.0",  // sysName
        "1.3.6.1.2.1.1.6.0",  // sysLocation
        "1.3.6.1.2.1.1.7.0"   // sysServices
    )

    val INTERFACE_OID = "1.3.6.1.2.1.2"
}
