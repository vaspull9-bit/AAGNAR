package com.example.aagnar.domain.service

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class P2PSignalingClient @Inject constructor() {
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _connectionState = MutableStateFlow<String>("Disconnected")
    val connectionState = _connectionState

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages = _messages

    fun connectToServer(serverIp: String = "192.168.88.240") {
        // Если уже подключены - сначала отключаемся
        if (socket?.isConnected == true) {
            disconnect()
        }

        scope.launch {
            try {
                withContext(Dispatchers.Main) {
                    _connectionState.value = "Connecting..."
                    _messages.value += "🔄 Connecting to $serverIp:8889"
                }

                socket = Socket()
                withTimeout(10000) {
                    socket!!.connect(java.net.InetSocketAddress(serverIp, 8889), 10000)
                }

                writer = PrintWriter(socket!!.getOutputStream(), true)
                reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))

                withContext(Dispatchers.Main) {
                    _connectionState.value = "Connected"
                    _messages.value += "✅ TCP connected to server"
                }

                startReadingMessages()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _connectionState.value = "Error"
                    _messages.value += "❌ Connection failed: ${e.message}"
                }
            }
        }
    }

    private fun startReadingMessages() {
        scope.launch {
            try {
                var message: String?
                while (reader?.readLine().also { message = it } != null) {
                    withContext(Dispatchers.Main) {
                        _messages.value += "📨 Server: $message"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _messages.value += "❌ Read error: ${e.message}"
                }
                disconnect()
            }
        }
    }

    fun sendMessage(text: String) {
        scope.launch {
            try {
                writer?.println("""{"type":"message","text":"$text","from":"android"}""")
                withContext(Dispatchers.Main) {
                    _messages.value += "📤 Sent: $text"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _messages.value += "❌ Send failed: ${e.message}"
                }
            }
        }
    }

    fun disconnect() {
        scope.launch {
            try {
                writer?.close()
                reader?.close()
                socket?.close()
            } catch (e: Exception) {
                // Ignore
            }
            withContext(Dispatchers.Main) {
                _connectionState.value = "Disconnected"
                _messages.value += "🔌 Disconnected"
            }
        }
    }

    fun cleanup() {
        scope.cancel()
        disconnect()
    }
}