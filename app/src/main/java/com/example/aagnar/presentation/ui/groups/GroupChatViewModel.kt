package com.example.aagnar.presentation.ui.groups

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aagnar.domain.model.Group
import com.example.aagnar.domain.model.GroupMessage
import kotlinx.coroutines.launch
import java.util.Date

class GroupChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<GroupMessage>>()
    val messages: LiveData<List<GroupMessage>> = _messages

    private val _groupInfo = MutableLiveData<Group>()
    val groupInfo: LiveData<Group> = _groupInfo

    fun loadGroupMessages(groupId: String) {
        viewModelScope.launch {
            // TODO: Загрузить сообщения группы
            _messages.value = emptyList()
        }
    }

    fun loadGroupInfo(groupId: String) {
        viewModelScope.launch {
            // TODO: Загрузить информацию о группе
            _groupInfo.value = Group(
                id = groupId,
                name = "Группа",
                creator = "user",                        // ← ОБЯЗАТЕЛЬНЫЙ
                members = emptyList(),                   // ← ОБЯЗАТЕЛЬНЫЙ
                admins = listOf("user"),                 // ← ОБЯЗАТЕЛЬНЫЙ
                createdAt = Date(),                      // ← ОБЯЗАТЕЛЬНЫЙ
                lastMessage = null,
                lastMessageTime = null,
                unreadCount = 0,
                avatarUrl = null
            )


        }
    }

    fun sendMessage(groupId: String, message: String) {
        viewModelScope.launch {
            // TODO: Отправить сообщение
        }
    }

    fun leaveGroup(groupId: String) {
        viewModelScope.launch {
            // TODO: Покинуть группу
        }
    }
}