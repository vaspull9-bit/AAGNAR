package com.example.aagnar.data.repository

import android.content.Context
import com.example.aagnar.data.remote.WebSocketClient
import com.example.aagnar.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import com.example.aagnar.domain.model.FileInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.aagnar.domain.repository.WebSocketRepository as DomainWebSocketRepository

@Singleton
class WebSocketRepository @Inject constructor(
    @ApplicationContext private val context: Context  // Добавьте @ApplicationContext
) : DomainWebSocketRepository {

    private var webSocketClient: WebSocketClient? = null

    override suspend fun sendEncryptedMessage(contactName: String, encryptedContent: String, messageId: String) {
        sendMessage(contactName, encryptedContent, messageId)
    }

    override suspend fun connect() {
        // TODO: Реализовать suspend connect с username
        // Временная реализация:
        webSocketClient?.connect()
    }

    override suspend fun disconnect() {
        webSocketClient?.disconnect()
        webSocketClient = null
    }

    override fun isConnected(): Flow<Boolean> {
        return webSocketClient?.connectionState ?: flowOf(false)
    }

    override suspend fun sendMessage(message: String) {
        // TODO: Реализовать отправку простого сообщения без указания получателя
        // Временная реализация:
        webSocketClient?.sendMessage("unknown", message, System.currentTimeMillis().toString())
    }

    override fun observeMessages(): Flow<String> {
        // TODO: Реализовать Flow сообщений
        return flowOf()
    }

    override suspend fun sendReadReceipt(contactName: String, messageId: String) {
        webSocketClient?.sendReadReceipt(contactName, messageId)
    }

    override suspend fun sendVoiceMessage(contactName: String, audioData: ByteArray, duration: Int, messageId: String) {
        // TODO: Реализовать отправку голосового сообщения
        // Временная заглушка для компиляции
    }

    override fun getConnectionState(): StateFlow<Boolean>? {
        return webSocketClient?.connectionState
    }

    override fun getIncomingMessages(): StateFlow<List<Message>>? {
        return webSocketClient?.incomingMessages
    }

    override fun sendTypingIndicator(toUser: String, isTyping: Boolean) {
        webSocketClient?.sendTypingIndicator(toUser, isTyping)
    }

    // ДОБАВИТЬ в WebSocketRepository.kt
    override fun sendContactInvite(toUser: String) {
        val username = getUsernameFromPrefs()
        // Используем webSocketClient который объявлен в классе
        webSocketClient?.sendContactInvite(toUser, username)
    }

    override fun acceptContactInvite(fromUser: String) {
        // Используем webSocketClient который объявлен в классе
        webSocketClient?.acceptContactInvite(fromUser)
    }

    private fun getUsernameFromPrefs(): String {
        // context уже есть в классе через @ApplicationContext
        val prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        return prefs.getString("username", "user_${System.currentTimeMillis()}") ?: "user_${System.currentTimeMillis()}"
    }


    // Существующие методы

    override fun sendFileChunk(
        toUser: String,
        fileInfo: FileInfo,
        chunkData: ByteArray,
        chunkIndex: Int,
        totalChunks: Int
    ) {
        webSocketClient?.sendFileChunk(toUser, fileInfo, chunkData, chunkIndex, totalChunks)
    }

    override  fun connect(username: String) {
        webSocketClient = WebSocketClient(context, username).apply {
            connect()
        }
    }

    override fun sendMessage(toUser: String, content: String, messageId: String) {
        webSocketClient?.sendMessage(toUser, content, messageId)
    }

    // WebRTC методы

    override fun sendWebRTCMessage(message: String) {
        // TODO: Отправлять сообщения на WebRTC signaling server
    }

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