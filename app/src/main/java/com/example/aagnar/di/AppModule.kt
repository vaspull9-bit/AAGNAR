// app/src/main/java/com/example/aagnar/di/AppModule.kt
package com.example.aagnar.di

import android.content.Context
import com.example.aagnar.data.repository.MatrixRepositoryImpl
import com.example.aagnar.data.repository.SettingsRepository
import com.example.aagnar.domain.repository.MatrixRepository
import com.example.aagnar.domain.service.MatrixService
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
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideMatrixService(
        @ApplicationContext context: Context,
        settingsRepository: SettingsRepository
    ): MatrixService {
        return MatrixService(context, settingsRepository)
    }

    // 🔥 ОБНОВЛЯЕМ ПРОВАЙДЕР ДЛЯ MatrixRepository
    @Provides
    @Singleton
    fun provideMatrixRepository(matrixService: MatrixService): MatrixRepository {
        return MatrixRepositoryImpl(matrixService)
    }
}