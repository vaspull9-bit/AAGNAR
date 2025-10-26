package com.example.aagnar.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import android.util.Log
import com.example.aagnar.data.local.dao.CallDao
import com.example.aagnar.data.local.dao.ContactDao
import com.example.aagnar.data.local.dao.MessageDao
import com.example.aagnar.data.local.entity.CallEntity
import com.example.aagnar.data.local.entity.ContactEntity
import com.example.aagnar.data.local.entity.MessageEntity
import com.example.aagnar.data.local.converters.CallStatusConverter
import com.example.aagnar.data.local.converters.MessageTypeConverter
import com.example.aagnar.data.local.converters.DateConverter
import java.util.concurrent.Executors

@Database(
    entities = [MessageEntity::class, ContactEntity::class, CallEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(MessageTypeConverter::class, CallStatusConverter::class, DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao
    abstract fun callDao(): CallDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aagnar_database"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Предзаполнение базы данных при первом создании
                        }
                    })
                    .setQueryCallback({ sqlQuery, bindArgs ->
                        // Логирование медленных запросов
                        Log.d("RoomQuery", "SQL: $sqlQuery, Args: $bindArgs")
                    }, Executors.newSingleThreadExecutor())
                    .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                    .enableMultiInstanceInvalidation()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}