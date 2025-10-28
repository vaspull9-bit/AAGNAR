package com.example.aagnar.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aagnar.data.local.AppDatabase
import com.example.aagnar.data.local.dao.ContactDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
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
                Log.d("RoomQuery", "SQL: $sqlQuery, Args: $bindArgs")
            }, Executors.newSingleThreadExecutor())
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .enableMultiInstanceInvalidation()
            .build()
    }

    @Provides
    @Singleton
    fun provideContactDao(database: AppDatabase): ContactDao {
        return database.contactDao()
    }

}