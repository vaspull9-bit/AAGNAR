package com.example.aagnar.data.local.converters

import androidx.room.TypeConverter
import com.example.aagnar.domain.model.CallDirection

class CallDirectionConverter {
    @TypeConverter
    fun fromCallDirection(direction: CallDirection): String = direction.name

    @TypeConverter
    fun toCallDirection(value: String): CallDirection = CallDirection.valueOf(value)
}