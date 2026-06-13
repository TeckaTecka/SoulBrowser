package com.teckatecka.netadmin.network.scanner

import com.teckatecka.netadmin.data.model.ScanResult
import com.teckatecka.netadmin.utils.IpUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.InetAddress
import java.net.NetworkInterface

/**
 * Skenuje lokální síť paralelně pomocí InetAddress.isReachable().
 * Emit-uje ScanResult pro každý nalezený host průběžně.
 */
class LanScanner {

    private val TAG = "LanScanner"

    /**
     * Spustí sken podsítě definované IP a prefixem.
     * Pokud ip/prefix jsou null, autodetekuje aktuální síť zařízení.
     */
    fun scan(
        baseIp:      String? = null,
        prefixLen:   Int?    = null,
        timeoutMs:   Int     = 800,
        parallelism: Int     = 64
    ): Flow<ScanResult> = flow {
        val localIp = baseIp ?: IpUtils.getLocalIpAddress() ?: return@flow
        val prefix  = prefixLen ?: detectPrefixLen(localIp) ?: 24

        val networkLong   = IpUtils.networkAddress(IpUtils.ipToLong(localIp), prefix)
        val broadcastLong = IpUtils.broadcastAddress(IpUtils.ipToLong(localIp), prefix)

        // Rozdělíme IP rozsah na chunky a zpracujeme paralelně
        val allIps = (networkLong + 1) until broadcastLong
        allIps.chunked(parallelism).forEach { chunk ->
            coroutineScope {
                chunk.map { ipLong ->
                    async(Dispatchers.IO) {
                        val ipStr = IpUtils.longToIp(ipLong)
                        val start = System.currentTimeMillis()
                        try {
                            val addr  = InetAddress.getByName(ipStr)
                            val up    = addr.isReachable(timeoutMs)
                            val ms    = System.currentTimeMillis() - start
                            if (up) {
                                val hostname = try { addr.canonicalHostName } catch (_: Exception) { "" }
                                ScanResult(
                                    ip       = ipStr,
                                    hostname = if (hostname == ipStr) "" else hostname,
                                    isUp     = true,
                                    pingMs   = ms
                                )
                            } else null
                        } catch (_: Exception) { null }
                    }
                }.awaitAll().filterNotNull().forEach { emit(it) }
            }
        }
    }.flowOn(Dispatchers.IO)

    /** Pokusí se zjistit délku prefixu z NetworkInterface pro danou IP. */
    private fun detectPrefixLen(ip: String): Int? {
        return try {
            NetworkInterface.getNetworkInterfaces()?.toList()?.forEach { iface ->
                iface.interfaceAddresses.forEach { ifAddr ->
                    if (ifAddr.address.hostAddress == ip) return ifAddr.networkPrefixLength.toInt()
                }
            }
            null
        } catch (_: Exception) { null }
    }
}

/** Extension pro chunking LongRange. */
private fun LongRange.chunked(size: Int): List<List<Long>> {
    val result = mutableListOf<List<Long>>()
    var chunk  = mutableListOf<Long>()
    for (i in this) {
        chunk.add(i)
        if (chunk.size == size) {
            result.add(chunk)
            chunk = mutableListOf()
        }
    }
    if (chunk.isNotEmpty()) result.add(chunk)
    return result
}
