package com.teckatecka.netadmin.ui.speedtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

sealed class SpeedTestUiState {
    object Idle                          : SpeedTestUiState()
    data class Testing(
        val phase:    Phase,
        val progress: Float         // 0..1
    ) : SpeedTestUiState()
    data class Done(
        val downloadMbps: Double,
        val uploadMbps:   Double,
        val pingMs:       Long
    ) : SpeedTestUiState()
    data class Error(val message: String) : SpeedTestUiState()

    enum class Phase { PING, DOWNLOAD, UPLOAD }
}

class SpeedTestViewModel : ViewModel() {

    private val _state   = MutableStateFlow<SpeedTestUiState>(SpeedTestUiState.Idle)
    val state: StateFlow<SpeedTestUiState> = _state

    private val _history = MutableStateFlow<List<SpeedTestRecord>>(emptyList())
    val history: StateFlow<List<SpeedTestRecord>> = _history

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val TEST_HOST    = "speed.cloudflare.com"
    private val DOWNLOAD_URL = "https://$TEST_HOST/__down?bytes=10000000"
    private val UPLOAD_URL   = "https://$TEST_HOST/__up"

    fun startTest() {
        if (_state.value is SpeedTestUiState.Testing) return
        _state.value = SpeedTestUiState.Testing(SpeedTestUiState.Phase.PING, 0f)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pingMs = measurePing()
                _state.value = SpeedTestUiState.Testing(SpeedTestUiState.Phase.DOWNLOAD, 0.1f)

                val downloadMbps = measureDownload()
                _state.value = SpeedTestUiState.Testing(SpeedTestUiState.Phase.UPLOAD, 0.6f)

                val uploadMbps = measureUpload()

                val result = SpeedTestUiState.Done(downloadMbps, uploadMbps, pingMs)
                _state.value = result

                _history.value = listOf(
                    SpeedTestRecord(System.currentTimeMillis(), downloadMbps, uploadMbps, pingMs)
                ) + _history.value
            } catch (e: Exception) {
                _state.value = SpeedTestUiState.Error(e.message ?: "Test failed")
            }
        }
    }

    private fun measurePing(): Long {
        val req   = Request.Builder().url("https://$TEST_HOST/cdn-cgi/trace").build()
        val start = System.currentTimeMillis()
        client.newCall(req).execute().use { }
        return System.currentTimeMillis() - start
    }

    private fun measureDownload(): Double {
        val req     = Request.Builder().url(DOWNLOAD_URL).build()
        val start   = System.currentTimeMillis()
        var bytes   = 0L
        client.newCall(req).execute().use { resp ->
            val buf    = ByteArray(8192)
            val stream = resp.body!!.byteStream()
            var n      = stream.read(buf)
            while (n != -1) { bytes += n; n = stream.read(buf) }
        }
        return toMbps(bytes, System.currentTimeMillis() - start)
    }

    private fun measureUpload(): Double {
        val data  = ByteArray(5_000_000)
        val body  = RequestBody.create("application/octet-stream".toMediaType(), data)
        val req   = Request.Builder().url(UPLOAD_URL).post(body).build()
        val start = System.currentTimeMillis()
        client.newCall(req).execute().use { }
        return toMbps(data.size.toLong(), System.currentTimeMillis() - start)
    }

    private fun toMbps(bytes: Long, ms: Long): Double =
        if (ms == 0L) 0.0 else (bytes * 8.0) / ms / 1000.0

    fun reset() { _state.value = SpeedTestUiState.Idle }
}
