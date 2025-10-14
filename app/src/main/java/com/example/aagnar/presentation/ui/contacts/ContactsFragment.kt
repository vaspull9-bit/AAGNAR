package com.example.aagnar.presentation.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.aagnar.R
import com.example.aagnar.presentation.viewmodel.CallViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactsFragment : Fragment() {

    private val viewModel: CallViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("ContactsFragment created - SIMPLE VERSION")

        // Базовые кнопки
        view.findViewById<View>(R.id.btnCallEcho)?.setOnClickListener {
            viewModel.makeCall("sip:echo@sip.linphone.org", false)
            requireActivity().onBackPressed()
        }

        view.findViewById<View>(R.id.btnBackToCall)?.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}