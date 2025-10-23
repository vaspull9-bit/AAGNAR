package com.example.aagnar.data.repository

import com.example.aagnar.data.local.dao.GroupDao
import com.example.aagnar.data.remote.WebSocketRepository
import com.example.aagnar.domain.model.Group
import com.example.aagnar.domain.model.GroupMessage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GroupRepository @Inject constructor(
    private val groupDao: GroupDao,
    private val webSocketRepository: WebSocketRepository
) {

    suspend fun createGroup(groupName: String, members: List<String>) {
        webSocketRepository.createGroup(groupName, members)
    }

    suspend fun sendGroupMessage(groupId: String, content: String): String {
        val messageId = java.util.UUID.randomUUID().toString()
        webSocketRepository.sendGroupMessage(groupId, content, messageId)
        return messageId
    }

    suspend fun addMembersToGroup(groupId: String, newMembers: List<String>) {
        webSocketRepository.addMembersToGroup(groupId, newMembers)
    }

    suspend fun leaveGroup(groupId: String) {
        webSocketRepository.leaveGroup(groupId)
    }

    // Локальные операции с базой данных
    fun getGroups(): Flow<List<Group>> {
        return groupDao.getGroups()
    }

    fun getGroupMessages(groupId: String): Flow<List<GroupMessage>> {
        return groupDao.getGroupMessages(groupId)
    }

    suspend fun insertGroup(group: Group) {
        groupDao.insertGroup(group)
    }

    suspend fun insertGroupMessage(message: GroupMessage) {
        groupDao.insertGroupMessage(message)
    }

    suspend fun updateGroupLastMessage(groupId: String, lastMessage: String, timestamp: java.util.Date) {
        groupDao.updateGroupLastMessage(groupId, lastMessage, timestamp)
    }
}