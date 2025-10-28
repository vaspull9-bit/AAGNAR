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
            timestamp = Date(this.timestamp),  // ← Date вместо Long
            isDelivered = this.isDelivered,
            isRead = this.isRead,
            isEncrypted = this.isEncrypted
            // УБРАТЬ поля которых нет в MessageEntity: text, type, isVoiceMessage, hasAttachment, isSynced
        )
    }

    private fun MessageEntity.toDomain(): Message {
        return Message(
            id = this.id,
            contactName = this.contactName,
            content = this.content,
            timestamp = this.timestamp.time,  // ← Long вместо Date
            type = MessageType.RECEIVED,  // ← ЗАДАТЬ ЗНАЧЕНИЕ ПО УМОЛЧАНИЮ
            isDelivered = this.isDelivered,
            isRead = this.isRead,
            isEncrypted = this.isEncrypted
            // УБРАТЬ поля которых нет: text, isVoiceMessage, hasAttachment, isSynced
        )
    }


}