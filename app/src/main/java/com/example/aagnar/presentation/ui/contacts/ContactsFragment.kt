package com.example.aagnar.presentation.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.aagnar.R
import com.example.aagnar.util.PerformanceMonitor

class ContactsFragment : Fragment() {

    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var emptyState: View
    private lateinit var searchView: android.widget.SearchView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        PerformanceMonitor.measure("ContactsFragment.onViewCreated") {
            initViews(view)
            setupRecyclerView()
            loadContacts()
        }
    }

    private fun initViews(view: View) {
        contactsRecyclerView = view.findViewById(R.id.contactsRecyclerView)
        emptyState = view.findViewById(R.id.emptyState)
        searchView = view.findViewById(R.id.searchView)
    }

    private fun setupRecyclerView() {
        // TODO: Настроить адаптер контактов
    }

    private fun loadContacts() {
        // TODO: Загрузить список контактов
    }
}