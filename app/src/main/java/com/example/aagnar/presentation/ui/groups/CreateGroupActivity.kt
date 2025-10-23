package com.example.aagnar.presentation.ui.groups

import android.os.Bundle
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aagnar.databinding.ActivityCreateGroupBinding
import com.example.aagnar.domain.model.Contact
import com.example.aagnar.presentation.ui.contacts.ContactsAdapter
import com.example.aagnar.presentation.ui.contacts.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateGroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateGroupBinding
    private val viewModel: CreateGroupViewModel by viewModels()
    private lateinit var contactsAdapter: ContactsAdapter
    private val selectedContacts = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        observeViewModel()
        loadContacts()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.createButton.setOnClickListener {
            createGroup()
        }

        // Слушатель для поиска
        binding.searchInput.addTextChangedListener { text ->
            viewModel.filterContacts(text.toString())
        }
    }

    private fun setupRecyclerView() {
        contactsAdapter = ContactsAdapter(emptyList()) { contact ->
            toggleContactSelection(contact)
        }

        binding.contactsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CreateGroupActivity)
            adapter = contactsAdapter
        }

        binding.selectedContactsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CreateGroupActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = SelectedContactsAdapter(emptyList()) { contact ->
                removeContact(contact)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.contacts.observe(this) { contacts ->
            contactsAdapter.updateContacts(contacts)
        }

        viewModel.createGroupResult.observe(this) { result ->
            when (result) {
                is CreateGroupResult.Success -> {
                    showMessage("Группа создана успешно")
                    finish()
                }
                is CreateGroupResult.Error -> {
                    showMessage("Ошибка: ${result.message}")
                }
                else -> {}
            }
        }
    }

    private fun loadContacts() {
        viewModel.loadContacts()
    }

    private fun toggleContactSelection(contact: Contact) {
        if (selectedContacts.contains(contact.username)) {
            selectedContacts.remove(contact.username)
        } else {
            selectedContacts.add(contact.username)
        }
        updateSelectedContactsUI()
        updateCreateButton()
    }

    private fun removeContact(contact: Contact) {
        selectedContacts.remove(contact.username)
        updateSelectedContactsUI()
        updateCreateButton()
    }

    private fun updateSelectedContactsUI() {
        val selectedContactsList = selectedContacts.map { username ->
            Contact(username, Status.ONLINE) // TODO: Загрузить реальные данные
        }
        (binding.selectedContactsRecyclerView.adapter as? SelectedContactsAdapter)?.updateContacts(selectedContactsList)
    }

    private fun updateCreateButton() {
        val groupName = binding.groupNameInput.text.toString().trim()
        binding.createButton.isEnabled = groupName.isNotEmpty() && selectedContacts.isNotEmpty()
    }

    private fun createGroup() {
        val groupName = binding.groupNameInput.text.toString().trim()
        if (groupName.isNotEmpty() && selectedContacts.isNotEmpty()) {
            viewModel.createGroup(groupName, selectedContacts.toList())
        }
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}