package com.example.aagnar.domain.model

data class Chat(
    val id: String,
    val contactName: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
)