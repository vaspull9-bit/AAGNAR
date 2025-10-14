package com.example.aagnar.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.aagnar.data.local.dao.CallDao
import com.example.aagnar.data.local.dao.ContactDao
import com.example.aagnar.data.local.dao.MessageDao
import com.example.aagnar.data.local.entity.CallEntity
import com.example.aagnar.data.local.entity.ContactEntity
import com.example.aagnar.data.local.entity.MessageEntity
import com.example.aagnar.data.local.converters.CallStatusConverter
import com.example.aagnar.data.local.converters.MessageTypeConverter
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        // УДАЛИЛИ AccountEntity::class,
        ContactEntity::class,
        CallEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    // УДАЛИЛИ ProtocolTypeConverter::class,
    MessageTypeConverter::class,
    CallStatusConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    // УДАЛИЛИ abstract fun accountDao(): AccountDao
    abstract fun contactDao(): ContactDao
    abstract fun callDao(): CallDao
    abstract fun messageDao(): MessageDao

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
                    .addCallback(databaseCallback)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val databaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Инициализация базы данных при первом создании
            }
        }
    }
}