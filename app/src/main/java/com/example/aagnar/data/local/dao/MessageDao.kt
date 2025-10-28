package com.example.aagnar.data.local.dao

import androidx.room.*
import com.example.aagnar.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE contactName = :contactName ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getMessagesPaginated(contactName: String, limit: Int, offset: Int): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE contactName = :contactName ORDER BY timestamp DESC LIMIT :pageSize")
    fun getMessagesFlow(contactName: String, pageSize: Int): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE contactName = :contactName")
    suspend fun getMessageCount(contactName: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessages(messages: List<MessageEntity>)

    @Delete
    suspend fun deleteMessages(messages: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE timestamp < :olderThan AND contactName = :contactName")
    suspend fun deleteOldMessages(contactName: String, olderThan: Date)

    @Query("SELECT * FROM messages WHERE contactName = :contactName AND timestamp BETWEEN :start AND :end")
    suspend fun getMessagesInRange(contactName: String, start: Date, end: Date): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE contactName = :contactName ORDER BY timestamp ASC")
    suspend fun getMessagesByContact(contactName: String): List<MessageEntity>

    @Query("UPDATE messages SET isRead = 1 WHERE id = :messageId")
    suspend fun markAsRead(messageId: String)

}