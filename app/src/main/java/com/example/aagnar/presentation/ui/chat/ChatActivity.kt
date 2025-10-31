package com.example.aagnar.presentation.ui.chat

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aagnar.R
import com.example.aagnar.domain.model.FileInfo
import com.example.aagnar.domain.model.Message
import com.example.aagnar.domain.model.MessageType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var toolbarTitle: TextView
    private lateinit var voiceCallButton: ImageButton
    private lateinit var videoCallButton: ImageButton
    private lateinit var attachButton: ImageButton
    private lateinit var encryptionButton: ImageButton
    private lateinit var connectionStatus: TextView
    private lateinit var voiceRecordButton: ImageButton
    private lateinit var recordingLayout: LinearLayout
    private lateinit var messageInputLayout: LinearLayout
    private lateinit var recordingDuration: TextView

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var messagesAdapter: MessagesAdapter

    private var contactName: String = ""
    private var isRecording = false
    private val recordingHandler = Handler(Looper.getMainLooper())
    private val recordingRunnable = object : Runnable {
        override fun run() {
            if (isRecording) {
                val duration = 0 // TODO: get from audioViewModel
                updateRecordingUI(duration)
                recordingHandler.postDelayed(this, 1000)
            }
        }
    }

    companion object {
        private const val REQUEST_GALLERY = 1001
        private const val REQUEST_FILE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        initViews()

        contactName = intent.getStringExtra("contact_name") ?: "Unknown"
        setupUI()
        setupRecyclerView()
        setupClickListeners()
        setupVoiceRecording()
        setupTypingListener()
        observeViewModel()
    }

    private fun initViews() {
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        backButton = findViewById(R.id.backButton)
        toolbarTitle = findViewById(R.id.toolbarTitle)
        voiceCallButton = findViewById(R.id.voiceCallButton)
        videoCallButton = findViewById(R.id.videoCallButton)
        attachButton = findViewById(R.id.attachButton)
        encryptionButton = findViewById(R.id.encryptionButton)
        connectionStatus = findViewById(R.id.connectionStatus)
        voiceRecordButton = findViewById(R.id.voiceRecordButton)
        recordingLayout = findViewById(R.id.recordingLayout)
        messageInputLayout = findViewById(R.id.messageInputLayout)
        recordingDuration = findViewById(R.id.recordingDuration)
    }

    private fun setupUI() {
        toolbarTitle.text = contactName
        backButton.setOnClickListener {
            finish()
        }

        voiceCallButton.setOnClickListener {
            initiateVoiceCall()
        }
        videoCallButton.setOnClickListener {
            initiateVideoCall()
        }

        attachButton.setOnClickListener {
            showAttachmentDialog()
        }

        encryptionButton.setOnClickListener {
            showEncryptionDialog()
        }
    }

    private fun setupRecyclerView() {
        // ФИКС: Создаем адаптер с правильными параметрами
        messagesAdapter = MessagesAdapter(
            messages = emptyList(),
            onMessageClick = { message ->
                handleMessageClick(message)
            },
            onFileClick = { message ->
                handleMessageClick(message)
            },
            audioViewModel = null
        )

        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.adapter = messagesAdapter
    }

    private fun handleMessageClick(message: Message) {
        when (message.type) {
            MessageType.FILE -> openAttachment(message)
            MessageType.AUDIO -> playVoiceMessage(message)
            MessageType.IMAGE -> openAttachment(message)  // ← ИСПОЛЬЗУЕМ СУЩЕСТВУЮЩИЙ МЕТОД
            MessageType.VIDEO -> openAttachment(message)  // ← ИСПОЛЬЗУЕМ СУЩЕСТВУЮЩИЙ МЕТОД

            else -> showMessageActions(message)
        }
    }

    private fun openAttachment(message: Message) {
        // TODO: Открытие вложения
    }

    private fun playVoiceMessage(message: Message) {
        // TODO: Воспроизведение голосового сообщения
    }

    private fun showMessageActions(message: Message) {
        // TODO: Показать меню действий с сообщением
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            sendMessage()
        }

        messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun setupTypingListener() {
        messageInput.addTextChangedListener(object : TextWatcher {
            private var typingTimer: android.os.CountDownTimer? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setTyping(true)
                typingTimer?.cancel()
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

    private fun observeViewModel() {
        viewModel.messages.observe(this) { messages ->
            messagesAdapter.updateMessages(messages)
            messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
        }

        viewModel.isConnected.observe(this) { isConnected ->
            connectionStatus.visibility = if (isConnected) View.GONE else View.VISIBLE
        }

        viewModel.isTyping.observe(this) { isTyping ->
            if (isTyping) {
                showMessage("$contactName печатает...")
            }
        }
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            viewModel.sendMessage(contactName, messageText)
            messageInput.setText("")
        }
    }

    private fun initiateVoiceCall() {
        showMessage("Инициирование голосового звонка с $contactName")
    }

    private fun initiateVideoCall() {
        showMessage("Инициирование видео звонка с $contactName")
    }

    private fun showMessage(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    // Voice recording methods
    private fun setupVoiceRecording() {
        voiceRecordButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startRecording()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stopRecording()
                }
            }
            true
        }
    }

    private fun startRecording() {
        isRecording = true
        showRecordingUI()
        recordingHandler.post(recordingRunnable)
    }

    private fun stopRecording() {
        if (isRecording) {
            isRecording = false
            recordingHandler.removeCallbacks(recordingRunnable)
            hideRecordingUI()
        }
    }

    private fun showRecordingUI() {
        recordingLayout.visibility = View.VISIBLE
        messageInputLayout.visibility = View.GONE
    }

    private fun hideRecordingUI() {
        recordingLayout.visibility = View.GONE
        messageInputLayout.visibility = View.VISIBLE
    }

    private fun updateRecordingUI(duration: Int) {
        recordingDuration.text = formatDuration(duration)
    }

    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    // Attachment methods
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

    private fun openFile(fileInfo: FileInfo) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileInfo.uri, fileInfo.type)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            showMessage("Не удалось открыть файл")
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, REQUEST_FILE)
    }

    private fun openCamera() {
        showMessage("Функция камеры в разработке")
    }

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

    // Encryption methods
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
        showMessage("Проверка ключей шифрования...")
    }

    override fun onDestroy() {
        super.onDestroy()
        recordingHandler.removeCallbacks(recordingRunnable)
        if (isRecording) {
            // audioViewModel.cancelRecording()
        }
    }
}