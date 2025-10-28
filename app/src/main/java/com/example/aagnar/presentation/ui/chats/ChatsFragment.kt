package com.example.aagnar.presentation.ui.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.aagnar.R
import com.example.aagnar.util.PerformanceMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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