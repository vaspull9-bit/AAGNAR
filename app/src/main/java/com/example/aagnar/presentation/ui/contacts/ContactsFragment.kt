package com.example.aagnar.presentation.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.aagnar.databinding.FragmentContactsBinding
import com.example.aagnar.presentation.viewmodel.CallViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CallViewModel by viewModels()

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

        binding.btnCallEcho.setOnClickListener {
            viewModel.makeCall("sip:echo@sip.linphone.org", false)
            requireActivity().onBackPressed() // Возврат к звонку
        }

        binding.btnCallAlice.setOnClickListener {
            viewModel.makeCall("sip:alice@sip.linphone.org", false)
            requireActivity().onBackPressed()
        }

        binding.btnCallBob.setOnClickListener {
            viewModel.makeCall("sip:bob@sip.linphone.org", false)
            requireActivity().onBackPressed()
        }

        binding.btnBackToCall.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}