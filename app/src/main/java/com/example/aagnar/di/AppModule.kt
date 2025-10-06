// di/AppModule.kt
package com.example.aagnar.di

import android.content.Context
import com.example.aagnar.data.local.AppDatabase
import com.example.aagnar.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.aagnar.data.local.dao.AccountDao
import com.example.aagnar.domain.repository.AccountRepository
import com.example.aagnar.data.repository.AccountRepositoryImpl

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


    // TODO: Добавить остальные репозитории
}