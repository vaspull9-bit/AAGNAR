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
import com.example.aagnar.data.local.dao.GroupDao

@Database(
    entities = [Message::class, Contact::class, Chat::class, Group::class, GroupMessage::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao
    abstract fun chatDao(): ChatDao
    abstract fun groupDao(): GroupDao

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
                        if (BuildConfig.DEBUG) {
                            Log.d("RoomQuery", "SQL: $sqlQuery, Args: $bindArgs")
                        }
                    }, Executors.newSingleThreadExecutor())
                    .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                    .enableMultiInstanceInvalidation()
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Миграции для обновления схемы
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Миграция с версии 1 на 2
                database.execSQL("ALTER TABLE message ADD COLUMN is_encrypted INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Миграция с версии 2 на 3
                database.execSQL("CREATE TABLE IF NOT EXISTS `group` (...)")
                database.execSQL("CREATE TABLE IF NOT EXISTS group_message (...)")
            }
        }
    }
}