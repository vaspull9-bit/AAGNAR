package com.example.aagnar.domain.repository

import com.example.aagnar.domain.model.Message
interface MessageRepository {
    suspend fun sendMessage(roomId: String, message: String)
    suspend fun createDirectMessage(userId: String): String
    suspend fun uploadFile(roomId: String, filePath: String): String
    suspend fun insertMessage(message: Message)
    suspend fun getMessagesWithContact(contactName: String): List<Message>
    suspend fun markMessageAsRead(messageId: String)
    
    }