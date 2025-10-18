package com.example.aagnar.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aagnar.domain.service.P2PSignalingClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class P2PViewModel @Inject constructor(
    private val p2pClient: P2PSignalingClient
) : ViewModel() {

    val connectionState: StateFlow<String> = p2pClient.connectionState
    val messages: StateFlow<List<String>> = p2pClient.messages

    fun connect() {
        p2pClient.connectToServer("192.168.88.240")
    }

    fun sendMessage(message: String) {
        p2pClient.sendMessage(message)
    }

    fun disconnect() {
        p2pClient.disconnect()
    }
}