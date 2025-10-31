package com.example.aagnar.domain.repository

import com.example.aagnar.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import com.example.aagnar.domain.model.FileInfo

interface WebSocketRepository {
    suspend fun connect()
    suspend fun disconnect()
    fun isConnected(): Flow<Boolean>
    suspend fun sendMessage(message: String)
    fun observeMessages(): Flow<String>
    suspend fun sendReadReceipt(contactName: String, messageId: String)

    // Методы для чата
    suspend fun sendVoiceMessage(contactName: String, audioData: ByteArray, duration: Int, messageId: String)
    fun getConnectionState(): StateFlow<Boolean>?
    fun getIncomingMessages(): StateFlow<List<Message>>?
    fun sendTypingIndicator(toUser: String, isTyping: Boolean)
    suspend fun sendEncryptedMessage(contactName: String, encryptedContent: String, messageId: String)
    fun sendWebRTCMessage(message: String)

    // Методы для файлов
    fun sendFileChunk(
        toUser: String,
        fileInfo: FileInfo,
        chunkData: ByteArray,
        chunkIndex: Int,
        totalChunks: Int
    )

    // Методы с параметрами
    fun connect(username: String)
    fun sendMessage(toUser: String, content: String, messageId: String)

    // ДОБАВИТЬ ТОЛЬКО ОБЪЯВЛЕНИЯ МЕТОДОВ (без реализации)
    fun sendContactInvite(toUser: String)
    fun acceptContactInvite(fromUser: String)
}