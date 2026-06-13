package com.teckatecka.netadmin.ui.routeros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teckatecka.netadmin.network.routeros.RouterOsClient
import com.teckatecka.netadmin.network.routeros.RouterOsException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class RouterInfo(
    val identity:     String,
    val version:      String,
    val boardName:    String,
    val cpuLoad:      Int,
    val freeMemory:   Long,
    val totalMemory:  Long,
    val uptime:       String
)

data class RouterInterface(
    val name:       String,
    val type:       String,
    val macAddress: String,
    val ipAddress:  String,
    val running:    Boolean,
    val txBytes:    Long,
    val rxBytes:    Long
)

sealed class RouterOsUiState {
    object Disconnected                                          : RouterOsUiState()
    object Connecting                                            : RouterOsUiState()
    data class Connected(
        val host: String,
        val info: RouterInfo? = null,
        val interfaces: List<RouterInterface> = emptyList()
    ) : RouterOsUiState()
    data class Error(val message: String)                        : RouterOsUiState()
}

class RouterOsViewModel : ViewModel() {

    private val client = RouterOsClient()

    private val _state = MutableStateFlow<RouterOsUiState>(RouterOsUiState.Disconnected)
    val state: StateFlow<RouterOsUiState> = _state

    private var keepAliveJob: Job? = null

    fun connect(host: String, port: Int, username: String, password: String) {
        _state.value = RouterOsUiState.Connecting
        viewModelScope.launch(Dispatchers.IO) {
            try {
                client.connect(host, port, username, password)
                _state.value = RouterOsUiState.Connected(host = host)
                loadDashboard(host)
                startKeepalive()
            } catch (e: Exception) {
                _state.value = RouterOsUiState.Error(e.message ?: "Connection failed")
            }
        }
    }

    private suspend fun loadDashboard(host: String) {
        try {
            val resource    = client.send(listOf("/system/resource/print")).firstOrNull() ?: return
            val identity    = client.send(listOf("/system/identity/print")).firstOrNull()?.get("name") ?: host
            val interfaces  = loadInterfaces()

            val info = RouterInfo(
                identity    = identity,
                version     = resource["version"]     ?: "",
                boardName   = resource["board-name"]  ?: "",
                cpuLoad     = resource["cpu-load"]?.toIntOrNull() ?: 0,
                freeMemory  = resource["free-memory"]?.toLongOrNull() ?: 0,
                totalMemory = resource["total-memory"]?.toLongOrNull() ?: 0,
                uptime      = resource["uptime"]      ?: ""
            )
            _state.value = RouterOsUiState.Connected(host = host, info = info, interfaces = interfaces)
        } catch (e: Exception) {
            _state.value = RouterOsUiState.Error(e.message ?: "Failed to load dashboard")
        }
    }

    private suspend fun loadInterfaces(): List<RouterInterface> {
        return try {
            val ifaceList  = client.send(listOf("/interface/print"))
            val ipList     = client.send(listOf("/ip/address/print"))

            ifaceList.map { iface ->
                val name    = iface["name"] ?: ""
                val ipEntry = ipList.find { it["interface"] == name }
                RouterInterface(
                    name       = name,
                    type       = iface["type"]        ?: "",
                    macAddress = iface["mac-address"] ?: "",
                    ipAddress  = ipEntry?.get("address") ?: "",
                    running    = iface["running"]     == "true",
                    txBytes    = iface["tx-byte"]?.toLongOrNull() ?: 0,
                    rxBytes    = iface["rx-byte"]?.toLongOrNull() ?: 0
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    /** Keepalive každých 30s aby session nevypršela. */
    private fun startKeepalive() {
        keepAliveJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive && client.isConnected) {
                delay(30_000)
                try { client.send(listOf("/system/identity/print")) }
                catch (_: Exception) { break }
            }
        }
    }

    fun disconnect() {
        keepAliveJob?.cancel()
        client.disconnect()
        _state.value = RouterOsUiState.Disconnected
    }

    override fun onCleared() {
        super.onCleared()
        client.disconnect()
    }
}
