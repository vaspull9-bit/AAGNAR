package com.example.aagnar.presentation.ui.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.aagnar.R
import com.example.aagnar.util.PerformanceMonitor

class ChatsFragment : Fragment() {

    private lateinit var chatsRecyclerView: RecyclerView
    private lateinit var emptyState: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        PerformanceMonitor.measure("ChatsFragment.onViewCreated") {
            initViews(view)
            setupRecyclerView()
            loadChats()
        }
    }

    private fun initViews(view: View) {
        chatsRecyclerView = view.findViewById(R.id.chatsRecyclerView)
        emptyState = view.findViewById(R.id.emptyState)
    }

    private fun setupRecyclerView() {
        // TODO: Настроить адаптер чатов
    }

    private fun loadChats() {
        // TODO: Загрузить список чатов
    }
}