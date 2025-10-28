package com.example.aagnar.presentation.ui.groups

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aagnar.domain.model.Contact
import com.example.aagnar.domain.repository.ContactRepository
import com.example.aagnar.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.aagnar.domain.model.Status

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> = _contacts

    private val _createGroupResult = MutableLiveData<CreateGroupResult>()
    val createGroupResult: LiveData<CreateGroupResult> = _createGroupResult

    private var allContacts = emptyList<Contact>()

    fun loadContacts() {
        viewModelScope.launch {
            // TODO: Загрузить контакты из репозитория
            allContacts = listOf(
                Contact(name = "user123", address = "", status = Status.ONLINE),  // ← name вместо username
                Contact(name = "alice", address = "", status = Status.ONLINE),
                Contact(name = "bob", address = "", status = Status.OFFLINE)
            )
            _contacts.value = allContacts
        }
    }

    fun filterContacts(query: String) {
        val filtered = if (query.isEmpty()) {
            allContacts
        } else {
            allContacts.filter { it.name.contains(query, ignoreCase = true) }
        }
        _contacts.value = filtered
    }

    fun createGroup(groupName: String, members: List<String>) {
        viewModelScope.launch {
            try {
                groupRepository.createGroup(groupName, members)
                _createGroupResult.value = CreateGroupResult.Success
            } catch (e: Exception) {
                _createGroupResult.value = CreateGroupResult.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class CreateGroupResult {
    object Success : CreateGroupResult()
    data class Error(val message: String) : CreateGroupResult()
}