package com.example.aagnar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.example.aagnar.data.local.converters.MessageTypeConverter
import com.example.aagnar.domain.model.MessageType

@Entity(tableName = "messages")
@TypeConverters(MessageTypeConverter::class)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "contactId")
    val contactId: Long,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "isOutgoing", defaultValue = "0")
    val isOutgoing: Boolean = false,

    @ColumnInfo(name = "type", defaultValue = "TEXT")
    val type: MessageType = MessageType.TEXT,

    @ColumnInfo(name = "filePath")
    val filePath: String? = null,

    @ColumnInfo(name = "isDelivered", defaultValue = "0")
    val isDelivered: Boolean = false
)