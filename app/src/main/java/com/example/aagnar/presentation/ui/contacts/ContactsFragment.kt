package com.example.aagnar.presentation.ui.contacts

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
class ContactsFragment : Fragment() {

    @Inject
    lateinit var performanceMonitor: PerformanceMonitor
    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var emptyState: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return performanceMonitor.measure("ContactsFragment.onCreateView") {
            inflater.inflate(R.layout.fragment_contacts, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        performanceMonitor.measure("ContactsFragment.onViewCreated") {
            initViews(view)
            setupRecyclerView()
            loadContacts()
        }
    }

    private fun initViews(view: View) {
        performanceMonitor.measure("ContactsFragment.initViews") {
            contactsRecyclerView = view.findViewById(R.id.contactsRecyclerView)
            emptyState = view.findViewById(R.id.emptyState)
        }
    }

    private fun setupRecyclerView() {
        performanceMonitor.measure("ContactsFragment.setupRecyclerView") {
            // TODO: Настроить адаптер контактов
        }
    }

    private fun loadContacts() {
        performanceMonitor.measure("ContactsFragment.loadContacts") {
            // TODO: Загрузить список контактов
        }
    }

    private fun showEmptyState(show: Boolean) {
        emptyState.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        performanceMonitor.logPerformanceEvent("ContactsFragment resumed")
    }
}