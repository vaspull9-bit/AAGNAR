package com.example.aagnar.data.repository

import com.example.aagnar.domain.model.Group
import com.example.aagnar.domain.model.GroupMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor() {
    // ВРЕМЕННО: убрали зависимости от GroupDao и WebSocketRepository

    suspend fun createGroup(groupName: String, members: List<String>) {
        // Заглушка
        println("Creating group: $groupName with members: $members")
    }

    suspend fun sendGroupMessage(groupId: String, content: String): String {
        // Заглушка
        val messageId = UUID.randomUUID().toString()
        println("Sending message to group $groupId: $content")
        return messageId
    }

    suspend fun addMembersToGroup(groupId: String, newMembers: List<String>) {
        // Заглушка
        println("Adding members to group $groupId: $newMembers")
    }

    suspend fun leaveGroup(groupId: String) {
        // Заглушка
        println("Leaving group: $groupId")
    }

    // Локальные операции с базой данных - заглушки
    fun getGroups(): Flow<List<Group>> = flowOf(emptyList())

    fun getGroupMessages(groupId: String): Flow<List<GroupMessage>> = flowOf(emptyList())

    suspend fun insertGroup(group: Group) {
        // Заглушка
    }

    suspend fun insertGroupMessage(message: GroupMessage) {
        // Заглушка
    }

    suspend fun updateGroupLastMessage(groupId: String, lastMessage: String, timestamp: Date) {
        // Заглушка
    }
}