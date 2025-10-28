package com.example.aagnar.domain.service

import android.content.Context
import android.net.Uri
import com.example.aagnar.data.repository.WebSocketRepository
import com.example.aagnar.domain.model.FileInfo
import com.example.aagnar.util.FileManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FileTransferService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val webSocketRepository: WebSocketRepository
) {

    suspend fun sendFile(toUser: String, fileUri: Uri, onProgress: (Int) -> Unit = {}): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val fileInfo = FileManager.getFileInfo(context, fileUri)
                if (fileInfo == null) {
                    return@withContext false
                }

                val chunks = FileManager.readFileInChunks(context, fileUri)
                val totalChunks = chunks.size

                val sendingFileInfo = FileInfo(
                    name = fileInfo.name,
                    size = fileInfo.size,
                    type = fileInfo.type,
                    uri = fileUri
                )

                // Отправляем chunks по одному
                chunks.forEachIndexed { index, chunk ->
                    sendFileChunk(  // ← ИСПРАВЛЕНО: используем метод этого класса
                        toUser = toUser,
                        fileInfo = sendingFileInfo,
                        chunkData = chunk,
                        chunkIndex = index,
                        totalChunks = totalChunks
                    )

                    // Обновляем прогресс
                    val progress = ((index + 1) * 100 / totalChunks)
                    onProgress(progress)

                    // Небольшая задержка между chunks
                    Thread.sleep(50)
                }

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun sendFileChunk(
        toUser: String,
        fileInfo: FileInfo,
        chunkData: ByteArray,
        chunkIndex: Int,
        totalChunks: Int
    ) {
        webSocketRepository.sendFileChunk(toUser, fileInfo, chunkData, chunkIndex, totalChunks)  // ← ИСПРАВЛЕНО: используем webSocketRepository вместо webSocketClient
    }

    suspend fun receiveFile(chunks: List<ByteArray>, fileName: String): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                FileManager.saveFileFromChunks(context, chunks, fileName)
            } catch (e: Exception) {
                null
            }
        }
    }
}