package com.teckatecka.netadmin.utils

data class CidrResult(
    val inputIp:      String,
    val prefixLen:    Int,
    val networkAddr:  String,
    val broadcastAddr:String,
    val subnetMask:   String,
    val wildcardMask: String,
    val firstHost:    String,
    val lastHost:     String,
    val hostCount:    Long,
    val ipBinary:     String,
    val maskBinary:   String
)

data class VlsmSubnet(
    val name:        String,
    val hostsNeeded: Int
)

data class VlsmResult(
    val name:         String,
    val network:      String,
    val prefixLen:    Int,
    val subnetMask:   String,
    val firstHost:    String,
    val lastHost:     String,
    val broadcastAddr:String,
    val usableHosts:  Long
)

object CidrCalculator {

    /** Hlavní výpočet CIDR. Vyhodí IllegalArgumentException při chybném vstupu. */
    fun calculate(cidr: String): CidrResult {
        val (ipStr, prefix) = IpUtils.parseCidr(cidr)
        val ipLong    = IpUtils.ipToLong(ipStr)
        val netLong   = IpUtils.networkAddress(ipLong, prefix)
        val bcastLong = IpUtils.broadcastAddress(ipLong, prefix)
        val hostCount = IpUtils.hostCount(prefix)

        val firstHost = if (prefix < 31) IpUtils.longToIp(netLong + 1) else IpUtils.longToIp(netLong)
        val lastHost  = if (prefix < 31) IpUtils.longToIp(bcastLong - 1) else IpUtils.longToIp(bcastLong)

        return CidrResult(
            inputIp       = ipStr,
            prefixLen     = prefix,
            networkAddr   = IpUtils.longToIp(netLong),
            broadcastAddr = IpUtils.longToIp(bcastLong),
            subnetMask    = IpUtils.prefixToMask(prefix),
            wildcardMask  = IpUtils.wildcardMask(prefix),
            firstHost     = firstHost,
            lastHost      = lastHost,
            hostCount     = hostCount,
            ipBinary      = longToBinary(ipLong),
            maskBinary    = longToBinary(IpUtils.ipToLong(IpUtils.prefixToMask(prefix)))
        )
    }

    /**
     * VLSM výpočet.
     * Seřadí podsítě od největší po nejmenší a alokuje bloky z baseNetwork.
     * Vyhodí IllegalStateException pokud nestačí adresní prostor.
     */
    fun calculateVlsm(baseNetwork: String, subnets: List<VlsmSubnet>): List<VlsmResult> {
        val (baseIp, basePrefix) = IpUtils.parseCidr(baseNetwork)
        val baseNetLong  = IpUtils.networkAddress(IpUtils.ipToLong(baseIp), basePrefix)
        val baseBcastLong = IpUtils.broadcastAddress(IpUtils.ipToLong(baseIp), basePrefix)
        val totalAddresses = baseBcastLong - baseNetLong + 1

        // Seřazení od největší potřeby po nejmenší
        val sorted = subnets.sortedByDescending { it.hostsNeeded }
        val results = mutableListOf<VlsmResult>()
        var currentStart = baseNetLong

        for (subnet in sorted) {
            // Potřebujeme hostsNeeded + 2 (síť + broadcast) adres, zaokrouhleno nahoru na mocninu 2
            val needed     = subnet.hostsNeeded + 2L
            val blockSize  = IpUtils.nextPowerOfTwo(needed)
            val prefixLen  = 32 - java.lang.Long.numberOfTrailingZeros(blockSize).toInt()

            // Zarovnání na hranici bloku
            val aligned = alignToBlock(currentStart, blockSize)
            val blockEnd = aligned + blockSize - 1

            if (blockEnd > baseBcastLong) {
                throw IllegalStateException("Insufficient address space for subnet '${subnet.name}'")
            }

            val usable = IpUtils.hostCount(prefixLen)
            results.add(VlsmResult(
                name          = subnet.name,
                network       = IpUtils.longToIp(aligned),
                prefixLen     = prefixLen,
                subnetMask    = IpUtils.prefixToMask(prefixLen),
                firstHost     = IpUtils.longToIp(aligned + 1),
                lastHost      = IpUtils.longToIp(blockEnd - 1),
                broadcastAddr = IpUtils.longToIp(blockEnd),
                usableHosts   = usable
            ))
            currentStart = blockEnd + 1
        }
        return results
    }

    private fun alignToBlock(addr: Long, blockSize: Long): Long {
        val remainder = addr % blockSize
        return if (remainder == 0L) addr else addr + (blockSize - remainder)
    }

    private fun longToBinary(value: Long): String {
        val bin = java.lang.Long.toBinaryString(value).padStart(32, '0')
        return "${bin.substring(0, 8)}.${bin.substring(8, 16)}.${bin.substring(16, 24)}.${bin.substring(24, 32)}"
    }
}
