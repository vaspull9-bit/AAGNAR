package com.example.aagnar.di


import android.content.Context
import com.example.aagnar.data.local.AppDatabase
import com.example.aagnar.domain.repository.MatrixRepository
import com.example.aagnar.data.repository.MatrixRepositoryImpl
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }


    @Provides
    @Singleton
    fun provideMatrixService(@ApplicationContext context: Context): MatrixService {
        return MatrixService(context)
    }


    @Provides
    @Singleton
    fun provideMatrixRepository(matrixService: MatrixService): MatrixRepository {
        return MatrixRepositoryImpl(matrixService)
    }
}