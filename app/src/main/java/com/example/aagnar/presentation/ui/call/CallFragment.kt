// presentation/ui/call/CallFragment.kt
package com.example.aagnar.presentation.ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import com.example.aagnar.databinding.FragmentCallBinding
import com.example.aagnar.presentation.viewmodel.CallViewModel
import kotlinx.coroutines.launch
import android.graphics.Color
import android.widget.Button


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
        observeCallStatus() // ДОБАВЬ ЭТУ СТРОКУ


        binding.btnMakeCall.setOnClickListener {
            val testNumber = "sip:echo@sip.linphone.org"
            viewModel.makeCall(testNumber, false)
            binding.tvCallStatus.text = "Calling echo test..."
        }



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

        // End Call button
        binding.btnEndCall.setOnClickListener {
            viewModel.endCall()
        }

        binding.btnHold.setOnClickListener {
            viewModel.holdCall()
            updateHoldButtons(true)
        }

        binding.btnUnhold.setOnClickListener {
            viewModel.unholdCall()
            updateHoldButtons(false)
        }
        binding.btnMakeCall.setOnClickListener {
            val number = "sip:test@sip.linphone.org"
            viewModel.makeCall(number, false)
        }


// В метод setupUI() добавляем:
        binding.btnMakeCall.setOnClickListener {
            val testNumber = "sip:echo@sip.linphone.org"  // Тестовый эхо-сервер
            viewModel.makeCall(testNumber, false)
            binding.tvCallStatus.text = "Calling echo test..."
        }

    }

    private fun updateHoldButtons(isHeld: Boolean) {
        updateButtonState(binding.btnHold, isHeld)
        updateButtonState(binding.btnUnhold, !isHeld)
    }

    private fun updateButtonState(button: Button, isActive: Boolean) {
        if (isActive) {
            // Зеленый когда активно
            button.setBackgroundColor(Color.parseColor("#4CAF50"))
            button.setTextColor(Color.WHITE)
        } else {
            // Синий когда неактивно
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
                        binding.tvCallStatus.text = "In call with ${state.contactAddress}"
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

    // ДОБАВЬ ЭТОТ НОВЫЙ МЕТОД:
    private fun observeCallStatus() {
        lifecycleScope.launch {
            viewModel.callStatus.collect { status ->
                // Добавляем статус к основному тексту
                val currentText = binding.tvCallStatus.text.toString()
                if (!currentText.contains("Status:")) {
                    binding.tvCallStatus.text = "$currentText\nStatus: $status"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}