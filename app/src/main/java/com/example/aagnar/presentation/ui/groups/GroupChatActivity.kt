package com.example.aagnar.presentation.ui.groups

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aagnar.databinding.ActivityGroupChatBinding
import com.example.aagnar.domain.model.Group
import com.example.aagnar.domain.model.GroupMessage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupChatBinding
    private val viewModel: GroupChatViewModel by viewModels()
    private lateinit var messagesAdapter: GroupMessagesAdapter

    private var groupId: String = ""
    private var groupName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupId = intent.getStringExtra("group_id") ?: ""
        groupName = intent.getStringExtra("group_name") ?: "Групповой чат"

        setupUI()
        setupRecyclerView()
        observeViewModel()
        loadGroupData()
    }

    private fun setupUI() {
        binding.toolbar.title = groupName
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun setupRecyclerView() {
        messagesAdapter = GroupMessagesAdapter(emptyList()) { message ->
            // Обработка кликов на сообщения
        }

        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@GroupChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messagesAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(this) { messages ->
            messagesAdapter.updateMessages(messages)
            binding.messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
        }

        viewModel.groupInfo.observe(this) { group ->
            groupName = group.name
            binding.toolbar.title = groupName
        }
    }

    private fun loadGroupData() {
        viewModel.loadGroupMessages(groupId)
        viewModel.loadGroupInfo(groupId)
    }

    private fun sendMessage() {
        val messageText = binding.messageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            viewModel.sendMessage(groupId, messageText)
            binding.messageInput.setText("")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_group_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_group_info -> {
                showGroupInfo()
                true
            }
            R.id.action_add_members -> {
                addMembers()
                true
            }
            R.id.action_leave_group -> {
                leaveGroup()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showGroupInfo() {
        // TODO: Показать информацию о группе
    }

    private fun addMembers() {
        // TODO: Открыть экран добавления участников
    }

    private fun leaveGroup() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Покинуть группу")
            .setMessage("Вы уверены, что хотите покинуть группу \"$groupName\"?")
            .setPositiveButton("Покинуть") { _, _ ->
                viewModel.leaveGroup(groupId)
                finish()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}