package com.example.aagnar.data.repository

import com.example.aagnar.data.local.dao.MessageDao
import com.example.aagnar.data.local.entity.MessageEntity
import com.example.aagnar.domain.model.Message
import com.example.aagnar.domain.model.MessageType
import com.example.aagnar.domain.repository.MessageRepository
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Date

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao
) : MessageRepository {

    override suspend fun sendMessage(roomId: String, message: String) {
        // TODO: Реализовать
    }

    override suspend fun createDirectMessage(userId: String): String {
        // TODO: Реализовать
        return "room_$userId"
    }

    override suspend fun uploadFile(roomId: String, filePath: String): String {
        // TODO: Реализовать
        return "file_id"
    }

    override suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(message.toEntity())
    }

    override suspend fun getMessagesWithContact(contactName: String): List<Message> {
        return messageDao.getMessagesByContact(contactName).map { it.toDomain() }
    }

    override suspend fun markMessageAsRead(messageId: String) {
        messageDao.markAsRead(messageId)
    }

    // Extension functions for conversion between Domain and Entity
    // Extension functions for conversion between Domain and Entity
    private fun Message.toEntity(): MessageEntity {
        return MessageEntity(
            id = this.id,
            contactName = this.contactName,
            content = this.content,
            timestamp = Date(this.timestamp),
            type = this.type.name,
            isDelivered = this.isDelivered ?: false,
            isRead = this.isRead ?: false,
            isEncrypted = this.isEncrypted ?: false,
            isVoiceMessage = this.isVoiceMessage ?: false,
            hasAttachment = this.hasAttachment ?: false,
            filePath = this.fileInfo?.uri?.toString() ?: "",
            voiceDuration = this.voiceMessageInfo?.duration ?: 0
        )
    }

    private fun MessageEntity.toDomain(): Message {
        return Message(
            id = this.id,
            contactName = this.contactName,
            content = this.content,
            timestamp = this.timestamp.time,
            type = MessageType.valueOf(this.type),
            isDelivered = this.isDelivered,
            isRead = this.isRead,
            isEncrypted = this.isEncrypted,
            isVoiceMessage = this.isVoiceMessage,
            hasAttachment = this.hasAttachment,
            fileInfo = if (this.filePath.isNotEmpty()) {
                try {
                    com.example.aagnar.domain.model.FileInfo(
                        name = "file", // ← временное значение
                        size = 0L,     // ← временное значение
                        type = "file", // ← временное значение
                        uri = android.net.Uri.parse(this.filePath)
                    )
                } catch (e: Exception) {
                    null
                }
            } else null,
            voiceMessageInfo = if (this.voiceDuration > 0) {
                com.example.aagnar.domain.model.VoiceMessageInfo(
                    duration = this.voiceDuration
                )
            } else null
        )
    }


}