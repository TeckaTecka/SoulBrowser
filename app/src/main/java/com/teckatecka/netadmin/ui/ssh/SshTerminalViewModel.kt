package com.teckatecka.netadmin.ui.ssh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teckatecka.netadmin.network.ssh.SshClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class SshUiState {
    object Idle       : SshUiState()
    object Connecting : SshUiState()
    object Connected  : SshUiState()
    data class Error(val message: String) : SshUiState()
}

class SshTerminalViewModel : ViewModel() {

    private val client = SshClient()

    private val _state    = MutableStateFlow<SshUiState>(SshUiState.Idle)
    val state: StateFlow<SshUiState> = _state

    private val _output   = MutableStateFlow("")
    val output: StateFlow<String> = _output

    private var readJob: Job? = null

    fun connect(host: String, port: Int, username: String, password: String) {
        viewModelScope.launch {
            _state.value = SshUiState.Connecting
            try {
                client.connect(host, port, username, password)
                _state.value = SshUiState.Connected
                startReading()
            } catch (e: Exception) {
                _state.value = SshUiState.Error(e.message ?: "Connection failed")
            }
        }
    }

    private fun startReading() {
        readJob = viewModelScope.launch(Dispatchers.IO) {
            val buf = ByteArray(4096)
            try {
                while (client.isConnected) {
                    val available = client.inputStream?.available() ?: 0
                    if (available > 0) {
                        val n = client.inputStream!!.read(buf, 0, minOf(available, buf.size))
                        if (n > 0) {
                            val text = String(buf, 0, n, Charsets.UTF_8)
                            withContext(Dispatchers.Main) {
                                _output.value = _output.value + text
                            }
                        }
                    } else {
                        kotlinx.coroutines.delay(50)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun sendCommand(cmd: String) {
        viewModelScope.launch {
            try {
                client.send(cmd + "\n")
            } catch (e: Exception) {
                _state.value = SshUiState.Error(e.message ?: "Send failed")
            }
        }
    }

    fun disconnect() {
        readJob?.cancel()
        viewModelScope.launch {
            client.disconnect()
            _state.value = SshUiState.Idle
            _output.value = ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        readJob?.cancel()
        viewModelScope.launch { client.disconnect() }
    }
}
