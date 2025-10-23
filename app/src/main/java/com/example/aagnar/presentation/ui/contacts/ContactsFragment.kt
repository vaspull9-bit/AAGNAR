package com.example.aagnar.presentation.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aagnar.databinding.FragmentContactsBinding

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupContactsList()
        loadContacts()
    }

    private fun setupContactsList() {
        val contactsAdapter = ContactsAdapter(emptyList()) { contact ->
            openContact(contact)
        }

        binding.contactsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = contactsAdapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadContacts()
        }
    }

    private fun loadContacts() {
        // TODO: Загрузить контакты из базы данных
        val contacts = listOf(
            Contact("user123", Status.ONLINE, System.currentTimeMillis() - 300000),
            Contact("alice", Status.AWAY, System.currentTimeMillis() - 3600000),
            Contact("bob", Status.OFFLINE, System.currentTimeMillis() - 86400000)
        )

        (binding.contactsRecyclerView.adapter as? ContactsAdapter)?.updateContacts(contacts)
        binding.swipeRefresh.isRefreshing = false

        binding.emptyState.visibility = if (contacts.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openContact(contact: Contact) {
        // TODO: Открыть детали контакта или начать чат
        showMessage("Открыть контакт: ${contact.username}")
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openChat(contactName: String) {
        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra("contact_name", contactName)
        }
        startActivity(intent)
    }

}