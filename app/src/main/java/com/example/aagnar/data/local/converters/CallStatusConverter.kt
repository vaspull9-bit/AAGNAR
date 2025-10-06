package com.example.aagnar.data.local.converters

import androidx.room.TypeConverter
import com.example.aagnar.domain.model.CallStatus

class CallStatusConverter {
    @TypeConverter
    fun fromCallStatus(status: CallStatus): String = status.name

    @TypeConverter
    fun toCallStatus(value: String): CallStatus = CallStatus.valueOf(value)
}