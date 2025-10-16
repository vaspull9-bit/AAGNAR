// C:\Users\trii\AndroidStudioProjects\AAGNAR\app\src\main\java\com\example\aagnar\data\repository\MatrixRepositoryImpl.kt

package com.example.aagnar.data.repository

import com.example.aagnar.domain.repository.MatrixRepository
import com.example.aagnar.domain.service.CallState
import com.example.aagnar.domain.service.MatrixMessage
import com.example.aagnar.domain.service.MatrixService
import com.example.aagnar.domain.service.MatrixState
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

class MatrixRepositoryImpl @Inject constructor(
    private val matrixService: MatrixService
) : MatrixRepository {

    override suspend fun initialize() {
        matrixService.initialize()  // ← ВЫЗОВЕТ initialize()
    }
    // ДОБАВЬ ЭТОТ МЕТОД:
    override suspend fun register(username: String, password: String, displayName: String): Boolean {
        return matrixService.register(username, password, displayName)
    }

    override suspend fun login(username: String, password: String): Boolean {
        return matrixService.login(username, password)
    }

    override suspend fun sendMessage(peerId: String, text: String) {
        matrixService.sendMessage(peerId, text)
    }

    override suspend fun sendFile(peerId: String, file: File, mimeType: String) {
        matrixService.sendFile(peerId, file, mimeType)
    }

    override suspend fun startCall(peerId: String, isVideo: Boolean) {
        matrixService.startCall(peerId, isVideo)
    }

    override suspend fun answerCall(peerId: String) {
        matrixService.answerCall(peerId)
    }

    override suspend fun endCall(peerId: String) {
        matrixService.endCall(peerId)
    }

    override val connectionState: StateFlow<MatrixState>
        get() = matrixService.connectionState

    override val messages: StateFlow<List<MatrixMessage>>
        get() = matrixService.messages

    override val callState: StateFlow<CallState>
        get() = matrixService.callState
}