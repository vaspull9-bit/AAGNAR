package com.example.aagnar.data.local.dao

import androidx.room.*
import com.example.aagnar.domain.model.Group
import com.example.aagnar.domain.model.GroupMessage
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface GroupDao {

    @Query("SELECT * FROM `group` ORDER BY lastMessageTime DESC")
    fun getGroups(): Flow<List<Group>>

    @Query("SELECT * FROM group_message WHERE groupId = :groupId ORDER BY timestamp ASC")
    fun getGroupMessages(groupId: String): Flow<List<GroupMessage>>

    @Query("SELECT * FROM `group` WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): Group?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: Group)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMessage(message: GroupMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMessages(messages: List<GroupMessage>)

    @Query("UPDATE `group` SET lastMessage = :lastMessage, lastMessageTime = :timestamp WHERE id = :groupId")
    suspend fun updateGroupLastMessage(groupId: String, lastMessage: String, timestamp: Date)

    @Query("UPDATE `group` SET unreadCount = unreadCount + 1 WHERE id = :groupId")
    suspend fun incrementUnreadCount(groupId: String)

    @Query("UPDATE `group` SET unreadCount = 0 WHERE id = :groupId")
    suspend fun resetUnreadCount(groupId: String)

    @Delete
    suspend fun deleteGroup(group: Group)

    @Query("DELETE FROM group_message WHERE groupId = :groupId")
    suspend fun deleteGroupMessages(groupId: String)
}