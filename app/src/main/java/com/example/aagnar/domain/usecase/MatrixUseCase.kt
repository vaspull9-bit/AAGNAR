// domain/usecase/MatrixUseCase.kt
package com.example.aagnar.domain.usecase

import com.example.aagnar.domain.repository.MatrixRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import java.io.File  // 🔥 ДОБАВЬ ЭТОТ ИМПОРТ!

class MatrixUseCase @Inject constructor(
    private val matrixRepository: MatrixRepository
) {




    suspend fun initializeMatrix() {
        matrixRepository.initialize()
        // Будет вызывать initialize() из MatrixService
    }

    suspend fun login(username: String, password: String): Boolean {
        return matrixRepository.login(username, password)
    }

    suspend fun register(username: String, password: String, displayName: String): Boolean {
        return matrixRepository.register(username, password, displayName)
    }

    // ДОБАВЬ ЭТОТ МЕТОД:
    suspend fun sendFile(peerId: String, file: File, mimeType: String) {
        matrixRepository.sendFile(peerId, file, mimeType)
    }

    suspend fun sendMessage(peerId: String, text: String) {
        matrixRepository.sendMessage(peerId, text)
    }

    suspend fun startCall(peerId: String, isVideo: Boolean) {
        matrixRepository.startCall(peerId, isVideo)
    }

    // 🔥 ДОБАВЬ ЭТОТ МЕТОД:
    suspend fun endCall(peerId: String) {
        matrixRepository.endCall(peerId)
    }

    // 🔥 ДОБАВЬ ЭТОТ МЕТОД:
    suspend fun answerCall(peerId: String) {
        matrixRepository.answerCall(peerId)
    }

    val connectionState: StateFlow<com.example.aagnar.domain.service.MatrixState>
        get() = matrixRepository.connectionState

    val messages: StateFlow<List<com.example.aagnar.domain.service.MatrixMessage>>
        get() = matrixRepository.messages

    val callState: StateFlow<com.example.aagnar.domain.service.CallState>
        get() = matrixRepository.callState
}