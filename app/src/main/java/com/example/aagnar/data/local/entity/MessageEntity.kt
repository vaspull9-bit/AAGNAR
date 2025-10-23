package com.example.aagnar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.example.aagnar.data.local.converters.MessageTypeConverter
import com.example.aagnar.domain.model.MessageType

@Entity(
    tableName = "message",
    indices = [
        Index(value = ["contactName", "timestamp"], unique = false),
        Index(value = ["timestamp"], unique = false),
        Index(value = ["isDelivered"], unique = false)
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "contactName")
    val contactName: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "timestamp", index = true)
    val timestamp: Date,

    @ColumnInfo(name = "isDelivered", index = true)
    val isDelivered: Boolean = false,

    @ColumnInfo(name = "isRead")
    val isRead: Boolean = false,

    @ColumnInfo(name = "isEncrypted")
    val isEncrypted: Boolean = false
) {
    fun toDomain(): Message {
        return Message(
            id = id,
            contactName = contactName,
            content = content,
            timestamp = timestamp,
            type = MessageType.RECEIVED, // Определяется в бизнес-логике
            isDelivered = isDelivered,
            isRead = isRead,
            isEncrypted = isEncrypted
        )
    }
}