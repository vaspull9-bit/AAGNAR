package com.example.aagnar.presentation.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VoIPService : Service() {
    // private lateinit var pjSipManager: PjSipManager
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        // pjSipManager = PjSipManager(this)

        scope.launch {
            // pjSipManager.initializeSip()
            initializeSipStub()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    fun makeCall(uri: String) {
        scope.launch {
            // pjSipManager.makeCall(uri)
            makeCallStub(uri)
        }
    }

    private fun initializeSipStub() {
        println("SIP stub initialized")
    }

    private fun makeCallStub(uri: String) {
        println("Calling $uri (stub)")
    }
}