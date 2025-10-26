package com.example.aagnar.domain.model

data class VoiceMessageInfo(
    val duration: Int,
    val filePath: String? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Int = 0
)