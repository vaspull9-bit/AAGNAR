package com.example.aagnar.domain.repository

import com.example.aagnar.domain.model.FileInfo
import com.example.aagnar.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WebSocketRepository {
    suspend fun connect()
    suspend fun disconnect()
    fun isConnected(): Flow<Boolean>
    suspend fun sendMessage(message: String)
    fun observeMessages(): Flow<String>
    suspend fun sendReadReceipt(contactName: String, messageId: String)

    // ДОБАВИТЬ МЕТОДЫ ИЗ ДРУГОГО ФАЙЛА
    suspend fun sendVoiceMessage(contactName: String, audioData: ByteArray, duration: Int, messageId: String)
    fun getConnectionState(): StateFlow<Boolean>?
    fun getIncomingMessages(): StateFlow<List<Message>>?
    fun sendTypingIndicator(toUser: String, isTyping: Boolean)
    suspend fun sendEncryptedMessage(contactName: String, encryptedContent: String, messageId: String)
    // ДОБАВЬТЕ ЭТОТ МЕТОД:
    fun sendWebRTCMessage(message: String)

    // Дополнительные методы если нужны:
    fun sendFileChunk(
        toUser: String,
        fileInfo: FileInfo,
        chunkData: ByteArray,
        chunkIndex: Int,
        totalChunks: Int
    )

    fun connect(username: String)

    fun sendMessage(toUser: String, content: String, messageId: String)


}