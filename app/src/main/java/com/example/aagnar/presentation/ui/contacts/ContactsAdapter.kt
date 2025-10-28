package com.example.aagnar.presentation.ui.contacts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aagnar.R
import com.example.aagnar.domain.model.Contact
import com.example.aagnar.domain.model.Status
import kotlin.math.absoluteValue

class ContactsAdapter(
    private var contacts: List<Contact>,
    private val onContactClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {


    fun updateContacts(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact)
        holder.itemView.setOnClickListener { onContactClick(contact) }
    }

    override fun getItemCount() = contacts.size

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameText: TextView = itemView.findViewById(R.id.contactName)
        private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)

        fun bind(contact: Contact) {
            usernameText.text = contact.name

            val statusDrawable = when (contact.status) {
                Status.ONLINE -> R.drawable.status_online
                Status.OFFLINE -> R.drawable.status_offline
                Status.UNREGISTERED -> R.drawable.status_unregistered
            }
            statusIndicator.setBackgroundResource(statusDrawable)
        }
    }
}