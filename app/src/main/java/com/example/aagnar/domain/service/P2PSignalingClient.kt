package com.example.aagnar.domain.service

import kotlinx.coroutines.flow.MutableStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class P2PSignalingClient @Inject constructor() {
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    private val _connectionState = MutableStateFlow<String>("Disconnected")
    val connectionState = _connectionState

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages = _messages

    fun connectToServer(serverIp: String = "192.168.88.240") {
        try {
            _connectionState.value = "Connecting..."
            _messages.value += "🔄 Connecting to $serverIp:8887"

            socket = Socket(serverIp, 8887)
            writer = PrintWriter(socket!!.getOutputStream(), true)
            reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))

            _connectionState.value = "Connected"
            _messages.value += "✅ TCP connected to server"

            // Читаем сообщения от сервера в отдельном потоке
            Thread {
                try {
                    var message: String?
                    while (reader?.readLine().also { message = it } != null) {
                        _messages.value += "📨 Server: $message"
                    }
                } catch (e: IOException) {
                    _messages.value += "❌ Read error: ${e.message}"
                    disconnect()
                }
            }.start()

        } catch (e: Exception) {
            _connectionState.value = "Error"
            _messages.value += "❌ Connection failed: ${e.message}"
        }
    }

    fun sendMessage(text: String) {
        try {
            writer?.println("""{"type":"message","text":"$text","from":"android"}""")
            _messages.value += "📤 Sent: $text"
        } catch (e: Exception) {
            _messages.value += "❌ Send failed: ${e.message}"
        }
    }

    fun disconnect() {
        try {
            writer?.close()
            reader?.close()
            socket?.close()
        } catch (e: Exception) {
            // Ignore
        }
        _connectionState.value = "Disconnected"
        _messages.value += "🔌 Disconnected"
    }
}