package com.example.pokedex.data.online

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean

class OnlinePvpTransport @javax.inject.Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _incomingPackets = MutableSharedFlow<OnlinePacket>(extraBufferCapacity = 32)
    val incomingPackets: Flow<OnlinePacket> = _incomingPackets.asSharedFlow()

    private var serverSocket: ServerSocket? = null
    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: BufferedWriter? = null
    private var readerJob: Job? = null
    private val isClosed = AtomicBoolean(false)

    suspend fun host(): RoomInvite = withContext(Dispatchers.IO) {
        close()
        val server = ServerSocket(0)
        server.soTimeout = 0
        serverSocket = server
        val invite = RoomInvite(
            hostAddress = NetworkUtils.localIpv4Address(),
            hostPort = server.localPort,
            roomId = NetworkUtils.randomRoomId()
        )
        scope.launch { acceptLoop(server) }
        invite
    }

    suspend fun join(code: String) = withContext(Dispatchers.IO) {
        close()
        val invite = RoomInviteCodec.decode(code)
        val client = Socket(invite.hostAddress, invite.hostPort)
        bindSocket(client)
    }

    suspend fun send(packet: OnlinePacket) = withContext(Dispatchers.IO) {
        writer?.apply {
            write(OnlinePacketCodec.encode(packet))
            newLine()
            flush()
        }
    }

    suspend fun close() = withContext(Dispatchers.IO) {
        if (isClosed.compareAndSet(false, true)) {
            readerJob?.cancel()
            readerJob = null
            try {
                writer?.flush()
            } catch (_: Throwable) {
            }
            try {
                reader?.close()
            } catch (_: Throwable) {
            }
            try {
                writer?.close()
            } catch (_: Throwable) {
            }
            try {
                socket?.close()
            } catch (_: Throwable) {
            }
            try {
                serverSocket?.close()
            } catch (_: Throwable) {
            }
            reader = null
            writer = null
            socket = null
            serverSocket = null
        }
    }

    private suspend fun acceptLoop(server: ServerSocket) = withContext(Dispatchers.IO) {
        val client = server.accept()
        bindSocket(client)
    }

    private fun bindSocket(client: Socket) {
        isClosed.set(false)
        socket = client
        writer = BufferedWriter(OutputStreamWriter(client.getOutputStream(), Charsets.UTF_8))
        reader = BufferedReader(InputStreamReader(client.getInputStream(), Charsets.UTF_8))
        startReader()
    }

    private fun startReader() {
        readerJob?.cancel()
        readerJob = scope.launch {
            try {
                while (!isClosed.get()) {
                    val line = reader?.readLine() ?: break
                    if (line.isNotBlank()) {
                        _incomingPackets.tryEmit(OnlinePacketCodec.decode(line))
                    }
                }
            } catch (_: SocketTimeoutException) {
            } catch (_: SocketException) {
            } catch (_: Throwable) {
            } finally {
                close()
            }
        }
    }
}

private object NetworkUtils {
    fun localIpv4Address(): String {
        val interfaces = java.net.NetworkInterface.getNetworkInterfaces() ?: throw UnknownHostException("Aucune interface reseau")
        for (networkInterface in interfaces.toList()) {
            if (!networkInterface.isUp || networkInterface.isLoopback) continue
            val addresses = networkInterface.inetAddresses.toList()
            for (address in addresses) {
                if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                    return address.hostAddress ?: continue
                }
            }
        }
        throw UnknownHostException("Impossible de determiner l'adresse IPv4 locale")
    }

    fun randomRoomId(): String = java.util.UUID.randomUUID().toString().take(8).uppercase()
}

