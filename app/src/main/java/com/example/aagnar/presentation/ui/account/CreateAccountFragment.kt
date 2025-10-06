package com.example.aagnar.presentation.ui.account

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.aagnar.R
import com.example.aagnar.databinding.FragmentCreateAccountBinding
import com.example.aagnar.domain.model.AudioCodec
import com.example.aagnar.domain.model.VideoCodec
import com.example.aagnar.domain.model.ProtocolType
import com.example.aagnar.presentation.util.showToast
//import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.aagnar.presentation.viewmodel.AccountViewModel
import com.example.aagnar.presentation.util.showToast


//@AndroidEntryPoint
class CreateAccountFragment : Fragment() {

    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AccountViewModel by viewModels()



//    private fun Fragment.showToast(message: String) {
//        requireContext().showToast(message)
//    }
    private val requiredPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupSpinners()
        observeViewModel()
        checkPermissions()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnCreateAccount.setOnClickListener {
            if (validateInput()) {
                createAccount()
            }
        }

        binding.btnAdvancedSettings.setOnClickListener {
            toggleAdvancedSettings()
        }

        binding.cbUseProxy.setOnCheckedChangeListener { _, isChecked ->
            binding.tilProxyServer.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.tilProxyPort.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun setupSpinners() {
        // Audio codecs spinner
        val audioCodecs = AudioCodec.values().map { it.displayName }
        val audioAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, audioCodecs)
        audioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spAudioCodec.adapter = audioAdapter
        binding.spAudioCodec.setSelection(audioCodecs.indexOf(AudioCodec.OPUS.displayName))

        // Video codecs spinner
        val videoCodecs = VideoCodec.values().map { it.displayName }
        val videoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, videoCodecs)
        videoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spVideoCodec.adapter = videoAdapter
        binding.spVideoCodec.setSelection(videoCodecs.indexOf(VideoCodec.VP8.displayName))

        // Protocol type spinner
        val protocols = ProtocolType.values().map { it.name }
        val protocolAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, protocols)
        protocolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spProtocol.adapter = protocolAdapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.btnCreateAccount.isEnabled = !state.isLoading

                when {
                    state.error != null -> {
                        binding.tvError.text = state.error
                        binding.tvError.visibility = View.VISIBLE
                        requireContext().showToast(state.error ?: "Unknown error")
                    }
                    state.accountCreated -> {
                        binding.tvError.visibility = View.GONE
                        requireContext().showToast("Account created successfully")
                        findNavController().popBackStack()
                    }
                    else -> {
                        binding.tvError.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val server = binding.etServer.text.toString().trim()

        // Validate username
        if (username.isEmpty()) {
            binding.tilUsername.error = "Username is required"
            isValid = false
        } else if (username.length < 3) {
            binding.tilUsername.error = "Username must be at least 3 characters"
            isValid = false
        } else {
            binding.tilUsername.error = null
        }

        // Validate password
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        // Validate server (optional but if provided, check format)
        if (server.isNotEmpty() && !isValidServer(server)) {
            binding.tilServer.error = "Invalid server format"
            isValid = false
        } else {
            binding.tilServer.error = null
        }

        // Validate proxy settings if enabled
        if (binding.cbUseProxy.isChecked) {
            val proxyServer = binding.etProxyServer.text.toString().trim()
            val proxyPort = binding.etProxyPort.text.toString().trim()

            if (proxyServer.isEmpty()) {
                binding.tilProxyServer.error = "Proxy server is required"
                isValid = false
            } else {
                binding.tilProxyServer.error = null
            }

            if (proxyPort.isEmpty()) {
                binding.tilProxyPort.error = "Proxy port is required"
                isValid = false
            } else if (!proxyPort.matches(Regex("\\d+")) || proxyPort.toInt() !in 1..65535) {
                binding.tilProxyPort.error = "Invalid port number"
                isValid = false
            } else {
                binding.tilProxyPort.error = null
            }
        }

        return isValid
    }

    private fun isValidServer(server: String): Boolean {
        return server.matches(Regex("^[a-zA-Z0-9.-]+(:\\d+)?\$")) ||
                server.matches(Regex("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d+)?\$"))
    }

    private fun createAccount() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val server = binding.etServer.text.toString().trim()
        val audioCodec = AudioCodec.values()[binding.spAudioCodec.selectedItemPosition]
        val videoCodec = VideoCodec.values()[binding.spVideoCodec.selectedItemPosition]
        val protocol = ProtocolType.values()[binding.spProtocol.selectedItemPosition]

        val proxyServer = if (binding.cbUseProxy.isChecked) {
            binding.etProxyServer.text.toString().trim()
        } else null

        val proxyPort = if (binding.cbUseProxy.isChecked) {
            binding.etProxyPort.text.toString().trim().toIntOrNull()
        } else null

        val enableVideo = binding.cbEnableVideo.isChecked
        val enableEncryption = binding.cbEnableEncryption.isChecked

        viewModel.createAccount(
            username = username,
            password = password,
            server = server.ifEmpty { null },
            protocol = protocol,
            audioCodec = audioCodec,
            videoCodec = videoCodec,
            proxyServer = proxyServer,
            proxyPort = proxyPort,
            enableVideo = enableVideo,
            enableEncryption = enableEncryption
        )
    }

    private fun toggleAdvancedSettings() {
        val isVisible = binding.advancedSettings.visibility == View.VISIBLE
        binding.advancedSettings.visibility = if (isVisible) View.GONE else View.VISIBLE
        binding.btnAdvancedSettings.text = if (isVisible) "Show Advanced" else "Hide Advanced"
    }

    private fun checkPermissions() {
        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissions(missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (!allGranted) {
                requireContext().showToast("Some permissions are required for VoIP functionality")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}