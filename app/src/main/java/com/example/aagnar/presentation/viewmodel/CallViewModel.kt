// CallViewModel.kt
package com.example.aagnar.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aagnar.domain.usecase.MatrixUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    application: Application,
    private val matrixUseCase: MatrixUseCase  // 🔥 ЗАМЕНИЛИ НА MATRIX
) : AndroidViewModel(application) {

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _isVideoOn = MutableStateFlow(false)
    val isVideoOn: StateFlow<Boolean> = _isVideoOn.asStateFlow()

    private val _callDuration = MutableStateFlow(0L)
    val callDuration: StateFlow<Long> = _callDuration.asStateFlow()

    private val _callStatus = MutableStateFlow("IDLE")
    val callStatus: StateFlow<String> = _callStatus.asStateFlow()

    ///////////////////////////////////////////////////
    //
    //            ФУНКЦИИ ДЛЯ MATRIX ЗВОНКОВ
    //
    ////////////////////////////////////////////////////

    fun updateCallStatus() {
        viewModelScope.launch {
            // TODO: Получить реальный статус из Matrix
            _callStatus.value = "ACTIVE" // временная заглушка
        }
    }

    fun holdCall() {
        viewModelScope.launch {
            // TODO: Matrix hold call
            println("Matrix hold call")
            updateCallStatus()
        }
    }

    fun unholdCall() {
        viewModelScope.launch {
            // TODO: Matrix unhold call
            println("Matrix unhold call")
            updateCallStatus()
        }
    }

    fun makeCall(contactAddress: String, isVideoCall: Boolean) {
        viewModelScope.launch {
            _callState.value = CallState.Connecting(contactAddress, isVideoCall)
            // 🔥 MATRIX ЗВОНОК
            matrixUseCase.startCall(contactAddress, isVideoCall)
            // НЕ переходим сразу в Active - ждем callback от Matrix
        }
    }

    fun endCall() {
        viewModelScope.launch {
            _callState.value = CallState.Disconnected
            _callDuration.value = 0L
            // 🔥 MATRIX END CALL
            matrixUseCase.endCall("current_peer") // TODO: получить текущий peer
            updateCallStatus()
        }
    }

    fun toggleMute() {
        viewModelScope.launch {
            _isMuted.value = !_isMuted.value
            // TODO: Реальное управление микрофоном через Matrix WebRTC
            println("Matrix mute: ${_isMuted.value}")
        }
    }

    fun toggleSpeaker() {
        viewModelScope.launch {
            _isSpeakerOn.value = !_isSpeakerOn.value
            // TODO: Реальное переключение динамика через Matrix
            println("Matrix speaker: ${_isSpeakerOn.value}")
        }
    }

    fun toggleVideo() {
        viewModelScope.launch {
            _isVideoOn.value = !_isVideoOn.value
            // TODO: Реальное управление видео через Matrix
            println("Matrix video: ${_isVideoOn.value}")
        }
    }

    private fun startCallTimer() {
        viewModelScope.launch {
            while (_callState.value is CallState.Active) {
                kotlinx.coroutines.delay(1000)
                _callDuration.value += 1
            }
        }
    }

    fun answerCall(caller: String, isVideo: Boolean) {
        viewModelScope.launch {
            _callState.value = CallState.Active(caller, isVideo)
            // 🔥 MATRIX ANSWER CALL
            matrixUseCase.answerCall(caller)
            startCallTimer()
        }
    }

    // 🔥 НОВЫЕ МЕТОДЫ ДЛЯ MATRIX
    fun startMatrixCall(peerId: String, isVideo: Boolean) {
        viewModelScope.launch {
            matrixUseCase.startCall(peerId, isVideo)
        }
    }

    fun endMatrixCall() {
        viewModelScope.launch {
            matrixUseCase.endCall("current_peer") // TODO
        }
    }
}

sealed class CallState {
    object Idle : CallState()
    data class Connecting(val contactAddress: String, val isVideo: Boolean) : CallState()
    data class Active(val contactAddress: String, val isVideo: Boolean) : CallState()
    data class Incoming(val caller: String, val isVideo: Boolean) : CallState()
    object Disconnected : CallState()
}