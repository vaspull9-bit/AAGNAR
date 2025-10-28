package com.example.aagnar.presentation.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.example.aagnar.domain.model.FileInfo
import com.example.aagnar.domain.model.Message
import com.example.aagnar.domain.model.MessageType
import com.example.aagnar.domain.model.VoiceMessageInfo
import com.example.aagnar.domain.repository.MessageRepository
import com.example.aagnar.domain.repository.WebSocketRepository
import com.example.aagnar.domain.service.EncryptionService
import com.example.aagnar.domain.service.FileTransferService
import com.example.aagnar.util.AudioRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val webSocketRepository: WebSocketRepository,
    private val encryptionService: EncryptionService,
    private val fileTransferService: FileTransferService,
    private val audioRecorder: AudioRecorder,
    @ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _isConnected = MutableLiveData<Boolean>(false)
    val isConnected: LiveData<Boolean> = _isConnected

    private val _isTyping = MutableLiveData<Boolean>(false)
    val isTyping: LiveData<Boolean> = _isTyping

    private val _fileTransferProgress = MutableLiveData<Pair<String, Int>>() // fileId to progress
    val fileTransferProgress: LiveData<Pair<String, Int>> = _fileTransferProgress

    private val _receivedFiles = MutableLiveData<List<FileInfo>>()
    val receivedFiles: LiveData<List<FileInfo>> = _receivedFiles

    private var currentContact: String = ""

    init {
        observeWebSocket()
    }

    // Методы для голосовых сообщений
    fun sendVoiceMessage(contactName: String, audioData: ByteArray, duration: Int, messageId: String? = null) {
        viewModelScope.launch {
            val finalMessageId = messageId ?: UUID.randomUUID().toString()

            val newMessage = Message(
                id = finalMessageId,
                contactName = contactName,
                content = "🎤 Голосовое сообщение",
                timestamp = System.currentTimeMillis(),
                type = MessageType.SENT,
                isVoiceMessage = true,
                voiceMessageInfo = VoiceMessageInfo(
                    duration = duration,
                    audioData = audioData
                )
            )

            // Сохраняем в базу
            messageRepository.insertMessage(newMessage)

            // Отправляем через WebSocket
            webSocketRepository.sendVoiceMessage(contactName, audioData, duration, finalMessageId)

            // Обновляем UI
            val currentMessages = _messages.value.orEmpty().toMutableList()
            currentMessages.add(newMessage)
            _messages.value = currentMessages
        }
    }

    fun saveReceivedVoiceMessage(messageId: String, audioData: String, duration: Int): String? {
        return try {
            val fileName = "voice_${System.currentTimeMillis()}.m4a"
            val audioFile = audioRecorder.decodeAudioFromBase64(audioData, fileName)
            audioFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    // Метод для отправки файла
    fun sendFile(contactName: String, fileUri: android.net.Uri) {
        viewModelScope.launch {
            val fileInfo = com.example.aagnar.util.FileManager.getFileInfo(context, fileUri)
            if (fileInfo != null) {
                val messageId = UUID.randomUUID().toString()
                val newMessage = Message(
                    id = messageId,
                    contactName = contactName,
                    content = "📎 ${fileInfo.name}",
                    timestamp = System.currentTimeMillis(),
                    type = MessageType.SENT,
                    hasAttachment = true,
                    fileInfo = FileInfo(
                        name = fileInfo.name,
                        size = fileInfo.size,
                        type = fileInfo.type,
                        uri = fileUri,
                        transferProgress = 0
                    )
                )

                // Сохраняем в базу
                messageRepository.insertMessage(newMessage)

                // Обновляем UI
                val currentMessages = _messages.value.orEmpty().toMutableList()
                currentMessages.add(newMessage)
                _messages.value = currentMessages

                // Начинаем передачу файла
                val success = fileTransferService.sendFile(contactName, fileUri) { progress ->
                    _fileTransferProgress.postValue(Pair(messageId, progress))
                }

                if (!success) {
                    // TODO: Показать ошибку
                }
            }
        }
    }

    // Метод для получения файла
    fun downloadFile(messageId: String, fileInfo: FileInfo) {
        viewModelScope.launch {
            fileInfo.uri?.let { uri ->
                // Открываем файл
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, fileInfo.type)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(intent)
            }
        }
    }

    fun loadMessages(contactName: String) {
        currentContact = contactName
        viewModelScope.launch {
            // Загружаем сообщения из базы данных
            val dbMessages = messageRepository.getMessagesWithContact(contactName)
            _messages.value = dbMessages

            // Подключаемся к WebSocket если еще не подключены
            if (webSocketRepository.getConnectionState()?.value != true) {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val username = prefs.getString("username", "") ?: ""
                if (username.isNotEmpty()) {
                    webSocketRepository.connect()
                }
            }
        }
    }

    // Обновляем метод sendMessage
    fun sendMessage(contactName: String, content: String) {
        viewModelScope.launch {
            val messageId = UUID.randomUUID().toString()

            // Проверяем есть ли ключ шифрования
            val finalContent = if (encryptionService.hasEncryptionKey(contactName)) {
                // Шифруем сообщение
                encryptionService.encryptMessage(contactName, content) ?: content
            } else {
                content
            }

            val newMessage = Message(
                id = messageId,
                contactName = contactName,
                content = finalContent,
                timestamp = System.currentTimeMillis(),
                type = MessageType.SENT,
                isDelivered = false,
                isEncrypted = encryptionService.hasEncryptionKey(contactName)
            )

            // Сохраняем в локальную базу
            messageRepository.insertMessage(newMessage)

            // Отправляем через WebSocket
            if (encryptionService.hasEncryptionKey(contactName)) {
                webSocketRepository.sendEncryptedMessage(contactName, finalContent, messageId)
            } else {
                // Создать JSON сообщение
                val messageJson = """
    {
        "to": "$contactName",
        "content": "$finalContent", 
        "messageId": "$messageId"
    }
""".trimIndent()

                webSocketRepository.sendMessage(messageJson)
            }

            // Обновляем UI
            val currentMessages = _messages.value.orEmpty().toMutableList()
            currentMessages.add(newMessage)
            _messages.value = currentMessages

            // Отправляем индикатор что перестали печатать
            webSocketRepository.sendTypingIndicator(contactName, false)
        }
    }

    // Добавляем метод для установки безопасного соединения
    fun setupSecureConnection(contactName: String) {
        viewModelScope.launch {
            val success = encryptionService.setupSecureChat(contactName)
            if (success) {
                // Добавляем системное сообщение
                val systemMessage = Message(
                    id = UUID.randomUUID().toString(),
                    contactName = contactName,
                    content = "🔒 Безопасное соединение установлено",
                    timestamp = System.currentTimeMillis(),
                    type = MessageType.TEXT,
                    isDelivered = true
                )

                val currentMessages = _messages.value.orEmpty().toMutableList()
                currentMessages.add(systemMessage)
                _messages.value = currentMessages
            }
        }
    }

    fun setTyping(isTyping: Boolean) {
        if (currentContact.isNotEmpty()) {
            webSocketRepository.sendTypingIndicator(currentContact, isTyping)
        }
    }

    fun markMessageAsRead(messageId: String) {
        viewModelScope.launch {
            messageRepository.markMessageAsRead(messageId)

            if (currentContact.isNotEmpty()) {
                webSocketRepository.sendReadReceipt(currentContact, messageId)
            }
        }
    }

    private fun observeWebSocket() {
        viewModelScope.launch {
            // Наблюдаем за состоянием соединения
            webSocketRepository.getConnectionState()?.collect { connected ->
                _isConnected.value = connected
            }
        }

        viewModelScope.launch {
            // Наблюдаем за входящими сообщениями
            webSocketRepository.observeMessages()?.collect { messageJson ->
                try {
                    // Парсим JSON в объект Message
                    val message = parseMessageFromJson(messageJson)

                    if (message.contactName == currentContact) {
                        // Сохраняем в базу данных
                        viewModelScope.launch {
                            messageRepository.insertMessage(message)
                        }

                        // Обновляем UI
                        val currentMessages = _messages.value.orEmpty().toMutableList()
                        currentMessages.add(message)
                        _messages.value = currentMessages

                        // Помечаем как прочитанные
                        markMessageAsRead(message.id)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun parseMessageFromJson(json: String): Message {
        // TODO: Реализовать парсинг JSON в Message
        // Временная заглушка
        return Message(
            id = UUID.randomUUID().toString(),
            contactName = "unknown",
            content = json,
            timestamp = System.currentTimeMillis(),
            type = MessageType.RECEIVED
        )
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            webSocketRepository.disconnect()
        }
    }
}