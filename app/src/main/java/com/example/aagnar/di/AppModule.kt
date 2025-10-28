package com.example.aagnar.di

import android.content.Context
import androidx.work.WorkManager
import com.example.aagnar.data.local.AppDatabase
import com.example.aagnar.data.local.dao.ContactDao
import com.example.aagnar.data.local.dao.MessageDao
import com.example.aagnar.data.repository.ContactRepositoryImpl
import com.example.aagnar.data.repository.GroupRepositoryImpl
import com.example.aagnar.data.repository.MessageRepositoryImpl
import com.example.aagnar.data.repository.SettingsRepository
import com.example.aagnar.data.repository.WebSocketRepository
import com.example.aagnar.domain.repository.ContactRepository
import com.example.aagnar.domain.repository.GroupRepository
import com.example.aagnar.domain.repository.MessageRepository
import com.example.aagnar.security.KeyManager
import com.example.aagnar.domain.repository.WebSocketRepository as DomainWebSocketRepository
import com.example.aagnar.util.AudioPlayer
import com.example.aagnar.util.AudioRecorder
import com.example.aagnar.util.PerformanceMonitor
import com.example.aagnar.webrtc.SignalingClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(): AudioPlayer {
        return AudioPlayer()
    }

    @Provides
    @Singleton
    fun provideAudioRecorder(@ApplicationContext context: Context): AudioRecorder {
        return AudioRecorder(context)
    }

    @Provides
    @Singleton
    fun provideWebSocketRepository(
        @ApplicationContext context: Context
    ): DomainWebSocketRepository {
        return WebSocketRepository(context)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(
        messageDao: MessageDao  // Добавьте зависимость MessageDao
    ): MessageRepository {
        return MessageRepositoryImpl(messageDao)
    }

    @Provides
    @Singleton
    fun provideSignalingClient(
        @ApplicationContext context: Context,
        webSocketRepository: DomainWebSocketRepository
    ): SignalingClient {
        return SignalingClient(context, webSocketRepository)
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    @Singleton
    fun provideKeyManager(@ApplicationContext context: Context): KeyManager {
        return KeyManager(context)
    }

// Hilt автоматически создаст ContactRepositoryImpl благодаря @Inject конструктору!
    @Provides
    @Singleton
    fun provideContactRepository(
        contactDao: ContactDao
    ): ContactRepository {
        return ContactRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideGroupRepository(): GroupRepository {
        return GroupRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun providePerformanceMonitor(): PerformanceMonitor {
        return PerformanceMonitor()
    }

}