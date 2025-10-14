package com.example.aagnar.presentation.ui.matrix

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.aagnar.R
import com.example.aagnar.presentation.viewmodel.MatrixViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MatrixFragment : Fragment() {

    @Inject
    lateinit var matrixService: com.example.aagnar.domain.service.MatrixService
    private val viewModel: MatrixViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_matrix, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            matrixService.initialize()
        }
        setupUI(view)
        observeState(view)
    }

    private fun setupUI(view: View) {
        viewModel.initializeMatrix()

        view.findViewById<View>(R.id.btnInitialize).setOnClickListener {
            viewModel.initializeMatrix()
            println("Initialize clicked!")
        }

        view.findViewById<View>(R.id.btnLogin).setOnClickListener {
            viewModel.login("test_user", "test_pass")
            println("Login clicked!")
        }

        view.findViewById<View>(R.id.btnSendMessage).setOnClickListener {
            viewModel.sendMessage("test_peer", "Hello Matrix!")
            println("Send message clicked!")
        }

        view.findViewById<View>(R.id.btnTestCall).setOnClickListener {
            viewModel.startCall("test_peer", false)
            println("Test call clicked!")
        }
    }

    private fun observeState(view: View) {
        lifecycleScope.launch {
            viewModel.connectionState.collect { state ->
                view.findViewById<android.widget.TextView>(R.id.tvStatus).text = "Status: $state"
                updateStatusColor(view, state)
            }
        }

        lifecycleScope.launch {
            viewModel.messages.collect { messages ->
                view.findViewById<android.widget.TextView>(R.id.tvMessages).text = "Messages: ${messages.size}"
            }
        }
    }

    private fun updateStatusColor(view: View, state: com.example.aagnar.domain.service.MatrixState) {
        val color = when (state) {
            is com.example.aagnar.domain.service.MatrixState.Connected -> "#4CAF50" // зеленый
            is com.example.aagnar.domain.service.MatrixState.Registered -> "#2196F3" // синий
            is com.example.aagnar.domain.service.MatrixState.Error -> "#F44336" // красный
            else -> "#9E9E9E" // серый
        }
        view.findViewById<android.widget.TextView>(R.id.tvStatus).setTextColor(android.graphics.Color.parseColor(color))
    }
}