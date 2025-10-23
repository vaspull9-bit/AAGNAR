package com.example.aagnar.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.*
import java.util.*

object FileManager {

    private const val CHUNK_SIZE = 16 * 1024 // 16KB chunks

    // Получение информации о файле
    data class FileInfo(
        val name: String,
        val size: Long,
        val type: String,
        val uri: Uri
    )

    fun getFileInfo(context: Context, uri: Uri): FileInfo? {
        return try {
            val contentResolver: ContentResolver = context.contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    val size = it.getLong(it.getColumnIndex(OpenableColumns.SIZE))
                    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

                    FileInfo(
                        name = displayName ?: "unknown",
                        size = size,
                        type = mimeType,
                        uri = uri
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    // Чтение файла по chunks
    fun readFileInChunks(context: Context, uri: Uri): List<ByteArray> {
        val chunks = mutableListOf<ByteArray>()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val buffer = ByteArray(CHUNK_SIZE)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val chunk = if (bytesRead < CHUNK_SIZE) {
                        buffer.copyOf(bytesRead)
                    } else {
                        buffer
                    }
                    chunks.add(chunk)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return chunks
    }

    // Сохранение файла из chunks
    fun saveFileFromChunks(context: Context, chunks: List<ByteArray>, fileName: String): Uri? {
        return try {
            val downloadsDir = File(context.getExternalFilesDir(null), "downloads")
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { outputStream ->
                chunks.forEach { chunk ->
                    outputStream.write(chunk)
                }
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }

    // Получение размера файла в читаемом формате
    fun getReadableFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }

    // Получение иконки для типа файла
    fun getFileIconResId(fileType: String): Int {
        return when {
            fileType.startsWith("image/") -> R.drawable.ic_image
            fileType.startsWith("video/") -> R.drawable.ic_video
            fileType.startsWith("audio/") -> R.drawable.ic_audio
            fileType == "application/pdf" -> R.drawable.ic_pdf
            fileType.contains("word") -> R.drawable.ic_document
            fileType.contains("excel") -> R.drawable.ic_spreadsheet
            fileType.contains("powerpoint") -> R.drawable.ic_presentation
            else -> R.drawable.ic_file
        }
    }
}