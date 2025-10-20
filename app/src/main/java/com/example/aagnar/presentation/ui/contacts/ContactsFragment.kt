package com.example.aagnar.presentation.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.aagnar.R

class ContactsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchInput = view.findViewById<EditText>(R.id.searchInput)
        val contactsList = view.findViewById<RecyclerView>(R.id.contactsRecyclerView)

        // Поиск контактов по логину
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchInput.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchContact(query)
                }
                true
            } else {
                false
            }
        }

        // Показываем список недавних контактов
        showRecentContacts()
    }

    private fun searchContact(username: String) {
        // Поиск контакта по логину
        // Показываем результат поиска
    }

    private fun showRecentContacts() {
        // Показываем список недавних контактов
        // С индикаторами статуса (зеленый/коричневый/красный)
    }
}