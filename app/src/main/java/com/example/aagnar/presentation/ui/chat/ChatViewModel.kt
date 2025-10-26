package com.example.aagnar.presentation.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aagnar.domain.model.Message
import com.example.aagnar.domain.model.MessageType
import com.example.aagnar.domain.repository.MessageRepository
import com.example.aagnar.domain.repository.WebSocketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import com.example.aagnar.domain.service.EncryptionService
import com.example.aagnar.domain.service.FileTransferService
import com.example.aagnar.domain.model.VoiceMessageInfo
import com.example.aagnar.domain.model.FileInfo
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val webSocketRepository: WebSocketRepository,
// Добавляем в конструктор
    private val encryptionService: EncryptionService,
    // Добавляем в конструктор
    private val fileTransferService: FileTransferService,
// Добавляем в конструктор
    private val audioViewModel: AudioViewModel

) : ViewModel() {



    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _isConnected = MutableLiveData<Boolean>(false)
    val isConnected: LiveData<Boolean> = _isConnected

    private val _isTyping = MutableLiveData<Boolean>(false)
    val isTyping: LiveData<Boolean> = _isTyping

    private var currentContact: String = ""

    init {
        observeWebSocket()
    }

    // Добавляем методы для голосовых сообщений
    fun sendVoiceMessage(contactName: String, audioData: String, duration: Int) {
        viewModelScope.launch {
            val messageId = UUID.randomUUID().toString()

            val newMessage = Message(
                id = messageId,
                contactName = contactName,
                content = "🎤 Голосовое сообщение",
                timestamp = Date(),
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
            webSocketRepository.sendVoiceMessage(contactName, audioData, duration, messageId)

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


    // Добавляем новые LiveData
    private val _fileTransferProgress = MutableLiveData<Pair<String, Int>>() // fileId to progress
    val fileTransferProgress: LiveData<Pair<String, Int>> = _fileTransferProgress

    private val _receivedFiles = MutableLiveData<List<FileInfo>>()
    val receivedFiles: LiveData<List<FileInfo>> = _receivedFiles

    // Метод для отправки файла
    fun sendFile(contactName: String, fileUri: android.net.Uri) {
        viewModelScope.launch {
            val fileInfo = com.example.aagnar.util.FileManager.getFileInfo(
                androidx.core.content.ContextProvider.getApplicationContext(),
                fileUri
            )

            if (fileInfo != null) {
                val messageId = UUID.randomUUID().toString()
                val newMessage = Message(
                    id = messageId,
                    contactName = contactName,
                    content = "📎 ${fileInfo.name}",
                    timestamp = Date(),
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
            // TODO: Реализовать скачивание файла
            // Пока просто открываем если файл локальный
            fileInfo.uri?.let { uri ->
                // Открываем файл
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, fileInfo.type)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                androidx.core.content.ContextProvider.getApplicationContext().startActivity(intent)
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
                val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(
                    androidx.core.content.ContextProvider.getApplicationContext()
                )
                val username = prefs.getString("username", "") ?: ""
                if (username.isNotEmpty()) {
                    webSocketRepository.connect(username)
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
                timestamp = Date(),
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
                webSocketRepository.sendMessage(contactName, finalContent, messageId)
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
                    timestamp = Date(),
                    type = MessageType.SYSTEM,
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
            webSocketRepository.observeMessages()?.collect { newMessages ->

                if (newMessages.isNotEmpty()) {
                    val relevantMessages = newMessages.filter { it.contactName == currentContact }

                    if (relevantMessages.isNotEmpty()) {
                        // Сохраняем в базу данных
                        relevantMessages.forEach { message ->
                            viewModelScope.launch {
                                messageRepository.createDirectMessage(message)


                            }
                        }

                        // Обновляем UI
                        val currentMessages = _messages.value.orEmpty().toMutableList()
                        currentMessages.addAll(relevantMessages)
                        _messages.value = currentMessages

                        // Помечаем как прочитанные
                        relevantMessages.forEach { message ->
                            markMessageAsRead(message.id)
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketRepository.disconnect()

            }
}