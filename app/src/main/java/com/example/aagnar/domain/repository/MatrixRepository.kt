// app/src/main/java/com/example/aagnar/domain/repository/MatrixRepository.kt
package com.example.aagnar.domain.repository

import com.example.aagnar.domain.service.CallState
import com.example.aagnar.domain.service.MatrixMessage
import com.example.aagnar.domain.service.MatrixState
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface MatrixRepository {
    suspend fun initialize()
    suspend fun register(username: String, password: String, displayName: String): Boolean
    suspend fun login(username: String, password: String): Boolean
    suspend fun sendMessage(peerId: String, text: String)
    suspend fun sendFile(peerId: String, file: File, mimeType: String)
    suspend fun startCall(peerId: String, isVideo: Boolean)
    suspend fun answerCall(peerId: String)
    suspend fun endCall(peerId: String)
    val connectionState: StateFlow<MatrixState>
    val messages: StateFlow<List<MatrixMessage>>
    val callState: StateFlow<CallState>
}