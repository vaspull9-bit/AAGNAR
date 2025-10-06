package com.example.aagnar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.example.aagnar.data.local.converters.CallDirectionConverter
import com.example.aagnar.data.local.converters.CallStatusConverter
import com.example.aagnar.domain.model.CallDirection
import com.example.aagnar.domain.model.CallStatus

@Entity(tableName = "calls")
@TypeConverters(CallDirectionConverter::class, CallStatusConverter::class)
data class CallEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "contactId")
    val contactId: Long,

    @ColumnInfo(name = "direction")
    val direction: CallDirection,

    @ColumnInfo(name = "status")
    val status: CallStatus,

    @ColumnInfo(name = "startTime")
    val startTime: Long,

    @ColumnInfo(name = "endTime", defaultValue = "0")
    val endTime: Long = 0,

    @ColumnInfo(name = "isVideo", defaultValue = "0")
    val isVideo: Boolean = false,

    @ColumnInfo(name = "recordPath")
    val recordPath: String? = null
)