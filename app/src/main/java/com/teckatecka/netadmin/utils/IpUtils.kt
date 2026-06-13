package com.teckatecka.netadmin.utils

import java.net.InetAddress

object IpUtils {

    /** Vrátí lokální IPv4 adresu zařízení ve formátu "192.168.1.5" nebo null. */
    fun getLocalIpAddress(): String? {
        return try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()?.toList() ?: return null
            for (iface in interfaces) {
                if (!iface.isUp || iface.isLoopback) continue
                for (addr in iface.inetAddresses.toList()) {
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) return addr.hostAddress
                }
            }
            null
        } catch (_: Exception) { null }
    }

    /** Z IP adresy a prefixu vrátí network adresu (long). */
    fun networkAddress(ip: Long, prefixLen: Int): Long {
        val mask = if (prefixLen == 0) 0L else (0xFFFFFFFFL shl (32 - prefixLen)) and 0xFFFFFFFFL
        return ip and mask
    }

    /** Broadcast adresa. */
    fun broadcastAddress(ip: Long, prefixLen: Int): Long {
        val mask = if (prefixLen == 0) 0L else (0xFFFFFFFFL shl (32 - prefixLen)) and 0xFFFFFFFFL
        return (ip and mask) or (mask.inv() and 0xFFFFFFFFL)
    }

    /** Počet hostitelských adres v prefixu. */
    fun hostCount(prefixLen: Int): Long = when (prefixLen) {
        32   -> 1L
        31   -> 2L
        else -> (1L shl (32 - prefixLen)) - 2
    }

    /** Převede dotted-decimal string na long. */
    fun ipToLong(ip: String): Long {
        val parts = ip.trim().split(".")
        if (parts.size != 4) throw IllegalArgumentException("Invalid IP: $ip")
        var result = 0L
        for (part in parts) {
            val octet = part.toInt()
            if (octet !in 0..255) throw IllegalArgumentException("Invalid octet: $octet")
            result = (result shl 8) or octet.toLong()
        }
        return result
    }

    /** Převede long na dotted-decimal string. */
    fun longToIp(value: Long): String {
        return "${(value shr 24) and 0xFF}.${(value shr 16) and 0xFF}.${(value shr 8) and 0xFF}.${value and 0xFF}"
    }

    /** Převede prefix length na dotted subnet mask. */
    fun prefixToMask(prefixLen: Int): String {
        val mask = if (prefixLen == 0) 0L else (0xFFFFFFFFL shl (32 - prefixLen)) and 0xFFFFFFFFL
        return longToIp(mask)
    }

    /** Převede dotted subnet mask na prefix length. */
    fun maskToPrefix(mask: String): Int {
        val maskLong = ipToLong(mask)
        var prefix = 0
        var bit = 1L shl 31
        while (bit > 0 && (maskLong and bit) != 0L) {
            prefix++
            bit = bit shr 1
        }
        return prefix
    }

    /** Wildcard maska (inverze subnet masky). */
    fun wildcardMask(prefixLen: Int): String {
        val mask = if (prefixLen == 0) 0L else (0xFFFFFFFFL shl (32 - prefixLen)) and 0xFFFFFFFFL
        return longToIp(mask.inv() and 0xFFFFFFFFL)
    }

    /** Parsuje CIDR vstup (např. "192.168.1.0/24") a vrátí Pair(ip, prefix). */
    fun parseCidr(cidr: String): Pair<String, Int> {
        val parts = cidr.trim().split("/")
        val ip     = parts[0].trim()
        val prefix = if (parts.size == 2) parts[1].trim().toInt() else 32
        if (prefix !in 0..32) throw IllegalArgumentException("Prefix out of range: $prefix")
        ipToLong(ip) // validace IP
        return Pair(ip, prefix)
    }

    /**
     * Ping pomocí InetAddress.isReachable() — nevyžaduje root od API 26.
     * Vrátí dobu odezvy v ms, nebo -1 pokud host nedostupný.
     */
    suspend fun ping(host: String, timeoutMs: Int = 1000): Long {
        return try {
            val addr  = InetAddress.getByName(host)
            val start = System.currentTimeMillis()
            val up    = addr.isReachable(timeoutMs)
            if (up) System.currentTimeMillis() - start else -1L
        } catch (_: Exception) { -1L }
    }

    /** Vrátí nejbližší vyšší mocninu 2 — pro VLSM výpočet velikosti bloku. */
    fun nextPowerOfTwo(n: Long): Long {
        var p = 1L
        while (p < n) p = p shl 1
        return p
    }
}
