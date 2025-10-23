package com.example.aagnar.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.aagnar.util.EncryptionUtils
import java.security.KeyStore
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class KeyManager(private val context: Context) {

    private val keyAlias = "aagnar_master_key"
    private val sharedPrefsName = "encrypted_prefs"

    // Получение зашифрованных SharedPreferences
    private fun getEncryptedPreferences(): EncryptedSharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return EncryptedSharedPreferences.create(
            sharedPrefsName,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    // Сохранение ключа для контакта
    fun saveContactKey(contactName: String, secretKey: SecretKey) {
        val prefs = getEncryptedPreferences()
        val keyString = Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)
        prefs.edit().putString("key_$contactName", keyString).apply()
    }

    // Загрузка ключа для контакта
    fun loadContactKey(contactName: String): SecretKey? {
        val prefs = getEncryptedPreferences()
        val keyString = prefs.getString("key_$contactName", null)
        return keyString?.let {
            val keyBytes = Base64.decode(it, Base64.DEFAULT)
            SecretKeySpec(keyBytes, "AES")
        }
    }

    // Генерация и сохранение ключа для нового контакта
    fun generateContactKey(contactName: String): SecretKey {
        val secretKey = EncryptionUtils.generateAESKey()
        saveContactKey(contactName, secretKey)
        return secretKey
    }

    // Сохранение публичного ключа пользователя
    fun saveUserPublicKey(publicKey: String) {
        val prefs = getEncryptedPreferences()
        prefs.edit().putString("user_public_key", publicKey).apply()
    }

    // Загрузка публичного ключа пользователя
    fun getUserPublicKey(): String? {
        val prefs = getEncryptedPreferences()
        return prefs.getString("user_public_key", null)
    }

    // Сохранение приватного ключа пользователя
    fun saveUserPrivateKey(privateKey: String) {
        val prefs = getEncryptedPreferences()
        prefs.edit().putString("user_private_key", privateKey).apply()
    }

    // Загрузка приватного ключа пользователя
    fun getUserPrivateKey(): String? {
        val prefs = getEncryptedPreferences()
        return prefs.getString("user_private_key", null)
    }

    // Очистка всех ключей (при logout)
    fun clearAllKeys() {
        val prefs = getEncryptedPreferences()
        prefs.edit().clear().apply()
    }
}