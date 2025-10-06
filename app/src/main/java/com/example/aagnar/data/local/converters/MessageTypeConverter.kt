// data/local/converters/MessageTypeConverter.kt
package com.example.aagnar.data.local.converters

import androidx.room.TypeConverter
import com.example.aagnar.domain.model.MessageType

class MessageTypeConverter {
    @TypeConverter
    fun fromMessageType(messageType: MessageType): String {
        return messageType.name
    }

    @TypeConverter
    fun toMessageType(value: String): MessageType {
        return MessageType.valueOf(value)
    }
}