package com.example.aagnar.domain.model

import java.util.*

data class Group(
    val id: String,
    val name: String,
    val creator: String,
    val members: List<String>,
    val admins: List<String>,
    val createdAt: Date,
    val lastMessage: String? = null,
    val lastMessageTime: Date? = null,
    val unreadCount: Int = 0,
    val avatarUrl: String? = null
)

data class GroupMessage(
    val id: String,
    val groupId: String,
    val sender: String,
    val content: String,
    val timestamp: Date,
    val type: MessageType,
    val isDelivered: Boolean = false,
    val isRead: Boolean = false,
    val hasAttachment: Boolean = false,
    val fileInfo: FileInfo? = null,
    val isVoiceMessage: Boolean = false,
    val voiceMessageInfo: VoiceMessageInfo? = null
)

data class GroupMember(
    val username: String,
    val isAdmin: Boolean = false,
    val joinedAt: Date = Date()
)