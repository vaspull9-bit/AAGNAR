package com.example.aagnar.webrtc

import android.content.Context
import android.util.Log
import org.webrtc.*
import org.webrtc.PeerConnection.IceServer
import java.util.*

class WebRTCManager(
    private val context: Context,
    private val signalingListener: SignalingListener
) {

    companion object {
        private const val TAG = "WebRTCManager"

        // STUN/TURN серверы (можно использовать бесплатные)
        private val iceServers = listOf(
            IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
        )
    }

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localVideoSource: VideoSource? = null
    private var localAudioSource: AudioSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var localStream: MediaStream? = null
    private var remoteStream: MediaStream? = null

    private var videoCapturer: CameraVideoCapturer? = null
    private var eglBase: EglBase? = null

    private var isInitiator = false
    private var isVideoCall = true

    private val peerConnectionObserver = object : PeerConnection.Observer {
        override fun onSignalingChange(state: PeerConnection.SignalingState) {
            Log.d(TAG, "onSignalingChange: $state")
        }

        override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
            Log.d(TAG, "onIceConnectionChange: $state")
            signalingListener.onIceConnectionChange(state)
        }

        override fun onIceConnectionReceivingChange(receiving: Boolean) {
            Log.d(TAG, "onIceConnectionReceivingChange: $receiving")
        }

        override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {
            Log.d(TAG, "onIceGatheringChange: $state")
        }

        override fun onIceCandidate(candidate: IceCandidate) {
            Log.d(TAG, "onIceCandidate: $candidate")
            signalingListener.onIceCandidate(candidate)
        }

        override fun onIceCandidatesRemoved(candidates: Array<IceCandidate>) {
            Log.d(TAG, "onIceCandidatesRemoved")
        }

        override fun onAddStream(stream: MediaStream) {
            Log.d(TAG, "onAddStream: $stream")
            remoteStream = stream
            signalingListener.onRemoteStreamAdded(stream)
        }

        override fun onRemoveStream(stream: MediaStream) {
            Log.d(TAG, "onRemoveStream: $stream")
            signalingListener.onRemoteStreamRemoved()
        }

        override fun onDataChannel(dataChannel: DataChannel) {
            Log.d(TAG, "onDataChannel")
        }

        override fun onRenegotiationNeeded() {
            Log.d(TAG, "onRenegotiationNeeded")
        }

        override fun onAddTrack(receiver: RtpReceiver, streams: Array<MediaStream>) {
            Log.d(TAG, "onAddTrack")
        }
    }

    interface SignalingListener {
        fun onLocalStreamAdded(stream: MediaStream)
        fun onRemoteStreamAdded(stream: MediaStream)
        fun onRemoteStreamRemoved()
        fun onIceConnectionChange(state: PeerConnection.IceConnectionState)
        fun onIceCandidate(candidate: IceCandidate)
        fun onOfferCreated(offer: SessionDescription)
        fun onAnswerCreated(answer: SessionDescription)
    }

    fun initialize() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                .createInitializationOptions()
        )

        eglBase = EglBase.create()

        val options = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .createPeerConnectionFactory()

        Log.d(TAG, "WebRTC initialized")
    }

    fun createPeerConnection(): PeerConnection? {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            keyType = PeerConnection.KeyType.ECDSA
        }

        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, peerConnectionObserver)
        return peerConnection
    }

    fun startCall(isVideoCall: Boolean) {
        this.isVideoCall = isVideoCall
        this.isInitiator = true

        createPeerConnection()
        initializeMediaTracks()
        createOffer()
    }

    fun acceptCall(isVideoCall: Boolean) {
        this.isVideoCall = isVideoCall
        this.isInitiator = false

        createPeerConnection()
        initializeMediaTracks()
    }

    private fun initializeMediaTracks() {
        // Аудио
        val audioConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
        }

        localAudioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory?.createAudioTrack("audio_track", localAudioSource)

        // Видео (если видеозвонок)
        if (isVideoCall) {
            val videoSource = peerConnectionFactory?.createVideoSource(false)
            val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase?.eglBaseContext)

            videoCapturer = createCameraCapturer()
            videoCapturer?.initialize(surfaceTextureHelper, context, videoSource?.capturerObserver)
            videoCapturer?.startCapture(1280, 720, 30)

            localVideoSource = videoSource
            localVideoTrack = peerConnectionFactory?.createVideoTrack("video_track", localVideoSource)
        }

        // Создаем локальный поток
        localStream = peerConnectionFactory?.createLocalMediaStream("local_stream")
        localAudioTrack?.let { localStream?.addTrack(it) }
        localVideoTrack?.let { localStream?.addTrack(it) }

        // Добавляем поток в peer connection
        localStream?.let { stream ->
            peerConnection?.addStream(stream)
            signalingListener.onLocalStreamAdded(stream)
        }
    }

    private fun createCameraCapturer(): CameraVideoCapturer? {
        val cameraEnumerator = Camera2Enumerator(context)
        val deviceNames = cameraEnumerator.deviceNames

        for (deviceName in deviceNames) {
            if (cameraEnumerator.isFrontFacing(deviceName)) {
                return cameraEnumerator.createCapturer(deviceName, null)
            }
        }

        // Если фронтальная камера не найдена, используем любую доступную
        for (deviceName in deviceNames) {
            if (!cameraEnumerator.isBackFacing(deviceName)) {
                return cameraEnumerator.createCapturer(deviceName, null)
            }
        }

        return null
    }

    private fun createOffer() {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", if (isVideoCall) "true" else "false"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(offer: SessionDescription) {
                Log.d(TAG, "Offer created: ${offer.description}")
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {
                        Log.d(TAG, "Local description set")
                        signalingListener.onOfferCreated(offer)
                    }
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, offer)
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String) {
                Log.e(TAG, "Failed to create offer: $error")
            }
            override fun onSetFailure(error: String) {
                Log.e(TAG, "Failed to set local description: $error")
            }
        }, constraints)
    }

    fun createAnswer() {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", if (isVideoCall) "true" else "false"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(answer: SessionDescription) {
                Log.d(TAG, "Answer created: ${answer.description}")
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() {
                        Log.d(TAG, "Local description set")
                        signalingListener.onAnswerCreated(answer)
                    }
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, answer)
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String) {
                Log.e(TAG, "Failed to create answer: $error")
            }
            override fun onSetFailure(error: String) {
                Log.e(TAG, "Failed to set local description: $error")
            }
        }, constraints)
    }

    fun setRemoteDescription(description: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                Log.d(TAG, "Remote description set: ${description.type}")
                if (!isInitiator && description.type == SessionDescription.Type.OFFER) {
                    createAnswer()
                }
            }

            override fun onSetFailure(error: String) {
                Log.e(TAG, "Failed to set remote description: $error")
            }

            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, description)
    }

    fun addIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }

    fun switchCamera() {
        videoCapturer?.switchCamera(null)
    }

    fun toggleAudioMute(isMuted: Boolean) {
        localAudioTrack?.setEnabled(!isMuted)
    }

    fun toggleVideoMute(isMuted: Boolean) {
        localVideoTrack?.setEnabled(!isMuted)
        if (isMuted) {
            videoCapturer?.stopCapture()
        } else {
            videoCapturer?.startCapture(1280, 720, 30)
        }
    }

    fun endCall() {
        try {
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            localStream?.dispose()
            remoteStream?.dispose()
            peerConnection?.close()
            peerConnection?.dispose()
            peerConnectionFactory?.dispose()
            eglBase?.release()

            peerConnection = null
            localStream = null
            remoteStream = null
            videoCapturer = null

            Log.d(TAG, "Call ended and resources cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error ending call: ${e.message}")
        }
    }
}