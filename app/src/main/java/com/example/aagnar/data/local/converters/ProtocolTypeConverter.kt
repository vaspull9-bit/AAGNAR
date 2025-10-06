// data/local/converters/ProtocolTypeConverter.kt
package com.example.aagnar.data.local.converters

import androidx.room.TypeConverter
import com.example.aagnar.domain.model.ProtocolType

class ProtocolTypeConverter {
    @TypeConverter
    fun fromProtocolType(protocolType: ProtocolType): String {
        return protocolType.name
    }

    @TypeConverter
    fun toProtocolType(value: String): ProtocolType {
        return ProtocolType.valueOf(value)
    }
}