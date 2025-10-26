package com.example.aagnar.data.repository

import android.content.Context
import com.example.aagnar.data.remote.WebSocketClient
import com.example.aagnar.domain.model.Message
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import com.example.aagnar.domain.model.FileInfo // ← ДОБАВЬ ЭТУ СТРОКУ

@Singleton
class WebSocketRepository @Inject constructor(
    private val context: Context
) {
    private var webSocketClient: WebSocketClient? = null

    fun sendFileChunk(
        toUser: String,
        fileInfo: FileInfo,
        chunkData: ByteArray,
        chunkIndex: Int,
        totalChunks: Int
    ) {
        webSocketClient?.sendFileChunk(toUser, fileInfo, chunkData, chunkIndex, totalChunks)
    }

    fun connect(username: String) {
        webSocketClient = WebSocketClient(context, username).apply {
            connect()
        }
    }

    fun disconnect() {
        webSocketClient?.disconnect()
        webSocketClient = null
    }

    fun getConnectionState(): StateFlow<Boolean>? {
        return webSocketClient?.connectionState
    }

    fun getIncomingMessages(): StateFlow<List<Message>>? {
        return webSocketClient?.incomingMessages
    }

    fun sendMessage(toUser: String, content: String, messageId: String) {
        webSocketClient?.sendMessage(toUser, content, messageId)
    }

    fun sendTypingIndicator(toUser: String, isTyping: Boolean) {
        webSocketClient?.sendTypingIndicator(toUser, isTyping)
    }

    fun sendReadReceipt(toUser: String, messageId: String) {
        webSocketClient?.sendReadReceipt(toUser, messageId)
    }

    // Добавляем методы для WebRTC

    fun sendWebRTCMessage(message: String) {
        // TODO: Отправлять сообщения на WebRTC signaling server
    }

    // Добавляем обработку WebRTC сообщений в существующий WebSocket клиент
    private fun handleWebRTCMessage(json: JSONObject) {
        val type = json.getString("type")

        when (type) {
            "incoming-call" -> {
                val from = json.getString("from")
                val callId = json.getString("call_id")
                val isVideoCall = json.getBoolean("video_call")
                // Уведомить UI о входящем звонке
            }
            "call-accepted" -> {
                // Обработать принятие звонка
            }
            "call-rejected" -> {
                // Обработать отклонение звонка
            }
            "offer" -> {
                // Обработать WebRTC offer
            }
            "answer" -> {
                // Обработать WebRTC answer
            }
            "ice-candidate" -> {
                // Обработать ICE candidate
            }
            "call-ended" -> {
                // Обработать завершение звонка
            }
        }
    }

}