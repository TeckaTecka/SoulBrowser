package com.teckatecka.netadmin.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teckatecka.netadmin.data.model.ScanResult
import com.teckatecka.netadmin.network.scanner.LanScanner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ScannerUiState {
    object Idle                                          : ScannerUiState()
    data class Scanning(val found: List<ScanResult>)    : ScannerUiState()
    data class Done(val results: List<ScanResult>)      : ScannerUiState()
    data class Error(val message: String)               : ScannerUiState()
}

class ScannerViewModel : ViewModel() {

    private val scanner = LanScanner()

    private val _state = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val state: StateFlow<ScannerUiState> = _state

    private val found  = mutableListOf<ScanResult>()
    private var scanJob: Job? = null

    fun startScan() {
        if (scanJob?.isActive == true) return
        found.clear()
        _state.value = ScannerUiState.Scanning(emptyList())

        scanJob = viewModelScope.launch {
            try {
                scanner.scan().collect { result ->
                    found.add(result)
                    _state.value = ScannerUiState.Scanning(found.toList())
                }
                _state.value = ScannerUiState.Done(found.toList())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = ScannerUiState.Error(e.message ?: "Scan failed")
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        _state.value = ScannerUiState.Done(found.toList())
    }
}
