package com.example.aagnar.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "group_message") // ← ДОЛЖНО СОВПАДАТЬ С "group_message" в DAO
data class GroupMessage(
    @PrimaryKey
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