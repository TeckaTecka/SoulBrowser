package com.teckatecka.netadmin.network.ssh

import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

class SshClient {

    private var session: Session?      = null
    private var channel: ChannelShell? = null
    var inputStream:  InputStream?  = null
        private set
    var outputStream: OutputStream? = null
        private set

    val isConnected: Boolean
        get() = session?.isConnected == true && channel?.isConnected == true

    suspend fun connect(
        host:     String,
        port:     Int    = 22,
        username: String,
        password: String
    ) = withContext(Dispatchers.IO) {
        val jsch = JSch()
        val s = jsch.getSession(username, host, port)
        s.setPassword(password)
        s.setConfig("StrictHostKeyChecking", "no")
        s.connect(10_000)

        val ch = s.openChannel("shell") as ChannelShell
        ch.setPtyType("vt100")
        ch.setPtySize(80, 24, 800, 480)

        inputStream  = ch.inputStream
        outputStream = ch.outputStream
        ch.connect(5_000)

        session = s
        channel = ch
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        channel?.disconnect()
        session?.disconnect()
        channel = null
        session = null
        inputStream  = null
        outputStream = null
    }

    suspend fun send(text: String) = withContext(Dispatchers.IO) {
        outputStream?.write(text.toByteArray())
        outputStream?.flush()
    }
}
