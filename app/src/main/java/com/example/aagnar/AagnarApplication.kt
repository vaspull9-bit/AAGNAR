// AagnarApplication.kt
package com.example.aagnar

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AagnarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        println("AagnarApplication: Started with Matrix 1.6.36")
        // 🔥 УБИРАЕМ WorkManager инициализацию - пусть Hilt сам разбирается
    }
}