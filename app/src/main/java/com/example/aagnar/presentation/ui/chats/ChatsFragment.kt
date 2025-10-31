package com.example.aagnar.presentation.ui.chats

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aagnar.R
import com.example.aagnar.presentation.ui.chat.ChatActivity
import com.example.aagnar.util.PerformanceMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.aagnar.domain.model.Chat

@AndroidEntryPoint
class ChatsFragment : Fragment() {

    @Inject
    lateinit var performanceMonitor: PerformanceMonitor
    private lateinit var chatsRecyclerView: RecyclerView
    private lateinit var emptyState: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return performanceMonitor.measure("ChatsFragment.onCreateView") {
            inflater.inflate(R.layout.fragment_chats, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        performanceMonitor.measure("ChatsFragment.onViewCreated") {
            // Временные тестовые чаты вместо реальных
            val testChats = listOf(
                Chat(
                    id = "1",
                    contactName = "Test Contact 1",
                    lastMessage = "Привет!",
                    timestamp = System.currentTimeMillis(),
                    unreadCount = 0,
                    isOnline = true
                ),
                Chat(
                    id = "2",
                    contactName = "Test Contact 2",
                    lastMessage = "Как дела?",
                    timestamp = System.currentTimeMillis() - 300000,
                    unreadCount = 2,
                    isOnline = false
                ),
                Chat(
                    id = "3",
                    contactName = "Test Contact 3",
                    lastMessage = "Договорились",
                    timestamp = System.currentTimeMillis() - 86400000,
                    unreadCount = 0,
                    isOnline = true
                )
            )

            // Используем существующий ChatsAdapter
            val recyclerView = view.findViewById<RecyclerView>(R.id.chatsRecyclerView)
            if (recyclerView != null) {
                val adapter = ChatsAdapter(testChats) { chat ->
                    val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                        putExtra("contact_name", chat.contactName)
                    }
                    startActivity(intent)
                }
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
            }

            // Сохраняем существующие вызовы если они есть
            initViews(view)
            setupRecyclerView()
            loadChats()
        }
    }

    private fun initViews(view: View) {
        performanceMonitor.measure("ChatsFragment.initViews") {
            chatsRecyclerView = view.findViewById(R.id.chatsRecyclerView)
            emptyState = view.findViewById(R.id.emptyState)
        }
    }

    private fun setupRecyclerView() {
        performanceMonitor.measure("ChatsFragment.setupRecyclerView") {
            // TODO: Настроить адаптер чатов
            // chatsRecyclerView.adapter = ChatAdapter()
            // chatsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadChats() {
        performanceMonitor.measure("ChatsFragment.loadChats") {
            // TODO: Загрузить список чатов из репозитория
            // val chats = chatRepository.getChats()
            // updateUI(chats)
        }
    }

    private fun updateUI(chats: List<Any>) {
        performanceMonitor.measure("ChatsFragment.updateUI") {
            if (chats.isEmpty()) {
                showEmptyState(true)
            } else {
                showEmptyState(false)
                // adapter.submitList(chats)
            }
        }
    }

    private fun showEmptyState(show: Boolean) {
        emptyState.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        performanceMonitor.logPerformanceEvent("ChatsFragment resumed")
    }

    override fun onPause() {
        super.onPause()
        performanceMonitor.logPerformanceEvent("ChatsFragment paused")
    }
}