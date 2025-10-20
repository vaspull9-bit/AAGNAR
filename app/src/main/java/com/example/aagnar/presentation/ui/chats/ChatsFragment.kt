package com.example.aagnar.presentation.ui.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aagnar.databinding.FragmentChatsBinding
import com.example.aagnar.domain.model.Chat

class ChatsFragment : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChatsList()
        loadChats()
    }

    private fun setupChatsList() {
        val chatsAdapter = ChatsAdapter(emptyList()) { chat ->
            openChat(chat)
        }

        binding.chatsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatsAdapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadChats()
        }
    }

    private fun loadChats() {
        // TODO: Загрузить чаты из базы данных
        val chats = listOf(
            Chat(
                id = "1",
                contactName = "user123",
                lastMessage = "Привет! Как дела?",
                timestamp = System.currentTimeMillis() - 300000,
                unreadCount = 2,
                isOnline = true
            ),
            Chat(
                id = "2",
                contactName = "alice",
                lastMessage = "Договорились на завтра",
                timestamp = System.currentTimeMillis() - 86400000,
                unreadCount = 0,
                isOnline = false
            )
        )

        (binding.chatsRecyclerView.adapter as? ChatsAdapter)?.updateChats(chats)
        binding.swipeRefresh.isRefreshing = false

        // Показываем/скрываем empty state
        binding.emptyState.visibility = if (chats.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openChat(chat: Chat) {
        // TODO: Открыть экран чата
        showMessage("Открыть чат с ${chat.contactName}")
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}