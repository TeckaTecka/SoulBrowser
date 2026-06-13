package com.teckatecka.netadmin.ui.monitoring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teckatecka.netadmin.utils.IpUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MonitoredHost(
    val host:         String,
    val intervalMin:  Int,
    val isUp:         Boolean?    = null,   // null = ještě nezkontrolováno
    val lastCheckMs:  Long        = 0,
    val lastPingMs:   Long        = -1
)

class MonitoringViewModel : ViewModel() {

    private val _hosts = MutableStateFlow<List<MonitoredHost>>(emptyList())
    val hosts: StateFlow<List<MonitoredHost>> = _hosts

    private val jobs = mutableMapOf<String, Job>()

    fun addHost(host: String, intervalMin: Int) {
        val entry = MonitoredHost(host = host, intervalMin = intervalMin)
        _hosts.value = _hosts.value + entry
        startMonitoring(entry)
    }

    fun removeHost(host: MonitoredHost) {
        jobs[host.host]?.cancel()
        jobs.remove(host.host)
        _hosts.value = _hosts.value.filter { it.host != host.host }
    }

    private fun startMonitoring(entry: MonitoredHost) {
        val job = viewModelScope.launch {
            while (isActive) {
                val pingMs = IpUtils.ping(entry.host, 3000)
                val isUp   = pingMs >= 0
                updateHost(entry.host) { it.copy(isUp = isUp, lastCheckMs = System.currentTimeMillis(), lastPingMs = pingMs) }
                delay(entry.intervalMin * 60_000L)
            }
        }
        jobs[entry.host] = job
    }

    private fun updateHost(host: String, transform: (MonitoredHost) -> MonitoredHost) {
        _hosts.value = _hosts.value.map { if (it.host == host) transform(it) else it }
    }

    fun stopAll() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
    }

    override fun onCleared() {
        super.onCleared()
        stopAll()
    }
}
