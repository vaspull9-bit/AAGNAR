package com.example.aagnar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index
import java.util.Date

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["contactName", "timestamp"]),
        Index(value = ["timestamp"]),
        Index(value = ["isDelivered"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "contactName")
    val contactName: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Date,

    @ColumnInfo(name = "type")
    val type: String, // ← ДОБАВИТЬ: "SENT", "RECEIVED", "SYSTEM"

    @ColumnInfo(name = "isDelivered")
    val isDelivered: Boolean = false,

    @ColumnInfo(name = "isRead")
    val isRead: Boolean = false,

    @ColumnInfo(name = "isEncrypted")
    val isEncrypted: Boolean = false,

    @ColumnInfo(name = "isVoiceMessage")
    val isVoiceMessage: Boolean = false, // ← ДОБАВИТЬ

    @ColumnInfo(name = "hasAttachment")
    val hasAttachment: Boolean = false, // ← ДОБАВИТЬ

    @ColumnInfo(name = "filePath")
    val filePath: String = "", // ← ДОБАВИТЬ: для хранения URI файла

    @ColumnInfo(name = "voiceDuration")
    val voiceDuration: Int = 0 // ← ДОБАВИТЬ: длительность голосового сообщения
)