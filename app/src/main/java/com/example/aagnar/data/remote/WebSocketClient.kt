package com.example.aagnar.data.remote

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.aagnar.domain.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.util.*
import java.util.concurrent.Executors
import java.util.LinkedList // ← ДОБАВЬТЕ ЭТУ СТРОКУ
import java.util.Date // ← ДОБАВЬТЕ ЭТУ СТРОКУ
import com.example.aagnar.domain.model.FileInfo

class WebSocketClient(
    private val context: Context,
    private val username: String
) {

    companion object {
        private const val TAG = "WebSocketClient"
        private const val RECONNECT_DELAY = 5000L
        private const val MAX_RECONNECT_ATTEMPTS = 5
        private const val HEARTBEAT_INTERVAL = 30000L
        private const val BATCH_SIZE = 10
        private const val BATCH_DELAY_MS = 100L
    }

    private var webSocket: WebSocketClientImpl? = null
    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState

    private val _incomingMessages = MutableStateFlow<List<Message>>(emptyList())
    val incomingMessages: StateFlow<List<Message>> = _incomingMessages

    // Механизм повторного подключения
    private var reconnectAttempts = 0
    private var heartbeatTimer: Timer? = null
    private var isManuallyDisconnected = false
    private val reconnectHandler = Handler(Looper.getMainLooper())

    // Оптимизация: пул сообщений для батчинга
    private val messageQueue = LinkedList<String>()
    private var isProcessingQueue = false
    private val batchExecutor = Executors.newSingleThreadExecutor()

    // Статистика для мониторинга
    private var messagesSent = 0
    private var messagesReceived = 0
    private var lastConnectionTime: Long = 0

    fun connect() {
        if (isManuallyDisconnected) {
            Log.d(TAG, "Manual disconnect - skipping connection")
            return
        }

        try {
            val serverUri = URI("ws://192.168.88.240:8889")
            webSocket = WebSocketClientImpl(serverUri).apply {
                connect()
            }
            lastConnectionTime = System.currentTimeMillis()
            Log.d(TAG, "Attempting connection to: $serverUri")
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed: ${e.message}")
            _connectionState.value = false
            scheduleReconnect()
        }
    }

    fun disconnect() {
        Log.d(TAG, "Manual disconnect initiated")
        isManuallyDisconnected = true
        stopHeartbeat()
        webSocket?.close()
        webSocket = null
        _connectionState.value = false

        synchronized(messageQueue) {
            messageQueue.clear()
        }

        batchExecutor.shutdown()
    }

    // Основные методы отправки сообщений
    fun sendMessage(toUser: String, content: String, messageId: String) {
        val message = JSONObject().apply {
            put("type", "message")
            put("from", username)
            put("to", toUser)
            put("content", content)
            put("message_id", messageId)
            put("timestamp", System.currentTimeMillis())
        }
        queueMessage(message.toString())
    }

    fun sendVoiceMessage(toUser: String, audioData: String, duration: Int, messageId: String) {
        val voiceMsg = JSONObject().apply {
            put("type", "voice_message")
            put("from", username)
            put("to", toUser)
            put("audio_data", audioData)
            put("duration", duration)
            put("message_id", messageId)
            put("timestamp", System.currentTimeMillis())
        }
        queueMessage(voiceMsg.toString())
    }

    fun sendEncryptedMessage(toUser: String, encryptedContent: String, messageId: String) {
        val message = JSONObject().apply {
            put("type", "encrypted_message")
            put("from", username)
            put("to", toUser)
            put("encrypted_content", encryptedContent)
            put("message_id", messageId)
            put("timestamp", System.currentTimeMillis())
        }
        queueMessage(message.toString())
    }

    fun sendFileChunk(
        toUser: String,
        fileInfo: com.example.aagnar.domain.model.FileInfo,
        chunkData: ByteArray,
        chunkIndex: Int,
        totalChunks: Int
    ) {
        val base64Chunk = android.util.Base64.encodeToString(chunkData, android.util.Base64.DEFAULT)

        val fileMsg = JSONObject().apply {
            put("type", "file_transfer")
            put("from", username)
            put("to", toUser)
            put("file_name", fileInfo.name)
            put("file_type", fileInfo.type)
            put("file_size", fileInfo.size)
            put("file_id", fileInfo.fileId)
            put("chunk_data", base64Chunk)
            put("chunk_index", chunkIndex)
            put("total_chunks", totalChunks)
        }
        queueMessage(fileMsg.toString())
    }

    fun sendTypingIndicator(toUser: String, isTyping: Boolean) {
        val typing = JSONObject().apply {
            put("type", "typing")
            put("from", username)
            put("to", toUser)
            put("typing", isTyping)
        }
        sendMessageImmediate(typing.toString()) // Отправляем немедленно для реального времени
    }

    fun sendReadReceipt(toUser: String, messageId: String) {
        val receipt = JSONObject().apply {
            put("type", "read_receipt")
            put("from", username)
            put("to", toUser)
            put("message_id", messageId)
        }
        sendMessageImmediate(receipt.toString()) // Отправляем немедленно для подтверждения
    }

    // Групповые чаты
    fun createGroup(groupName: String, members: List<String>) {
        val createGroupMsg = JSONObject().apply {
            put("type", "create_group")
            put("creator", username)
            put("group_name", groupName)
            put("members", JSONArray(members))
        }
        sendMessageImmediate(createGroupMsg.toString())
    }

    fun sendGroupMessage(groupId: String, content: String, messageId: String) {
        val groupMsg = JSONObject().apply {
            put("type", "group_message")
            put("from", username)
            put("group_id", groupId)
            put("content", content)
            put("message_id", messageId)
            put("timestamp", System.currentTimeMillis())
        }
        queueMessage(groupMsg.toString())
    }

    fun addMembersToGroup(groupId: String, newMembers: List<String>) {
        val addMembersMsg = JSONObject().apply {
            put("type", "add_to_group")
            put("group_id", groupId)
            put("inviter", username)
            put("new_members", JSONArray(newMembers))
        }
        sendMessageImmediate(addMembersMsg.toString())
    }

    fun leaveGroup(groupId: String) {
        val leaveMsg = JSONObject().apply {
            put("type", "leave_group")
            put("group_id", groupId)
            put("user", username)
        }
        sendMessageImmediate(leaveMsg.toString())
    }

    // WebRTC сигналинг
    fun sendWebRTCMessage(message: String) {
        sendMessageImmediate(message)
    }

    // Приватные методы для оптимизации
    private fun queueMessage(message: String) {
        synchronized(messageQueue) {
            messageQueue.add(message)
        }

        if (!isProcessingQueue && _connectionState.value) {
            processMessageQueue()
        }
    }

    private fun processMessageQueue() {
        if (isProcessingQueue || webSocket == null || !_connectionState.value) return

        isProcessingQueue = true

        batchExecutor.execute {
            try {
                val messagesToSend = mutableListOf<String>()
                synchronized(messageQueue) {
                    repeat(BATCH_SIZE) {
                        if (messageQueue.isNotEmpty()) {
                            messagesToSend.add(messageQueue.removeFirst())
                        }
                    }
                }

                if (messagesToSend.isNotEmpty()) {
                    if (messagesToSend.size == 1) {
                        // Отправляем одиночное сообщение
                        sendMessageImmediate(messagesToSend.first())
                    } else {
                        // Отправляем батч сообщений
                        val batchMessage = JSONObject().apply {
                            put("type", "batch")
                            put("messages", JSONArray(messagesToSend))
                            put("batch_size", messagesToSend.size)
                            put("timestamp", System.currentTimeMillis())
                        }
                        sendMessageImmediate(batchMessage.toString())
                        Log.d(TAG, "Sent batch of ${messagesToSend.size} messages")
                    }

                    messagesSent += messagesToSend.size
                }

                isProcessingQueue = false

                // Если еще есть сообщения в очереди, продолжаем обработку
                if (messageQueue.isNotEmpty()) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        processMessageQueue()
                    }, BATCH_DELAY_MS)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing message queue: ${e.message}")
                isProcessingQueue = false
            }
        }
    }

    private fun sendMessageImmediate(message: String) {
        try {
            webSocket?.send(message)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message: ${e.message}")
            // Возвращаем сообщение в очередь при ошибке
            queueMessage(message)
        }
    }

    private fun sendLogin() {
        val login = JSONObject().apply {
            put("type", "login")
            put("username", username)
            put("device_id", getDeviceId())
            put("app_version", getAppVersion())
            put("timestamp", System.currentTimeMillis())
        }
        sendMessageImmediate(login.toString())
    }

    private fun startHeartbeat() {
        heartbeatTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    if (_connectionState.value) {
                        val heartbeat = JSONObject().apply {
                            put("type", "heartbeat")
                            put("username", username)
                            put("timestamp", System.currentTimeMillis())
                            put("messages_sent", messagesSent)
                            put("messages_received", messagesReceived)
                        }
                        sendMessageImmediate(heartbeat.toString())
                    }
                }
            }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL)
        }
        Log.d(TAG, "Heartbeat started")
    }

    private fun stopHeartbeat() {
        heartbeatTimer?.cancel()
        heartbeatTimer = null
        Log.d(TAG, "Heartbeat stopped")
    }

    private fun scheduleReconnect() {
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts++
            val delay = RECONNECT_DELAY * reconnectAttempts

            Log.d(TAG, "Scheduling reconnect in ${delay}ms (attempt $reconnectAttempts)")

            reconnectHandler.postDelayed({
                if (!_connectionState.value && !isManuallyDisconnected) {
                    connect()
                }
            }, delay)
        } else {
            Log.e(TAG, "Max reconnection attempts reached")
            // Можно уведомить UI о невозможности подключения
        }
    }

    // Обработка входящих сообщений
    private fun handleMessage(jsonMessage: String) {
        messagesReceived++

        try {
            val json = JSONObject(jsonMessage)
            val type = json.getString("type")

            when (type) {
                "message" -> handleIncomingMessage(json)
                "encrypted_message" -> handleEncryptedMessage(json)
                "voice_message" -> handleVoiceMessage(json)
                "file_chunk" -> handleFileChunk(json)
                "typing" -> handleTypingIndicator(json)
                "read_receipt" -> handleReadReceipt(json)
                "message_ack" -> handleMessageAck(json)
                "voice_ack" -> handleVoiceAck(json)
                "file_ack" -> handleFileAck(json)
                "group_created" -> handleGroupCreated(json)
                "group_message" -> handleGroupMessage(json)
                "group_invite" -> handleGroupInvite(json)
                "members_added" -> handleGroupNotification(json)
                "member_left" -> handleGroupNotification(json)
                "batch" -> handleBatchMessage(json)
                "heartbeat_ack" -> handleHeartbeatAck(json)
                else -> Log.w(TAG, "Unknown message type: $type")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message: ${e.message}")
        }
    }

//    private fun handleIncomingMessage(json: JSONObject) {
//        val fromUser = json.getString("from")
//        val content = json.getString("content")
//        val messageId = json.getString("message_id")
//        val timestamp = json.getLong("timestamp")
//        val fileName = json.getString("file_name")
//        val fileType = json.getString("file_type")
//        val fileSize = json.getLong("file_size")
//        val fileId = json.getString("file_id")
//        val chunkIndex = json.getInt("chunk_index")
//        val totalChunks = json.getInt("total_chunks")
//
//        // Создаем fileInfo если модель требует его
//        val fileInfo = com.example.aagnar.domain.model.FileInfo(
//            name = fileName,
//            size = fileSize,
//            type = fileType,
//            fileId = fileId,
//            transferProgress = ((chunkIndex + 1) * 100 / totalChunks)
//        )
//
//        val message = Message(
//            id = fileId + "_chunk_" + chunkIndex,
//            contactName = fromUser,
//            content = "📎 $fileName",
//            timestamp = System.currentTimeMillis(), // ← ИСПРАВЛЕНО
//            type = com.example.aagnar.domain.model.MessageType.RECEIVED,
//            hasAttachment = true,
//            fileInfo = fileInfo
//        )
//
//        addToIncomingMessages(message)
//        Log.d(TAG, "Received message from $fromUser: ${content.take(50)}...")
//    }

    private fun handleIncomingMessage(json: JSONObject) {
        try {
            val fromUser = json.getString("from")
            val content = json.getString("content")
            val messageId = json.getString("message_id")
            val timestamp = json.getLong("timestamp")



            val message = Message(
                id = messageId,
                contactName = fromUser,
                content = content,
                timestamp = timestamp,
                type = com.example.aagnar.domain.model.MessageType.RECEIVED,
                isDelivered = true

            )

            addToIncomingMessages(message)
            Log.d(TAG, "Received message from $fromUser: ${content.take(50)}...")
        } catch (e: Exception) {
            Log.e(TAG, "Error handling incoming message: ${e.message}")
        }
    }


    private fun handleEncryptedMessage(json: JSONObject) {
        try {
            val fromUser = json.getString("from")
            val encryptedContent = json.getString("encrypted_content")
            val messageId = json.getString("message_id")
            val timestamp = json.getLong("timestamp")

            // TODO: Дешифровать сообщение с помощью KeyManager
            val decryptedContent = "🔒 [Зашифрованное сообщение]" // Временная заглушка

            val message = Message(
                id = messageId,
                contactName = fromUser,
                content = decryptedContent,
                timestamp = timestamp,
                type = com.example.aagnar.domain.model.MessageType.RECEIVED,
                isDelivered = true,
                isEncrypted = true
            )

            addToIncomingMessages(message)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling encrypted message: ${e.message}")
        }
    }

    private fun handleVoiceMessage(json: JSONObject) {
        try {
            val fromUser = json.getString("from")
            val audioData = json.getString("audio_data")
            val duration = json.getInt("duration")
            val messageId = json.getString("message_id")
            val timestamp = json.getLong("timestamp")

            val voiceMessageInfo = com.example.aagnar.domain.model.VoiceMessageInfo(
                duration = duration,
                audioData = audioData
            )

            val message = Message(
                id = messageId,
                contactName = fromUser,
                content = "🎤 Голосовое сообщение",
                timestamp = timestamp,
                type = com.example.aagnar.domain.model.MessageType.RECEIVED,
                isVoiceMessage = true,
                voiceMessageInfo = voiceMessageInfo
            )

            addToIncomingMessages(message)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling voice message: ${e.message}")
        }
    }

    private fun handleFileChunk(json: JSONObject) {
        try {
            val fromUser = json.getString("from")
            val fileName = json.getString("file_name")
            val fileType = json.getString("file_type")
            val fileSize = json.getLong("file_size")
            val fileId = json.getString("file_id")
            val chunkData = json.getString("chunk_data")
            val chunkIndex = json.getInt("chunk_index")
            val totalChunks = json.getInt("total_chunks")

            // Создаем fileInfo если модель требует его
            val fileInfo = com.example.aagnar.domain.model.FileInfo(
                name = fileName,
                size = fileSize,
                type = fileType,
                fileId = fileId,
                transferProgress = ((chunkIndex + 1) * 100 / totalChunks)
            )

            val message = Message(
                id = fileId + "_chunk_" + chunkIndex,
                contactName = fromUser,
                content = "📎 $fileName",
                timestamp = System.currentTimeMillis(),
                type = com.example.aagnar.domain.model.MessageType.RECEIVED,
                hasAttachment = true,
                fileInfo = fileInfo
            )

            addToIncomingMessages(message)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling file chunk: ${e.message}")
        }
    }

    private fun handleTypingIndicator(json: JSONObject) {
        val fromUser = json.getString("from")
        val isTyping = json.getBoolean("typing")

        // TODO: Уведомить UI о наборе текста
        Log.d(TAG, "User $fromUser is typing: $isTyping")
    }

    private fun handleReadReceipt(json: JSONObject) {
        val fromUser = json.getString("from")
        val messageId = json.getString("message_id")

        // TODO: Обновить статус сообщения как прочитанное
        Log.d(TAG, "Message $messageId read by $fromUser")
    }

    private fun handleMessageAck(json: JSONObject) {
        val messageId = json.getString("message_id")
        val status = json.getString("status")

        // TODO: Обновить статус доставки сообщения
        Log.d(TAG, "Message $messageId status: $status")
    }

    private fun handleVoiceAck(json: JSONObject) {
        val messageId = json.getString("message_id")
        val status = json.getString("status")

        Log.d(TAG, "Voice message $messageId status: $status")
    }

    private fun handleFileAck(json: JSONObject) {
        val fileId = json.getString("file_id")
        val chunkIndex = json.getInt("chunk_index")
        val status = json.getString("status")

        Log.d(TAG, "File chunk $chunkIndex of $fileId status: $status")
    }

    private fun handleGroupCreated(json: JSONObject) {
        val groupId = json.getString("group_id")
        val groupName = json.getString("group_name")

        Log.d(TAG, "Group created: $groupName ($groupId)")
    }

    private fun handleGroupMessage(json: JSONObject) {
        val fromUser = json.getString("from")
        val groupId = json.getString("group_id")
        val groupName = json.getString("group_name")
        val content = json.getString("content")
        val messageId = json.getString("message_id")
        val timestamp = json.getLong("timestamp")

        Log.d(TAG, "Group message from $fromUser in $groupName: $content")

        // TODO: Обработать групповое сообщение
    }

    private fun handleGroupInvite(json: JSONObject) {
        val groupId = json.getString("group_id")
        val groupName = json.getString("group_name")
        val inviter = json.getString("inviter")

        Log.d(TAG, "Invited to group $groupName by $inviter")

        // TODO: Показать уведомление о приглашении в группу
    }

    private fun handleGroupNotification(json: JSONObject) {
        val groupId = json.getString("group_id")
        val groupName = json.getString("group_name")
        val message = json.getString("message")

        Log.d(TAG, "Group notification for $groupName: $message")
    }

    private fun handleBatchMessage(json: JSONObject) {
        val messagesArray = json.getJSONArray("messages")
        for (i in 0 until messagesArray.length()) {
            val messageJson = messagesArray.getString(i)
            handleMessage(messageJson)
        }
        Log.d(TAG, "Processed batch of ${messagesArray.length()} messages")
    }

    private fun handleHeartbeatAck(json: JSONObject) {
        val timestamp = json.getLong("timestamp")
        val latency = System.currentTimeMillis() - timestamp
        Log.d(TAG, "Heartbeat acknowledged, latency: ${latency}ms")
    }

    private fun addToIncomingMessages(message: Message) {
        val currentMessages = _incomingMessages.value.toMutableList()
        currentMessages.add(message)
        _incomingMessages.value = currentMessages
    }

    // Вспомогательные методы
    private fun getDeviceId(): String {
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    // Методы для мониторинга и отладки
    fun getConnectionStats(): Map<String, Any> {
        return mapOf(
            "connected" to _connectionState.value,
            "reconnect_attempts" to reconnectAttempts,
            "messages_sent" to messagesSent,
            "messages_received" to messagesReceived,
            "queue_size" to messageQueue.size,
            "connection_duration" to if (lastConnectionTime > 0) System.currentTimeMillis() - lastConnectionTime else 0
        )
    }

    fun clearStats() {
        messagesSent = 0
        messagesReceived = 0
        reconnectAttempts = 0
    }

    // Внутренний WebSocket клиент
    private inner class WebSocketClientImpl(serverUri: URI) : WebSocketClient(serverUri) {

        override fun onOpen(handshakedata: ServerHandshake?) {
            Log.d(TAG, "WebSocket connection opened successfully")
            _connectionState.value = true
            reconnectAttempts = 0

            sendLogin()
            startHeartbeat()
            processMessageQueue()
        }

        override fun onMessage(message: String?) {
            message?.let {
                Log.d(TAG, "Message received: ${message.take(100)}...")
                handleMessage(it)
            }
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            Log.d(TAG, "WebSocket connection closed: $reason (code: $code, remote: $remote)")
            _connectionState.value = false
            stopHeartbeat()

            if (!isManuallyDisconnected) {
                scheduleReconnect()
            }
        }

        override fun onError(ex: Exception?) {
            Log.e(TAG, "WebSocket error: ${ex?.message}")
            _connectionState.value = false
        }
    }

    // Очистка ресурсов
    fun cleanup() {
        disconnect()
        batchExecutor.shutdownNow()
        reconnectHandler.removeCallbacksAndMessages(null)
    }
}