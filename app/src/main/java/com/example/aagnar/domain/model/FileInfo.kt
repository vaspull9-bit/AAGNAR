package com.example.aagnar.domain.model

import android.net.Uri

data class FileInfo(
    val name: String,
    val size: Long,
    val type: String,
    val uri: Uri,
    val transferProgress: Int = 0
)