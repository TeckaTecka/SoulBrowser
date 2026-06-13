package com.teckatecka.netadmin.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teckatecka.netadmin.network.scanner.PortResult
import com.teckatecka.netadmin.network.scanner.PortScanner
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PortScanUiState {
    object Idle                                              : PortScanUiState()
    data class Scanning(val found: List<PortResult>)         : PortScanUiState()
    data class Done(val results: List<PortResult>)           : PortScanUiState()
    data class Error(val message: String)                    : PortScanUiState()
}

class PortScannerViewModel : ViewModel() {

    private val scanner = PortScanner()

    private val _state = MutableStateFlow<PortScanUiState>(PortScanUiState.Idle)
    val state: StateFlow<PortScanUiState> = _state

    private val found   = mutableListOf<PortResult>()
    private var scanJob: Job? = null

    fun scanTop100(host: String) = doScan(host, PortScanner.TOP_100)

    fun scanRange(host: String, from: Int, to: Int) =
        doScan(host, (from..to).map { it }.toIntArray())

    private fun doScan(host: String, ports: IntArray) {
        scanJob?.cancel()
        found.clear()
        _state.value = PortScanUiState.Scanning(emptyList())

        scanJob = viewModelScope.launch {
            try {
                scanner.scan(host, ports).collect { result ->
                    found.add(result)
                    _state.value = PortScanUiState.Scanning(found.sortedBy { it.port })
                }
                _state.value = PortScanUiState.Done(found.sortedBy { it.port })
            } catch (e: Exception) {
                _state.value = PortScanUiState.Error(e.message ?: "Scan failed")
            }
        }
    }

    fun stop() {
        scanJob?.cancel()
        _state.value = PortScanUiState.Done(found.sortedBy { it.port })
    }
}
