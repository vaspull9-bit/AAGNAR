package com.example.aagnar.presentation.ui.call

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.aagnar.databinding.ActivityCallBinding
import com.example.aagnar.webrtc.SignalingClient
import com.example.aagnar.webrtc.WebRTCManager
import dagger.hilt.android.AndroidEntryPoint
import org.webrtc.*

@AndroidEntryPoint
class CallActivity : AppCompatActivity(), WebRTCManager.SignalingListener {

    private lateinit var binding: ActivityCallBinding
    private val viewModel: CallViewModel by viewModels()

    private lateinit var webRTCManager: WebRTCManager
    private lateinit var signalingClient: SignalingClient

    private var contactName: String = ""
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

        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contactName = intent.getStringExtra("contact_name") ?: "Unknown"
        isVideoCall = intent.getBooleanExtra("is_video_call", true)
        isIncomingCall = intent.getBooleanExtra("is_incoming", false)

        setupUI()
        checkPermissions()
    }

    private fun setupUI() {
        binding.contactName.text = contactName
        binding.callStatus.text = if (isIncomingCall) "Входящий вызов" else "Исходящий вызов"

        // Кнопки управления звонком
        binding.endCallButton.setOnClickListener {
            endCall()
        }

        binding.acceptCallButton.setOnClickListener {
            acceptCall()
        }

        binding.rejectCallButton.setOnClickListener {
            rejectCall()
        }

        binding.muteAudioButton.setOnClickListener {
            toggleAudioMute()
        }

        binding.muteVideoButton.setOnClickListener {
            toggleVideoMute()
        }

        binding.switchCameraButton.setOnClickListener {
            switchCamera()
        }

        // Показываем соответствующие кнопки для входящего/исходящего звонка
        if (isIncomingCall) {
            binding.incomingCallLayout.visibility = View.VISIBLE
            binding.activeCallLayout.visibility = View.GONE
        } else {
            binding.incomingCallLayout.visibility = View.GONE
            binding.activeCallLayout.visibility = View.VISIBLE
        }

        // Скрываем кнопку видео если это аудиозвонок
        if (!isVideoCall) {
            binding.muteVideoButton.visibility = View.GONE
            binding.switchCameraButton.visibility = View.GONE
        }
    }

    private fun checkPermissions() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            initializeWebRTC()
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
                initializeWebRTC()
            } else {
                showMessage("Разрешения необходимы для совершения звонка")
                finish()
            }
        }
    }

    private fun initializeWebRTC() {
        webRTCManager = WebRTCManager(this, this)
        webRTCManager.initialize()

        signalingClient = viewModel.getSignalingClient()

        if (isIncomingCall) {
            // Для входящего звонка ждем подтверждения
            binding.callStatus.text = "Входящий вызов..."
        } else {
            // Инициируем исходящий звонок
            startCall()
        }
    }

    private fun startCall() {
        webRTCManager.startCall(isVideoCall)
        signalingClient.sendCallRequest(contactName, isVideoCall)
        binding.callStatus.text = "Установка соединения..."
    }

    private fun acceptCall() {
        binding.incomingCallLayout.visibility = View.GONE
        binding.activeCallLayout.visibility = View.VISIBLE
        binding.callStatus.text = "Установка соединения..."

        webRTCManager.acceptCall(isVideoCall)
        signalingClient.sendCallAccept()
    }

    private fun rejectCall() {
        signalingClient.sendCallReject()
        finish()
    }

    private fun endCall() {
        signalingClient.sendEndCall()
        webRTCManager.endCall()
        finish()
    }

    private fun toggleAudioMute() {
        val isMuted = binding.muteAudioButton.isSelected
        binding.muteAudioButton.isSelected = !isMuted
        webRTCManager.toggleAudioMute(!isMuted)
    }

    private fun toggleVideoMute() {
        val isMuted = binding.muteVideoButton.isSelected
        binding.muteVideoButton.isSelected = !isMuted
        webRTCManager.toggleVideoMute(!isMuted)

        if (!isMuted) {
            binding.localVideoView.visibility = View.GONE
        } else {
            binding.localVideoView.visibility = View.VISIBLE
        }
    }

    private fun switchCamera() {
        webRTCManager.switchCamera()
    }

    // WebRTCManager.SignalingListener implementation
    override fun onLocalStreamAdded(stream: MediaStream) {
        runOnUiThread {
            // Отображаем локальное видео
            stream.videoTracks.firstOrNull()?.addSink(binding.localVideoView)
            binding.localVideoView.visibility = View.VISIBLE
        }
    }

    override fun onRemoteStreamAdded(stream: MediaStream) {
        runOnUiThread {
            // Отображаем удаленное видео
            stream.videoTracks.firstOrNull()?.addSink(binding.remoteVideoView)
            binding.remoteVideoView.visibility = View.VISIBLE
            binding.callStatus.text = "Соединение установлено"

            // Показываем управление звонком
            binding.callControls.visibility = View.VISIBLE
        }
    }

    override fun onRemoteStreamRemoved() {
        runOnUiThread {
            binding.remoteVideoView.visibility = View.GONE
            binding.callStatus.text = "Соединение прервано"
        }
    }

    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
        runOnUiThread {
            when (state) {
                PeerConnection.IceConnectionState.CONNECTED -> {
                    binding.callStatus.text = "Соединение установлено"
                }
                PeerConnection.IceConnectionState.DISCONNECTED -> {
                    binding.callStatus.text = "Соединение прервано"
                }
                PeerConnection.IceConnectionState.FAILED -> {
                    binding.callStatus.text = "Ошибка соединения"
                    endCall()
                }
                else -> {}
            }
        }
    }

    override fun onIceCandidate(candidate: IceCandidate) {
        signalingClient.sendIceCandidate(candidate)
    }

    override fun onOfferCreated(offer: SessionDescription) {
        signalingClient.sendOffer(offer)
    }

    override fun onAnswerCreated(answer: SessionDescription) {
        signalingClient.sendAnswer(answer)
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        // Запрещаем возврат во время звонка
        if (signalingClient.callState.value !is SignalingClient.CallState.Idle) {
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        webRTCManager.endCall()
    }
}