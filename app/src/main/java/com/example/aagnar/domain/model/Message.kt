package com.example.aagnar.domain.model

import android.net.Uri
import java.util.*

data class Message(
    val id: String,
    val contactName: String,
    val content: String,
    val timestamp: Date,
    val type: MessageType,
    val isDelivered: Boolean = false,
    val isRead: Boolean = false,
    val isEncrypted: Boolean = false,
    val encryptionKeyId: String? = null,
    val hasAttachment: Boolean = false,
    val fileInfo: FileInfo? = null,
    // Новые поля для голосовых сообщений
    val isVoiceMessage: Boolean = false,
    val voiceMessageInfo: VoiceMessageInfo? = null
)

data class VoiceMessageInfo(
    val duration: Int, // в секундах
    val filePath: String? = null, // локальный путь к файлу
    val audioData: String? = null, // base64 encoded audio data
    val isPlaying: Boolean = false,
    val currentPosition: Int = 0
)