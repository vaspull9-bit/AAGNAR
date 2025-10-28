package com.example.aagnar.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    val id: String,
    val contactName: String = "",  // для WebSocket
    val content: String = "",      // для WebSocket
    val text: String = "",         // для Room
    val timestamp: Long,
    val date: Date = Date(timestamp),
    val type: MessageType = MessageType.RECEIVED,
    val isRead: Boolean = false,
    val isDelivered: Boolean = false,
    val isEncrypted: Boolean = false,
    val isVoiceMessage: Boolean = false,
    val hasAttachment: Boolean = false,
    val isSynced: Boolean = false,
    val voiceMessageInfo: VoiceMessageInfo? = null,
    val fileInfo: FileInfo? = null
) {
    // Конструктор для обратной совместимости
    constructor(
        id: String,
        text: String,
        timestamp: Long,
        isSynced: Boolean = false
    ) : this(
        id = id,
        content = text,
        text = text,
        timestamp = timestamp,
        isSynced = isSynced
    )
}

