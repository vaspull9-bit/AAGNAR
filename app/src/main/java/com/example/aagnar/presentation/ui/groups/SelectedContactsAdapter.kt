package com.example.aagnar.presentation.ui.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import com.example.aagnar.R
import com.example.aagnar.domain.model.Contact

class SelectedContactsAdapter(
    private var contacts: List<Contact>,
    private val onRemoveClick: (Contact) -> Unit
) : RecyclerView.Adapter<SelectedContactsAdapter.SelectedContactViewHolder>() {

    inner class SelectedContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: CircleImageView = itemView.findViewById(R.id.contactAvatar)
        private val name: TextView = itemView.findViewById(R.id.contactName)
        private val removeButton: ImageButton = itemView.findViewById(R.id.removeButton)

        fun bind(contact: Contact) {
            name.text = contact.name

            removeButton.setOnClickListener {
                onRemoveClick(contact)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_contact, parent, false)
        return SelectedContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedContactViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int = contacts.size

    fun updateContacts(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
}