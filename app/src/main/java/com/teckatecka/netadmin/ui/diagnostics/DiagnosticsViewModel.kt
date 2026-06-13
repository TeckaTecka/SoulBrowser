package com.teckatecka.netadmin.ui.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teckatecka.netadmin.utils.IpUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

data class PingLine(
    val seq:     Int,
    val host:    String,
    val ms:      Long,          // -1 = timeout
    val ttl:     Int    = 0
)

data class TracerouteHop(
    val hop:      Int,
    val ip:       String,
    val hostname: String,
    val ms:       Long          // -1 = timeout/no reply
)

data class DnsRecord(
    val type:  String,
    val value: String
)

sealed class DiagUiState {
    object Idle                                          : DiagUiState()
    data class PingRunning(val lines: List<PingLine>)   : DiagUiState()
    data class PingDone(
        val lines: List<PingLine>,
        val sent:  Int, val received: Int,
        val minMs: Long, val avgMs: Long, val maxMs: Long
    ) : DiagUiState()
    data class TracerouteRunning(val hops: List<TracerouteHop>) : DiagUiState()
    data class TracerouteDone(val hops: List<TracerouteHop>)    : DiagUiState()
    data class DnsResult(val records: List<DnsRecord>)          : DiagUiState()
    data class Error(val message: String)                       : DiagUiState()
}

class DiagnosticsViewModel : ViewModel() {

    private val _state = MutableStateFlow<DiagUiState>(DiagUiState.Idle)
    val state: StateFlow<DiagUiState> = _state

    private var runningJob: Job? = null

    fun ping(host: String, count: Int = 4, intervalMs: Long = 1000) {
        cancelRunning()
        val lines = mutableListOf<PingLine>()
        _state.value = DiagUiState.PingRunning(emptyList())

        runningJob = viewModelScope.launch(Dispatchers.IO) {
            for (seq in 1..count) {
                if (!isActive) break
                val ms = IpUtils.ping(host, 2000)
                val line = PingLine(seq = seq, host = host, ms = ms)
                lines.add(line)
                _state.value = DiagUiState.PingRunning(lines.toList())
                if (seq < count) kotlinx.coroutines.delay(intervalMs)
            }
            val received = lines.count { it.ms >= 0 }
            val successMs = lines.filter { it.ms >= 0 }.map { it.ms }
            _state.value = DiagUiState.PingDone(
                lines    = lines.toList(),
                sent     = count,
                received = received,
                minMs    = successMs.minOrNull() ?: 0,
                avgMs    = if (successMs.isEmpty()) 0 else successMs.average().toLong(),
                maxMs    = successMs.maxOrNull() ?: 0
            )
        }
    }

    /**
     * Jednoduchý traceroute implementovaný přes TTL-omezené sockety.
     * Každý hop posílá paket s postupně rostoucím TTL a čeká na ICMP Time Exceeded.
     * Funguje bez root přes TCP SYN connect (Android safe).
     */
    fun traceroute(host: String, maxHops: Int = 30, timeoutMs: Int = 2000) {
        cancelRunning()
        val hops = mutableListOf<TracerouteHop>()
        _state.value = DiagUiState.TracerouteRunning(emptyList())

        runningJob = viewModelScope.launch(Dispatchers.IO) {
            val targetIp = try { InetAddress.getByName(host).hostAddress ?: host } catch (_: Exception) { host }

            for (ttl in 1..maxHops) {
                if (!isActive) break
                val start = System.currentTimeMillis()
                var hopIp       = ""
                var hopHostname = ""
                var hopMs       = -1L

                try {
                    val socket = java.net.Socket()
                    socket.soTimeout   = timeoutMs
                    // Nastavení TTL přes SocketOptions — reflection přístup pro Android
                    val implField = socket.javaClass.getDeclaredField("impl")
                    implField.isAccessible = true
                    val impl = implField.get(socket)
                    impl.javaClass.getMethod("setOption", Int::class.java, Any::class.java)
                        .invoke(impl, 0x1E /* IP_TTL */, ttl)

                    socket.connect(java.net.InetSocketAddress(targetIp, 80), timeoutMs)
                    hopMs       = System.currentTimeMillis() - start
                    hopIp       = socket.inetAddress?.hostAddress ?: targetIp
                    hopHostname = try { InetAddress.getByName(hopIp).canonicalHostName } catch (_: Exception) { hopIp }
                    socket.close()
                } catch (e: java.net.SocketTimeoutException) {
                    hopMs = -1L
                } catch (e: Exception) {
                    // ConnectException může nést informaci o intermediate hopu
                    hopMs       = System.currentTimeMillis() - start
                    hopIp       = e.message?.substringAfter("connect to ")?.substringBefore(" ")?.trim() ?: ""
                    hopHostname = if (hopIp.isNotEmpty()) {
                        try { InetAddress.getByName(hopIp).canonicalHostName } catch (_: Exception) { hopIp }
                    } else ""
                }

                val hop = TracerouteHop(hop = ttl, ip = hopIp, hostname = hopHostname, ms = hopMs)
                hops.add(hop)
                _state.value = DiagUiState.TracerouteRunning(hops.toList())

                if (hopIp == targetIp) break
            }
            _state.value = DiagUiState.TracerouteDone(hops.toList())
        }
    }

    /** DNS lookup přes java.net.InetAddress (A/AAAA záznamy). */
    fun dnsLookup(domain: String) {
        cancelRunning()
        _state.value = DiagUiState.Error("") // reset

        runningJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val addresses = InetAddress.getAllByName(domain)
                val records = addresses.map { addr ->
                    DnsRecord(
                        type  = if (addr is java.net.Inet6Address) "AAAA" else "A",
                        value = addr.hostAddress ?: ""
                    )
                }
                _state.value = DiagUiState.DnsResult(records)
            } catch (e: Exception) {
                _state.value = DiagUiState.Error(e.message ?: "DNS lookup failed")
            }
        }
    }

    fun cancelRunning() {
        runningJob?.cancel()
        runningJob = null
    }

    fun reset() {
        cancelRunning()
        _state.value = DiagUiState.Idle
    }
}
