package com.teckatecka.netadmin.network.scanner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.InetSocketAddress
import java.net.Socket

data class PortResult(
    val port:    Int,
    val state:   State,
    val service: String = KnownPorts.name(port)
) {
    enum class State { OPEN, CLOSED, FILTERED }
}

class PortScanner {

    fun scan(
        host:        String,
        ports:       IntArray,
        timeoutMs:   Int = 1500,
        parallelism: Int = 64
    ): Flow<PortResult> = flow {
        ports.toList().chunked(parallelism).forEach { chunk ->
            coroutineScope {
                chunk.map { port ->
                    async(Dispatchers.IO) {
                        try {
                            Socket().use { sock ->
                                sock.connect(InetSocketAddress(host, port), timeoutMs)
                                PortResult(port, PortResult.State.OPEN)
                            }
                        } catch (e: java.net.ConnectException) {
                            PortResult(port, PortResult.State.CLOSED)
                        } catch (_: Exception) {
                            PortResult(port, PortResult.State.FILTERED)
                        }
                    }
                }.awaitAll().filter { it.state == PortResult.State.OPEN }.forEach { emit(it) }
            }
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        val TOP_100 = intArrayOf(
            21, 22, 23, 25, 53, 80, 110, 111, 135, 139, 143, 179, 199,
            443, 445, 465, 514, 515, 548, 587, 631, 993, 995, 1025, 1026,
            1027, 1028, 1029, 1110, 1433, 1720, 1723, 1755, 1900, 2000,
            2001, 2049, 2121, 2717, 3000, 3128, 3306, 3389, 3986, 4899,
            5000, 5009, 5051, 5060, 5101, 5190, 5357, 5432, 5631, 5666,
            5800, 5900, 6000, 6001, 6646, 7070, 8000, 8008, 8009, 8080,
            8081, 8443, 8888, 9100, 9999, 10000, 32768, 49152, 49153,
            49154, 49155, 49156, 49157, 8728, 8729, 2000, 8291, 2001
        )
    }
}

/** Databáze nejznámějších portů. */
object KnownPorts {
    private val ports = mapOf(
        21    to "FTP",
        22    to "SSH",
        23    to "Telnet",
        25    to "SMTP",
        53    to "DNS",
        80    to "HTTP",
        110   to "POP3",
        143   to "IMAP",
        179   to "BGP",
        443   to "HTTPS",
        445   to "SMB",
        465   to "SMTPS",
        587   to "SMTP Submission",
        993   to "IMAPS",
        995   to "POP3S",
        1433  to "MS SQL",
        2049  to "NFS",
        3306  to "MySQL",
        3389  to "RDP",
        5432  to "PostgreSQL",
        5900  to "VNC",
        6379  to "Redis",
        8080  to "HTTP Alt",
        8443  to "HTTPS Alt",
        8728  to "RouterOS API",
        8729  to "RouterOS API SSL",
        8291  to "Winbox",
        9100  to "Printer",
        27017 to "MongoDB"
    )

    fun name(port: Int): String = ports[port] ?: ""
}
