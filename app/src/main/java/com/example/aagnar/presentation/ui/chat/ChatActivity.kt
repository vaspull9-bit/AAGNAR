package com.example.aagnar.presentation.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aagnar.R
import com.example.aagnar.databinding.ActivityChatBinding
import com.example.aagnar.domain.model.Message
import com.example.aagnar.domain.model.MessageType
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var messagesAdapter: MessagesAdapter

    private var contactName: String = ""


    // Добавляем в класс ChatActivity

    private var isRecording = false
    private val recordingHandler = Handler(Looper.getMainLooper())
    private val recordingRunnable = object : Runnable {
        override fun run() {
            if (isRecording) {
                val duration = audioViewModel.getRecordingDuration()
                updateRecordingUI(duration)
                recordingHandler.postDelayed(this, 1000)
            }
        }
    }

    // Добавляем кнопку записи в layout
    private fun setupVoiceRecording() {
        binding.voiceRecordButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startRecording()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> stopRecording()
            }
            true
        }
    }

    private fun startRecording() {
        val result = audioViewModel.startRecording()
        if (result.isSuccess) {
            isRecording = true
            showRecordingUI()
            recordingHandler.post(recordingRunnable)
        } else {
            showMessage("Ошибка записи: ${result.exceptionOrNull()?.message}")
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            isRecording = false
            recordingHandler.removeCallbacks(recordingRunnable)

            val result = audioViewModel.stopRecording()
            if (result.isSuccess) {
                result.getOrNull()?.let { (audioData, duration) ->
                    if (duration >= 1) { // Минимум 1 секунда
                        viewModel.sendVoiceMessage(contactName, audioData, duration)
                    } else {
                        showMessage("Сообщение слишком короткое")
                    }
                }
            } else {
                showMessage("Ошибка сохранения записи")
            }

            hideRecordingUI()
        }
    }

    private fun showRecordingUI() {
        binding.recordingLayout.visibility = View.VISIBLE
        binding.messageInputLayout.visibility = View.GONE
    }

    private fun hideRecordingUI() {
        binding.recordingLayout.visibility = View.GONE
        binding.messageInputLayout.visibility = View.VISIBLE
    }

    private fun updateRecordingUI(duration: Int) {
        binding.recordingDuration.text = formatDuration(duration)
        binding.recordingWaveView.addAmplitude(getRandomAmplitude())
    }

    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    private fun getRandomAmplitude(): Float {
        return (Math.random() * 0.6 + 0.2).toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contactName = intent.getStringExtra("contact_name") ?: "Unknown"
        setupUI()
        setupRecyclerView()
        setupClickListeners()

        // Добавляем в onCreate после setupClickListeners()
        private fun setupTypingListener() {
            binding.messageInput.addTextChangedListener(object : android.text.TextWatcher {
                private var typingTimer: android.os.CountDownTimer? = null

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Пользователь начал печатать
                    viewModel.setTyping(true)

                    // Сбрасываем таймер
                    typingTimer?.cancel()

                    // Запускаем таймер для отправки "перестал печатать"
                    typingTimer = object : android.os.CountDownTimer(1000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {}
                        override fun onFinish() {
                            viewModel.setTyping(false)
                        }
                    }.start()
                }

                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }

        // Обновляем observeViewModel()
        private fun observeViewModel() {
            viewModel.messages.observe(this) { messages ->
                messagesAdapter.updateMessages(messages)
                binding.messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
            }

            viewModel.isConnected.observe(this) { isConnected ->
                binding.connectionStatus.visibility = if (isConnected) View.GONE else View.VISIBLE
            }

            viewModel.isTyping.observe(this) { isTyping ->
                // TODO: Показать индикатор набора текста
                if (isTyping) {
                    showMessage("$contactName печатает...")
                }
            }
        }

// Вызываем в onCreate после observeViewModel()
        setupTypingListener()


        observeViewModel()
    }

    private fun setupUI() {


        // Добавляем в setupUI()
        binding.attachButton.setOnClickListener {
            showAttachmentDialog()
        }

        private fun showAttachmentDialog() {
            val options = arrayOf("Галерея", "Файлы", "Камера")

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Прикрепить файл")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> openGallery()
                        1 -> openFilePicker()
                        2 -> openCamera()
                    }
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        private fun openGallery() {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_GALLERY)
        }

        private fun openFilePicker() {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(intent, REQUEST_FILE)
        }

        private fun openCamera() {
            // TODO: Реализовать съемку фото
            showMessage("Функция камеры в разработке")
        }

        // Обработка результата выбора файла
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if (resultCode == RESULT_OK && data != null) {
                when (requestCode) {
                    REQUEST_GALLERY, REQUEST_FILE -> {
                        data.data?.let { uri ->
                            viewModel.sendFile(contactName, uri)
                        }
                    }
                }
            }
        }

// Добавляем константы
        companion object {
            private const val REQUEST_GALLERY = 1001
            private const val REQUEST_FILE = 1002
        }

        // Добавляем в setupUI()
        binding.encryptionButton.setOnClickListener {
            showEncryptionDialog()
        }

        private fun showEncryptionDialog() {
            val options = arrayOf("Установить безопасное соединение", "Проверить ключи шифрования")

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Безопасность")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> setupSecureConnection()
                        1 -> checkEncryptionKeys()
                    }
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        private fun setupSecureConnection() {
            viewModel.setupSecureConnection(contactName)
            showMessage("Устанавливается безопасное соединение...")
        }

        private fun checkEncryptionKeys() {
            // TODO: Показать информацию о ключах шифрования
            showMessage("Проверка ключей шифрования...")
        }
        binding.toolbarTitle.text = contactName
        binding.backButton.setOnClickListener {
            finish()
        }

        // Кнопки звонков
        binding.voiceCallButton.setOnClickListener {
            initiateVoiceCall()
        }
        binding.videoCallButton.setOnClickListener {
            initiateVideoCall()
        }
    }

    private fun setupRecyclerView() {
        messagesAdapter = MessagesAdapter(emptyList())
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messagesAdapter
        }
    }

    private fun setupClickListeners() {
        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        binding.messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(this) { messages ->
            messagesAdapter.updateMessages(messages)
            binding.messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
        }

        viewModel.isConnected.observe(this) { isConnected ->
            binding.connectionStatus.visibility = if (isConnected) View.GONE else View.VISIBLE
        }
    }

    private fun sendMessage() {
        val messageText = binding.messageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            viewModel.sendMessage(contactName, messageText)
            binding.messageInput.setText("")
        }
    }

    private fun initiateVoiceCall() {
        // TODO: Инициировать голосовой звонок через WebRTC
        showMessage("Инициирование голосового звонка с $contactName")
    }

    private fun initiateVideoCall() {
        // TODO: Инициировать видео звонок через WebRTC
        showMessage("Инициирование видео звонка с $contactName")
    }

    private fun showMessage(text: String) {
        android.widget.Toast.makeText(this, text, android.widget.Toast.LENGTH_SHORT).show()
    }

    // Добавляем в onDestroy()
    override fun onDestroy() {
        super.onDestroy()
        recordingHandler.removeCallbacks(recordingRunnable)
        if (isRecording) {
            audioViewModel.cancelRecording()
        }
    }

}