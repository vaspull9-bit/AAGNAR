package com.example.aagnar.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    fun createAccount(
        username: String,
        password: String,
        server: String? = null,
        protocol: com.example.aagnar.domain.model.ProtocolType = com.example.aagnar.domain.model.ProtocolType.SIP,
        audioCodec: com.example.aagnar.domain.model.AudioCodec = com.example.aagnar.domain.model.AudioCodec.OPUS,
        videoCodec: com.example.aagnar.domain.model.VideoCodec = com.example.aagnar.domain.model.VideoCodec.VP8,
        proxyServer: String? = null,
        proxyPort: Int? = null,
        enableVideo: Boolean = true,
        enableEncryption: Boolean = true
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // TODO: Реальная логика создания аккаунта
                kotlinx.coroutines.delay(1000)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    accountCreated = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}

data class AccountUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val accountCreated: Boolean = false
)