package com.example.aagnar.domain.service

import com.example.aagnar.security.KeyManager
import com.example.aagnar.util.EncryptionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.crypto.SecretKey
import javax.inject.Inject

class EncryptionService @Inject constructor(
    private val keyManager: KeyManager
) {

    suspend fun setupSecureChat(contactName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Генерируем ключ для контакта
            val secretKey = keyManager.generateContactKey(contactName)

            // Сохраняем ключ локально
            keyManager.saveContactKey(contactName, secretKey)

            // TODO: Обменяться ключами через WebSocket
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun encryptMessage(contactName: String, message: String): String? = withContext(Dispatchers.IO) {
        try {
            val secretKey = keyManager.loadContactKey(contactName)
            secretKey?.let {
                EncryptionUtils.encryptAES(message, it)
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun decryptMessage(contactName: String, encryptedMessage: String): String? = withContext(Dispatchers.IO) {
        try {
            val secretKey = keyManager.loadContactKey(contactName)
            secretKey?.let {
                EncryptionUtils.decryptAES(encryptedMessage, it)
            }
        } catch (e: Exception) {
            "🔒 [Не удалось расшифровать сообщение]"
        }
    }

    fun hasEncryptionKey(contactName: String): Boolean {
        return keyManager.loadContactKey(contactName) != null
    }
}