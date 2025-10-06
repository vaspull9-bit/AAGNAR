package com.example.aagnar.di

import android.content.Context
import com.example.aagnar.data.local.AppDatabase
import com.example.aagnar.data.local.dao.AccountDao
import com.example.aagnar.data.repository.AccountRepositoryImpl
import com.example.aagnar.data.repository.SipRepositoryImpl
import com.example.aagnar.domain.repository.AccountRepository
import com.example.aagnar.domain.repository.SipRepository
import com.example.aagnar.domain.service.LinphoneService
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideAccountRepository(accountDao: AccountDao): AccountRepository {
        return AccountRepositoryImpl(accountDao)
    }

    @Provides
    fun provideAccountDao(database: AppDatabase): AccountDao {
        return database.accountDao()
    }

    @Provides
    @Singleton
    fun provideLinphoneService(@ApplicationContext context: Context): LinphoneService {
        return LinphoneService(context)
    }

    @Provides
    @Singleton
    fun provideSipRepository(linphoneService: LinphoneService): SipRepository {
        return SipRepositoryImpl(linphoneService)
    }

    // TODO: Добавить остальные репозитории
}