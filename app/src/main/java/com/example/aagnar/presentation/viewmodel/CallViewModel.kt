package com.example.aagnar.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CallViewModel(application: Application) : AndroidViewModel(application) {

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

    fun makeCall(contactAddress: String, isVideoCall: Boolean) {
        viewModelScope.launch {
            _callState.value = CallState.Connecting(contactAddress, isVideoCall)
            // Симуляция установки соединения
            kotlinx.coroutines.delay(2000)
            _callState.value = CallState.Active(contactAddress, isVideoCall)
            startCallTimer()
        }
    }

    fun endCall() {
        viewModelScope.launch {
            _callState.value = CallState.Disconnected
            _callDuration.value = 0L
        }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    fun toggleSpeaker() {
        _isSpeakerOn.value = !_isSpeakerOn.value
    }

    fun toggleVideo() {
        _isVideoOn.value = !_isVideoOn.value
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