// ФАЙЛ: presentation/ui/contacts/AddContactDialog.kt
package com.example.aagnar.presentation.ui.contacts

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.aagnar.R

class AddContactDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_add_contact, null)

            val usernameInput = view.findViewById<EditText>(R.id.usernameInput)

            builder.setView(view)
                .setTitle("Добавить контакт")
                .setPositiveButton("Добавить") { _, _ ->
                    val username = usernameInput.text.toString().trim()
                    if (username.isNotEmpty()) {
                        // TODO: Отправить invite через WebSocket
                        // Пока просто покажем сообщение
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Запрос отправлен пользователю: $username",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("Отмена", null)

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}