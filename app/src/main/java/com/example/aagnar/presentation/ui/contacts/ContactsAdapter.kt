package com.example.aagnar.presentation.ui.contacts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aagnar.R

class ContactsAdapter(
    private val contacts: List<Contact>,
    private val onContactClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

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
        private val usernameText: TextView = itemView.findViewById(R.id.usernameText)
        // private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator) // Временно закомментируем

        fun bind(contact: Contact) {
            usernameText.text = contact.username

            // Временно убираем статусы
            // val statusDrawable = when (contact.status) {
            //     Status.ONLINE -> R.drawable.status_online
            //     Status.OFFLINE -> R.drawable.status_offline
            //     Status.UNREGISTERED -> R.drawable.status_unregistered
            // }
            // statusIndicator.setBackgroundResource(statusDrawable)
        }
    }
}