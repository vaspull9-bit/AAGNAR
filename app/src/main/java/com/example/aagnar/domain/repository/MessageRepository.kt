package com.example.aagnar.domain.repository

interface MessageRepository {
    suspend fun sendMessage(roomId: String, message: String)
    suspend fun createDirectMessage(userId: String): String
    suspend fun uploadFile(roomId: String, filePath: String): String
}