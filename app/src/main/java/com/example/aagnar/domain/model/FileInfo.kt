package com.example.aagnar.domain.model

import android.net.Uri

data class FileInfo(
    val name: String,
    val size: Long,
    val type: String,
    val uri: Uri? = null, // Сделайте nullable
    val transferProgress: Int = 0,
    val fileId: String = "" // Добавьте для WebSocket совместимости
)