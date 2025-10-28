package com.example.aagnar.presentation.ui.groups

import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aagnar.R
import com.example.aagnar.domain.model.Contact
import com.example.aagnar.presentation.ui.contacts.ContactsAdapter
import dagger.hilt.android.AndroidEntryPoint
import com.example.aagnar.domain.model.Status

@AndroidEntryPoint
class CreateGroupActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var groupNameInput: EditText
    private lateinit var searchInput: EditText
    private lateinit var createButton: Button
    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var selectedContactsRecyclerView: RecyclerView

    private val viewModel: CreateGroupViewModel by viewModels()
    private lateinit var contactsAdapter: ContactsAdapter
    private val selectedContacts = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        initViews()
        setupUI()
        setupRecyclerView()
        observeViewModel()
        loadContacts()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        groupNameInput = findViewById(R.id.groupNameInput)
        searchInput = findViewById(R.id.searchInput)
        createButton = findViewById(R.id.createButton)
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView)
        selectedContactsRecyclerView = findViewById(R.id.selectedContactsRecyclerView)
    }

    private fun setupUI() {
        toolbar.setNavigationOnClickListener {
            finish()
        }

        createButton.setOnClickListener {
            createGroup()
        }

        // Слушатель для поиска
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterContacts(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Слушатель для названия группы
        groupNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateCreateButton()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        contactsAdapter = ContactsAdapter(emptyList()) { contact ->
            toggleContactSelection(contact)
        }

        contactsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CreateGroupActivity)
            adapter = contactsAdapter
        }

        selectedContactsRecyclerView.apply {
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
        if (selectedContacts.contains(contact.name)) {
            selectedContacts.remove(contact.name)
        } else {
            selectedContacts.add(contact.name)
        }
        updateSelectedContactsUI()
        updateCreateButton()
    }

    private fun removeContact(contact: Contact) {
        selectedContacts.remove(contact.name)
        updateSelectedContactsUI()
        updateCreateButton()
    }

    private fun updateSelectedContactsUI() {
        val selectedContactsList = selectedContacts.map { name ->
            Contact(
                name = name,
                address = "",  // ОБЯЗАТЕЛЬНЫЙ
                status = Status.ONLINE  // ← ИСПОЛЬЗОВАТЬ STATUS
                // isOnline не обязателен (есть значение по умолчанию)
            )
        }
        (selectedContactsRecyclerView.adapter as? SelectedContactsAdapter)?.updateContacts(selectedContactsList)
    }

    private fun updateCreateButton() {
        val groupName = groupNameInput.text.toString().trim()
        createButton.isEnabled = groupName.isNotEmpty() && selectedContacts.isNotEmpty()
    }

    private fun createGroup() {
        val groupName = groupNameInput.text.toString().trim()
        if (groupName.isNotEmpty() && selectedContacts.isNotEmpty()) {
            viewModel.createGroup(groupName, selectedContacts.toList())
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}