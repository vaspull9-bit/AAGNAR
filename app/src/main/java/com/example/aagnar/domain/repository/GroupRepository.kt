package com.example.aagnar.domain.repository

import com.example.aagnar.domain.model.Group
import com.example.aagnar.domain.model.GroupMessage
import kotlinx.coroutines.flow.Flow
import java.util.*

interface GroupRepository {
    suspend fun createGroup(groupName: String, members: List<String>)
    suspend fun sendGroupMessage(groupId: String, content: String): String
    suspend fun addMembersToGroup(groupId: String, newMembers: List<String>)
    suspend fun leaveGroup(groupId: String)
    fun getGroups(): Flow<List<Group>>
    fun getGroupMessages(groupId: String): Flow<List<GroupMessage>>
    suspend fun insertGroup(group: Group)
    suspend fun insertGroupMessage(message: GroupMessage)
    suspend fun updateGroupLastMessage(groupId: String, lastMessage: String, timestamp: Date)
}