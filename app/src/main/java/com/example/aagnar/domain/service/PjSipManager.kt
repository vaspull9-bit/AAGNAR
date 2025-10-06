package com.example.aagnar.domain.service

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PjSipManager(private val context: Context) {

    suspend fun initializeSip() = withContext(Dispatchers.IO) {
        // Заглушка вместо реальной инициализации PJSIP
        println("PJSIP stub initialized")
    }

    suspend fun createAccount(username: String, password: String, domain: String) = withContext(Dispatchers.IO) {
        println("Account created: $username@$domain")
    }

    suspend fun makeCall(uri: String) = withContext(Dispatchers.IO) {
        println("Calling $uri (PJSIP stub)")
        // Закомментировать реальный PJSIP код пока не настроена библиотека
        // val call = Call(acc, -1)
        // val prm = CallOpParam(true)
        // call.makeCall(uri, prm)
    }
}
