// AagnarApplication.kt
package com.example.aagnar

import android.app.Application
import com.example.aagnar.domain.service.LinphoneService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AagnarApplication : Application() {

    @Inject
    lateinit var linphoneService: LinphoneService

    override fun onCreate() {
        super.onCreate()
        linphoneService.initialize()
    }
}

