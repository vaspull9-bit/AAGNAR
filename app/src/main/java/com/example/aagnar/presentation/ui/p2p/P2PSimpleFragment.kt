package com.example.aagnar.presentation.ui.p2p

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.aagnar.R
import com.example.aagnar.presentation.viewmodel.P2PViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class P2PSimpleFragment : Fragment() {

    private val viewModel: P2PViewModel by viewModels()
    private lateinit var statusText: TextView
    private lateinit var messagesText: TextView
    private lateinit var messageInput: EditText
    private lateinit var ipInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_p2p_simple, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statusText = view.findViewById(R.id.statusText)
        messagesText = view.findViewById(R.id.messagesText)
        messageInput = view.findViewById(R.id.messageInput)
        ipInput = view.findViewById(R.id.ipInput)

        view.findViewById<Button>(R.id.connectButton).setOnClickListener {
            val ip = ipInput.text.toString().trim()
            if (ip.isNotEmpty()) {
                viewModel.connectToServer(ip)
            } else {
                viewModel.connectToServer("192.168.88.240")
            }
        }

        view.findViewById<Button>(R.id.sendButton).setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                messageInput.text.clear()
            }
        }

        view.findViewById<Button>(R.id.disconnectButton).setOnClickListener {
            viewModel.disconnect()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.connectionState.collect { state ->
                statusText.text = "Status: $state"
            }
        }

        lifecycleScope.launch {
            viewModel.messages.collect { messages ->
                messagesText.text = messages.joinToString("\n")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // При возвращении на фрагмент проверяем состояние
        if (viewModel.connectionState.value == "Connected") {
            // Можно показать сообщение что подключение активно
            statusText.text = "Status: Connected (active)"
        }
    }

}