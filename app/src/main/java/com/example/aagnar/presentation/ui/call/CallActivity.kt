package com.example.aagnar.presentation.ui.call

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.aagnar.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallActivity : AppCompatActivity() {

    // Views из твоего layout
    private lateinit var remoteVideoView: org.webrtc.SurfaceViewRenderer
    private lateinit var localVideoView: org.webrtc.SurfaceViewRenderer
    private lateinit var contactName: TextView
    private lateinit var callStatus: TextView
    private lateinit var activeCallLayout: LinearLayout
    private lateinit var incomingCallLayout: LinearLayout
    private lateinit var endCallButton: ImageButton
    private lateinit var acceptCallButton: ImageButton
    private lateinit var rejectCallButton: ImageButton
    private lateinit var muteAudioButton: ImageButton
    private lateinit var muteVideoButton: ImageButton
    private lateinit var switchCameraButton: ImageButton

    private val viewModel: CallViewModel by viewModels()

    private var contactNameText: String = ""
    private var isVideoCall: Boolean = true
    private var isIncomingCall: Boolean = false

    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Настройка полноэкранного режима
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_call)

        // Инициализация View
        initViews()

        // Получение данных из Intent
        contactNameText = intent.getStringExtra("contact_name") ?: "Unknown"
        isVideoCall = intent.getBooleanExtra("is_video_call", true)
        isIncomingCall = intent.getBooleanExtra("is_incoming", false)

        setupUI()
        checkPermissions()
    }

    private fun initViews() {
        remoteVideoView = findViewById(R.id.remoteVideoView)
        localVideoView = findViewById(R.id.localVideoView)
        contactName = findViewById(R.id.contactName)
        callStatus = findViewById(R.id.callStatus)
        activeCallLayout = findViewById(R.id.activeCallLayout)
        incomingCallLayout = findViewById(R.id.incomingCallLayout)
        endCallButton = findViewById(R.id.endCallButton)
        acceptCallButton = findViewById(R.id.acceptCallButton)
        rejectCallButton = findViewById(R.id.rejectCallButton)
        muteAudioButton = findViewById(R.id.muteAudioButton)
        muteVideoButton = findViewById(R.id.muteVideoButton)
        switchCameraButton = findViewById(R.id.switchCameraButton)
    }

    private fun setupUI() {
        contactName.text = contactNameText
        callStatus.text = if (isIncomingCall) "Входящий вызов" else "Исходящий вызов"

        // Кнопки управления звонком
        endCallButton.setOnClickListener {
            endCall()
        }

        acceptCallButton.setOnClickListener {
            acceptCall()
        }

        rejectCallButton.setOnClickListener {
            rejectCall()
        }

        muteAudioButton.setOnClickListener {
            toggleAudioMute()
        }

        muteVideoButton.setOnClickListener {
            toggleVideoMute()
        }

        switchCameraButton.setOnClickListener {
            switchCamera()
        }

        // Показываем соответствующие кнопки для входящего/исходящего звонка
        if (isIncomingCall) {
            incomingCallLayout.visibility = View.VISIBLE
            activeCallLayout.visibility = View.GONE
        } else {
            incomingCallLayout.visibility = View.GONE
            activeCallLayout.visibility = View.VISIBLE
        }

        // Скрываем кнопку видео если это аудиозвонок
        if (!isVideoCall) {
            muteVideoButton.visibility = View.GONE
            switchCameraButton.visibility = View.GONE
        }
    }

    private fun checkPermissions() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            initializeCall()
        } else {
            requestPermissions(missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                initializeCall()
            } else {
                showMessage("Разрешения необходимы для совершения звонка")
                finish()
            }
        }
    }

    private fun initializeCall() {
        // Временная заглушка для WebRTC
        callStatus.text = "Инициализация звонка..."

        if (isIncomingCall) {
            // Для входящего звонка ждем подтверждения
            callStatus.text = "Входящий вызов..."
        } else {
            // Инициируем исходящий звонок
            startCall()
        }
    }

    private fun startCall() {
        callStatus.text = "Установка соединения..."
        // TODO: Реализовать начало звонка через WebRTC
    }

    private fun acceptCall() {
        incomingCallLayout.visibility = View.GONE
        activeCallLayout.visibility = View.VISIBLE
        callStatus.text = "Установка соединения..."
        // TODO: Реализовать принятие звонка через WebRTC
    }

    private fun rejectCall() {
        // TODO: Реализовать отклонение звонка
        finish()
    }

    private fun endCall() {
        // TODO: Реализовать завершение звонка
        finish()
    }

    private fun toggleAudioMute() {
        val isMuted = muteAudioButton.isSelected
        muteAudioButton.isSelected = !isMuted
        // Смена иконки
        muteAudioButton.setImageResource(
            if (!isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic_on
        )
        // TODO: Реализовать отключение звука
    }

    private fun toggleVideoMute() {
        val isMuted = muteVideoButton.isSelected
        muteVideoButton.isSelected = !isMuted
        // Смена иконки
        muteVideoButton.setImageResource(
            if (!isMuted) R.drawable.ic_videocam_off else R.drawable.ic_videocam_on
        )
        // TODO: Реализовать отключение видео
    }

    private fun switchCamera() {
        // TODO: Реализовать переключение камеры
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        // Запрещаем возврат во время звонка
        // TODO: Добавить проверку состояния звонка
        super.onBackPressed()
    }
}