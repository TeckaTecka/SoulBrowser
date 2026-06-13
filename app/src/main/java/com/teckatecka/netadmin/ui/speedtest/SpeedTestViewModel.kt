package com.teckatecka.netadmin.ui.speedtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
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

    private val _state = MutableStateFlow<SpeedTestUiState>(SpeedTestUiState.Idle)
    val state: StateFlow<SpeedTestUiState> = _state

    // Test server — Cloudflare speed test endpoint (nezaregistrovaný)
    private val TEST_HOST        = "speed.cloudflare.com"
    private val DOWNLOAD_URL     = "https://$TEST_HOST/__down?bytes=10000000"   // 10 MB
    private val UPLOAD_URL       = "https://$TEST_HOST/__up"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun startTest() {
        _state.value = SpeedTestUiState.Testing(SpeedTestUiState.Phase.PING, 0f)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Ping
                val pingMs = measurePing()
                _state.value = SpeedTestUiState.Testing(SpeedTestUiState.Phase.DOWNLOAD, 0f)

                // 2. Download
                val downloadMbps = measureDownload()
                _state.value = SpeedTestUiState.Testing(SpeedTestUiState.Phase.UPLOAD, 0.5f)

                // 3. Upload
                val uploadMbps = measureUpload()

                _state.value = SpeedTestUiState.Done(
                    downloadMbps = downloadMbps,
                    uploadMbps   = uploadMbps,
                    pingMs       = pingMs
                )
            } catch (e: Exception) {
                _state.value = SpeedTestUiState.Error(e.message ?: "Test failed")
            }
        }
    }

    private fun measurePing(): Long {
        val request = Request.Builder().url("https://$TEST_HOST/cdn-cgi/trace").build()
        val start   = System.currentTimeMillis()
        client.newCall(request).execute().use { }
        return System.currentTimeMillis() - start
    }

    private fun measureDownload(): Double {
        val request  = Request.Builder().url(DOWNLOAD_URL).build()
        val start    = System.currentTimeMillis()
        var bytes    = 0L
        client.newCall(request).execute().use { response ->
            val body    = response.body ?: throw Exception("Empty body")
            val buf     = ByteArray(8192)
            val stream  = body.byteStream()
            var read    = stream.read(buf)
            while (read != -1) {
                bytes += read
                read   = stream.read(buf)
            }
        }
        val elapsed = (System.currentTimeMillis() - start) / 1000.0
        return (bytes * 8) / elapsed / 1_000_000.0  // Mbps
    }

    private fun measureUpload(): Double {
        val uploadBytes = ByteArray(5_000_000) { 0 }   // 5 MB
        val body        = RequestBody.create("application/octet-stream".toMediaType(), uploadBytes)
        val request     = Request.Builder().url(UPLOAD_URL).post(body).build()
        val start       = System.currentTimeMillis()
        client.newCall(request).execute().use { }
        val elapsed     = (System.currentTimeMillis() - start) / 1000.0
        return (uploadBytes.size * 8L) / elapsed / 1_000_000.0
    }

    fun reset() {
        _state.value = SpeedTestUiState.Idle
    }
}
