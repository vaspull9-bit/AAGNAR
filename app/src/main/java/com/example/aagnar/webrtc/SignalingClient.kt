package com.example.aagnar.webrtc

import android.content.Context
import android.util.Log
import com.example.aagnar.domain.repository.WebSocketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.util.*

class SignalingClient(
    private val context: Context,
    private val webSocketRepository: WebSocketRepository
) {

    companion object {
        private const val TAG = "SignalingClient"
    }

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState

    private var currentCallId: String? = null
    private var currentContact: String? = null

    fun initialize(userId: String) {
        // TODO: Подключиться к WebRTC signaling server
        Log.d(TAG, "Signaling client initialized for user: $userId")
    }

    fun sendCallRequest(contact: String, isVideoCall: Boolean) {
        currentContact = contact
        currentCallId = "call_${System.currentTimeMillis()}_${UUID.randomUUID().toString().substring(0, 8)}"

        val callRequest = JSONObject().apply {
            put("type", "call-request")
            put("from", getCurrentUserId())
            put("to", contact)
            put("call_id", currentCallId)
            put("video_call", isVideoCall)
        }

        webSocketRepository.sendWebRTCMessage(callRequest.toString())
        _callState.value = CallState.Outgoing(contact, isVideoCall)
    }

    fun sendCallAccept() {
        currentCallId?.let { callId ->
            val callAccept = JSONObject().apply {
                put("type", "call-accept")
                put("from", getCurrentUserId())
                put("call_id", callId)
            }

            webSocketRepository.sendWebRTCMessage(callAccept.toString())
            _callState.value = CallState.Connecting(currentContact ?: "", true)
        }
    }

    fun sendCallReject(reason: String = "rejected") {
        currentCallId?.let { callId ->
            val callReject = JSONObject().apply {
                put("type", "call-reject")
                put("from", getCurrentUserId())
                put("call_id", callId)
                put("reason", reason)
            }

            webSocketRepository.sendWebRTCMessage(callReject.toString())
            resetCallState()
        }
    }

    fun sendOffer(offer: SessionDescription) {
        currentCallId?.let { callId ->
            currentContact?.let { contact ->
                val offerMsg = JSONObject().apply {
                    put("type", "offer")
                    put("from", getCurrentUserId())
                    put("to", contact)
                    put("call_id", callId)
                    put("offer", JSONObject().apply {
                        put("type", offer.type.canonicalForm())
                        put("sdp", offer.description)
                    })
                }

                webSocketRepository.sendWebRTCMessage(offerMsg.toString())
            }
        }
    }

    fun sendAnswer(answer: SessionDescription) {
        currentCallId?.let { callId ->
            currentContact?.let { contact ->
                val answerMsg = JSONObject().apply {
                    put("type", "answer")
                    put("from", getCurrentUserId())
                    put("to", contact)
                    put("call_id", callId)
                    put("answer", JSONObject().apply {
                        put("type", answer.type.canonicalForm())
                        put("sdp", answer.description)
                    })
                }

                webSocketRepository.sendWebRTCMessage(answerMsg.toString())
            }
        }
    }

    fun sendIceCandidate(candidate: IceCandidate) {
        currentCallId?.let { callId ->
            currentContact?.let { contact ->
                val candidateMsg = JSONObject().apply {
                    put("type", "ice-candidate")
                    put("from", getCurrentUserId())
                    put("to", contact)
                    put("call_id", callId)
                    put("candidate", JSONObject().apply {
                        put("sdpMid", candidate.sdpMid)
                        put("sdpMLineIndex", candidate.sdpMLineIndex)
                        put("sdp", candidate.sdp)
                    })
                }

                webSocketRepository.sendWebRTCMessage(candidateMsg.toString())
            }
        }
    }

    fun sendEndCall() {
        currentCallId?.let { callId ->
            val endCall = JSONObject().apply {
                put("type", "end-call")
                put("from", getCurrentUserId())
                put("call_id", callId)
            }

            webSocketRepository.sendWebRTCMessage(endCall.toString())
            resetCallState()
        }
    }

    fun handleIncomingCall(from: String, callId: String, isVideoCall: Boolean) {
        currentCallId = callId
        currentContact = from
        _callState.value = CallState.Incoming(from, isVideoCall)
    }

    fun handleCallAccepted() {
        _callState.value = CallState.Connecting(currentContact ?: "", true)
    }

    fun handleCallRejected(reason: String) {
        _callState.value = CallState.Ended(reason)
        resetCallState()
    }

    fun handleCallEnded() {
        _callState.value = CallState.Ended("Call ended")
        resetCallState()
    }

    private fun getCurrentUserId(): String {
        val prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        return prefs.getString("username", "") ?: ""
    }

    private fun resetCallState() {
        currentCallId = null
        currentContact = null
        _callState.value = CallState.Idle
    }

    sealed class CallState {
        object Idle : CallState()
        data class Outgoing(val contact: String, val isVideo: Boolean) : CallState()
        data class Incoming(val contact: String, val isVideo: Boolean) : CallState()
        data class Connecting(val contact: String, val isVideo: Boolean) : CallState()
        data class Connected(val contact: String, val isVideo: Boolean) : CallState()
        data class Ended(val reason: String) : CallState()
    }
}