// presentation/ui/call/CallFragment.kt
package com.example.aagnar.presentation.ui.call

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.aagnar.databinding.FragmentCallBinding
import com.example.aagnar.presentation.viewmodel.CallViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CallFragment : Fragment() {

    private var _binding: FragmentCallBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CallViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeCallState()
        observeSipStatus()
        observeCallDuration() // ДОБАВИЛ ТАЙМЕР
        // В onViewCreated() после observeCallDuration():
        // В onViewCreated() после observeCallDuration():
        lifecycleScope.launch {
            viewModel.callState.collect { state ->
                if (state is com.example.aagnar.presentation.viewmodel.CallState.Active) {
                    updateCallHistory(state.contactAddress)
                }
            }
        }
    }

    // Новый метод:
    private fun updateCallHistory(contact: String) {
        val displayName = contact.replace("sip:", "").replace("@sip.linphone.org", "")
        val currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val currentText = binding.tvCallHistory.text.toString()

        // Ищем существующую запись
        val regex = "- $displayName.*".toRegex()
        val match = regex.find(currentText)

        val newEntry = if (match != null) {
            val oldEntry = match.value
            val countMatch = ".*\\((\\d+)\\)".toRegex().find(oldEntry)
            val count = countMatch?.groupValues?.get(1)?.toInt() ?: 1
            "- $displayName (${count + 1}) - $currentTime"
        } else {
            "- $displayName - $currentTime"
        }

        // Обновляем историю
        val updatedHistory = if (match != null) {
            currentText.replace(regex, newEntry)
        } else {
            val entries = currentText.replace("Recent calls:\n", "").split("\n").toMutableList()
            entries.add(0, newEntry)
            // Ограничиваем 5 записями
            if (entries.size > 5) entries.removeAt(entries.size - 1)
            "Recent calls:\n${entries.joinToString("\n")}"
        }

        binding.tvCallHistory.text = updatedHistory
    }

    private fun setupUI() {
        // Mute button
        binding.btnMute.setOnClickListener {
            viewModel.toggleMute()
            updateButtonState(binding.btnMute, viewModel.isMuted.value)
        }

        // Speaker button
        binding.btnSpeaker.setOnClickListener {
            viewModel.toggleSpeaker()
            updateButtonState(binding.btnSpeaker, viewModel.isSpeakerOn.value)
        }

        // Video button
        binding.btnVideo.setOnClickListener {
            viewModel.toggleVideo()
            updateButtonState(binding.btnVideo, viewModel.isVideoOn.value)
        }

        // Hold button
        binding.btnHold.setOnClickListener {
            viewModel.holdCall()
            updateHoldButtons(true)
        }

        // Unhold button
        binding.btnUnhold.setOnClickListener {
            viewModel.unholdCall()
            updateHoldButtons(false)
        }

        // Make Call button
        binding.btnMakeCall.setOnClickListener {
            val testNumber = "sip:echo@sip.linphone.org"
            viewModel.makeCall(testNumber, false)
            binding.tvCallStatus.text = "Calling echo test..."
        }

        // End Call button
        binding.btnEndCall.setOnClickListener {
            viewModel.endCall()
        }

        binding.btnCallContact.setOnClickListener {
            val aliceNumber = "sip:alice@sip.linphone.org"
            viewModel.makeCall(aliceNumber, false)
            binding.tvCallStatus.text = "Calling Alice..."
        }

        binding.btnClearHistory.setOnClickListener {
            binding.tvCallHistory.text = "Recent calls:"
        }

        binding.btnContacts.setOnClickListener {
            val contactsFragment = com.example.aagnar.presentation.ui.contacts.ContactsFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, contactsFragment) // СИСТЕМНЫЙ КОНТЕЙНЕР
                .addToBackStack(null)
                .commit()
        }

    }

    private fun updateHoldButtons(isHeld: Boolean) {
        updateButtonState(binding.btnHold, isHeld)
        updateButtonState(binding.btnUnhold, !isHeld)
    }

    private fun updateButtonState(button: Button, isActive: Boolean) {
        if (isActive) {
            button.setBackgroundColor(Color.parseColor("#4CAF50"))
            button.setTextColor(Color.WHITE)
        } else {
            button.setBackgroundColor(Color.parseColor("#2196F3"))
            button.setTextColor(Color.WHITE)
        }
    }

    private fun observeCallState() {
        lifecycleScope.launch {
            viewModel.callState.collect { state ->
                when (state) {
                    is com.example.aagnar.presentation.viewmodel.CallState.Idle -> {
                        binding.tvCallStatus.text = "Call ended"
                    }
                    is com.example.aagnar.presentation.viewmodel.CallState.Connecting -> {
                        binding.tvCallStatus.text = "Calling ${state.contactAddress}..."
                    }
                    is com.example.aagnar.presentation.viewmodel.CallState.Active -> {
                        val displayName = state.contactAddress.replace("sip:", "").replace("@sip.linphone.org", "")
                        binding.tvCallStatus.text = "✅ LIVE CALL\nWith: $displayName"
                    }
                    is com.example.aagnar.presentation.viewmodel.CallState.Incoming -> {
                        binding.tvCallStatus.text = "Incoming from ${state.caller}"
                    }
                    is com.example.aagnar.presentation.viewmodel.CallState.Disconnected -> {
                        binding.tvCallStatus.text = "Call disconnected"
                    }
                }
            }
        }
    }

    // ДОБАВИЛ ТАЙМЕР
    private fun observeCallDuration() {
        lifecycleScope.launch {
            viewModel.callDuration.collect { duration ->
                val currentText = binding.tvCallStatus.text.toString()
                if (currentText.contains("In call with")) {
                    // ИСПРАВЛЯЕМ ЭТУ СТРОКУ:
                    when (val state = viewModel.callState.value) {
                        is com.example.aagnar.presentation.viewmodel.CallState.Active -> {
                            binding.tvCallStatus.text = "In call with ${state.contactAddress}\nDuration: ${duration}s"
                        }
                        else -> {
                            binding.tvCallStatus.text = "In call\nDuration: ${duration}s"
                        }
                    }
                }
            }
        }
    }

    private fun observeSipStatus() {
        lifecycleScope.launch {
            while (true) {
                val sipStatus = (requireActivity().application as com.example.aagnar.AagnarApplication)
                    .linphoneService.getRegistrationStatus()
                val currentText = binding.tvCallStatus.text.toString()
                if (!currentText.contains("SIP:")) {
                    binding.tvCallStatus.text = "$currentText\nSIP: $sipStatus"
                }
                kotlinx.coroutines.delay(5000) // Обновляем каждые 5 секунд
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}