package com.example.aagnar.presentation.ui.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aagnar.webrtc.SignalingClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val signalingClient: SignalingClient
) : ViewModel() {

    fun getSignalingClient(): SignalingClient {
        return signalingClient
    }

    fun getCallState(): StateFlow<SignalingClient.CallState> {
        return signalingClient.callState
    }

    override fun onCleared() {
        super.onCleared()
        // Очищаем ресурсы если нужно
    }
}