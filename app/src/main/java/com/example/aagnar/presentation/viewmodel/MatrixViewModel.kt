// presentation/viewmodel/MatrixViewModel.kt
package com.example.aagnar.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aagnar.domain.usecase.MatrixUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.File  // 🔥 ДОБАВЬ ЭТОТ ИМПОРТ!

@HiltViewModel
class MatrixViewModel @Inject constructor(
    private val matrixUseCase: MatrixUseCase
) : ViewModel() {

    val connectionState = matrixUseCase.connectionState
    val messages = matrixUseCase.messages
    val callState = matrixUseCase.callState


    init {
        // 🔥 ИНИЦИАЛИЗИРУЕМ ПРИ СОЗДАНИИ ViewModel
        initializeMatrix()
    }

    fun initializeMatrix() {
        viewModelScope.launch {
            matrixUseCase.initializeMatrix()
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            matrixUseCase.login(username, password)
        }
    }

    // ДОБАВЬ ЭТОТ МЕТОД:
    fun sendFile(peerId: String, file: File, mimeType: String) {
        viewModelScope.launch {
            matrixUseCase.sendFile(peerId, file, mimeType)
        }
    }

    fun register(username: String, password: String, displayName: String) {
        viewModelScope.launch {
            matrixUseCase.register(username, password, displayName)
        }
    }

    fun sendMessage(peerId: String, text: String) {
        viewModelScope.launch {
            matrixUseCase.sendMessage(peerId, text)
        }
    }

    fun startCall(peerId: String, isVideo: Boolean) {
        viewModelScope.launch {
            matrixUseCase.startCall(peerId, isVideo)
        }
    }
}