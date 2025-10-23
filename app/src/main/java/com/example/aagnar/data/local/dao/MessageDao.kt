package com.example.aagnar.data.local.dao

import androidx.room.*
import com.example.aagnar.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    // Используем пагинацию для больших списков
    @Query("SELECT * FROM message WHERE contactName = :contactName ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getMessagesPaginated(contactName: String, limit: Int, offset: Int): List<Message>

    // Flow с пагинацией
    @Query("SELECT * FROM message WHERE contactName = :contactName ORDER BY timestamp DESC LIMIT :pageSize")
    fun getMessagesFlow(contactName: String, pageSize: Int): Flow<List<Message>>

    // Оптимизированный запрос для подсчета
    @Query("SELECT COUNT(*) FROM message WHERE contactName = :contactName")
    suspend fun getMessageCount(contactName: String): Int

    // Пакетные операции для улучшения производительности
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)

    @Update
    suspend fun updateMessages(messages: List<Message>)

    @Delete
    suspend fun deleteMessages(messages: List<Message>)

    // Оптимизированное удаление старых сообщений
    @Query("DELETE FROM message WHERE timestamp < :olderThan AND contactName = :contactName")
    suspend fun deleteOldMessages(contactName: String, olderThan: Date)

    // Индексированные запросы
    @Query("SELECT * FROM message WHERE contactName = :contactName AND timestamp BETWEEN :start AND :end")
    suspend fun getMessagesInRange(contactName: String, start: Date, end: Date): List<Message>
}