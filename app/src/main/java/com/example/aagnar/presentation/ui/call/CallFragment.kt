// presentation/ui/call/CallFragment.kt
package com.example.aagnar.presentation.ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
// import dagger.hilt.android.AndroidEntryPoint
import com.example.aagnar.databinding.FragmentCallBinding
import com.example.aagnar.presentation.viewmodel.CallViewModel
import kotlinx.coroutines.launch

// @AndroidEntryPoint  // РАСКОММЕНТИРОВАТЬ
class CallFragment : Fragment() {

    private var _binding: FragmentCallBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CallViewModel by viewModels()  // РАСКОММЕНТИРОВАТЬ

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
        observeCallState()  // РАСКОММЕНТИРОВАТЬ
    }

    private fun setupUI() {
        binding.btnEndCall.setOnClickListener {
            viewModel.endCall()
        }

        binding.btnMute.setOnClickListener {
            viewModel.toggleMute()
        }

        binding.btnSpeaker.setOnClickListener {
            viewModel.toggleSpeaker()
        }

        binding.btnVideo.setOnClickListener {
            viewModel.toggleVideo()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}