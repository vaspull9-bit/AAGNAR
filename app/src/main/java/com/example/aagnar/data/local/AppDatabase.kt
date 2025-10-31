package com.example.aagnar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.aagnar.data.local.dao.CallDao
import com.example.aagnar.data.local.dao.ContactDao
import com.example.aagnar.data.local.dao.MessageDao
import com.example.aagnar.data.local.entity.CallEntity
import com.example.aagnar.data.local.entity.ContactEntity
import com.example.aagnar.data.local.entity.MessageEntity
import com.example.aagnar.data.local.converters.CallStatusConverter
import com.example.aagnar.data.local.converters.MessageTypeConverter
import com.example.aagnar.data.local.converters.DateConverter

@Database(
    entities = [MessageEntity::class, ContactEntity::class, CallEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(MessageTypeConverter::class, CallStatusConverter::class, DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao
    abstract fun callDao(): CallDao
}