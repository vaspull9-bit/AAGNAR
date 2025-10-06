package com.example.aagnar.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aagnar.domain.repository.SipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    application: Application,
    private val sipRepository: SipRepository
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
    //            Функции
    //
    //
    ////////////////////////////////////////////////////

    fun updateCallStatus() {
        viewModelScope.launch {
            // TODO: Получить реальный статус из SIP
            _callStatus.value = "ACTIVE" // временная заглушка
        }
    }

    fun holdCall() {
        viewModelScope.launch {
            sipRepository.holdCall()
            updateCallStatus()
        }
    }

    fun unholdCall() {
        viewModelScope.launch {
            sipRepository.unholdCall()
            updateCallStatus()
        }
    }



    fun makeCall(contactAddress: String, isVideoCall: Boolean) {
        viewModelScope.launch {
            _callState.value = CallState.Connecting(contactAddress, isVideoCall)
            sipRepository.makeCall(contactAddress) // Реальный вызов через Linphone!
            _callState.value = CallState.Active(contactAddress, isVideoCall)
            startCallTimer()
        }
    }

    fun endCall() {
        viewModelScope.launch {
            _callState.value = CallState.Disconnected
            _callDuration.value = 0L
            sipRepository.endCall()
            updateCallStatus() // ДОБАВЬ ЭТУ СТРОКУ
        }
    }

    fun toggleMute() {
        viewModelScope.launch {
            _isMuted.value = !_isMuted.value
            // TODO: Реальное управление микрофоном через SIP
            if (_isMuted.value) {
                sipRepository.muteMicrophone()
            } else {
                sipRepository.unmuteMicrophone()
            }
        }
    }

    fun toggleSpeaker() {
        viewModelScope.launch {
            _isSpeakerOn.value = !_isSpeakerOn.value
            // TODO: Реальное переключение динамика
            sipRepository.toggleSpeaker(_isSpeakerOn.value)
        }
    }

    fun toggleVideo() {
        viewModelScope.launch {
            _isVideoOn.value = !_isVideoOn.value
            // TODO: Реальное управление видео через SIP
            if (_isVideoOn.value) {
                sipRepository.enableVideo()
            } else {
                sipRepository.disableVideo()
            }
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
            // TODO: Реальный ответ на звонок через SIP
            sipRepository.answerCall()
            startCallTimer()
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