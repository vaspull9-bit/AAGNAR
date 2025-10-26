package com.example.aagnar.domain.repository

import kotlinx.coroutines.flow.Flow

interface WebSocketRepository {
    suspend fun connect()
    suspend fun disconnect()
    fun isConnected(): Flow<Boolean>
    suspend fun sendMessage(message: String)
    fun observeMessages(): Flow<String>
    // добавьте этот метод
    suspend fun sendReadReceipt(contactName: String, messageId: String)
}