package com.example.aagnar.domain.service

import android.content.Context
import org.linphone.core.*

class LinphoneService(private val context: Context) {
    private var core: Core? = null
    private var currentCall: Call? = null

    fun initialize() {
        try {
            val factory = Factory.instance()

            // Упрощенная инициализация без конфига
            core = factory.createCore(null, null, context)

            core?.addListener(object : CoreListenerStub() {
                override fun onCallStateChanged(core: Core, call: Call, state: Call.State, message: String) {
                    handleCallState(call, state)
                }
            })

            core?.start()
            println("Linphone initialized successfully")
        } catch (e: Exception) {
            println("Linphone initialization failed: ${e.message}")
        }
    }

    private fun handleCallState(call: Call, state: Call.State) {
        currentCall = call
        when (state) {
            Call.State.OutgoingInit -> println("Linphone: Outgoing call initiated")
            Call.State.OutgoingProgress -> println("Linphone: Outgoing call progressing")
            Call.State.OutgoingRinging -> println("Linphone: Outgoing call ringing")
            Call.State.Connected -> println("Linphone: Call connected")
            Call.State.End -> println("Linphone: Call ended")
            Call.State.Error -> println("Linphone: Call error")
            else -> println("Linphone: Call state: $state")
        }
    }
    fun createAccount(username: String, password: String, domain: String) {
        try {
            // Упрощенная версия - просто логируем
            println("Account creation requested: $username@$domain")
            println("Note: Real SIP account creation will be implemented when needed")

            // Временная заглушка - реальную логику добавим позже
            // когда разберемся с правильным API Linphone
        } catch (e: Exception) {
            println("Account creation failed: ${e.message}")
        }
    }

    fun makeCall(uri: String) {
        try {
            // РЕАЛЬНЫЙ ВЫЗОВ
            val address = core?.createAddress(uri)
            currentCall = address?.let { core?.inviteAddress(it) }
            println("Making REAL call to: $uri")
        } catch (e: Exception) {
            println("Make call failed: ${e.message}")
        }
    }

    fun answerCall() {
        try {
            currentCall?.accept()
            println("Call answered")
        } catch (e: Exception) {
            println("Answer call failed: ${e.message}")
        }
    }

    fun endCall() {
        try {
            currentCall?.terminate()
            currentCall = null
            println("Call ended")
        } catch (e: Exception) {
            println("End call failed: ${e.message}")
        }
    }

    fun toggleMute(muted: Boolean) {
        try {
            core?.isMicEnabled = !muted
            println("Microphone ${if (muted) "muted" else "unmuted"}")
        } catch (e: Exception) {
            println("Toggle mute failed: ${e.message}")
        }
    }

    fun toggleSpeaker(enabled: Boolean) {
        try {
            core?.audioDevices?.find { it.type == AudioDevice.Type.Speaker }?.let { speaker ->
                if (enabled) {
                    // Включаем динамик
                    core?.outputAudioDevice = speaker
                } else {
                    // Выключаем динамик - используем первый доступный не-динамик
                    core?.audioDevices?.find { it.type != AudioDevice.Type.Speaker }?.let { otherDevice ->
                        core?.outputAudioDevice = otherDevice
                    }
                }
            }
            println("Speaker ${if (enabled) "enabled" else "disabled"}")
        } catch (e: Exception) {
            println("Toggle speaker failed: ${e.message}")
        }
    }

    fun toggleVideo(enabled: Boolean) {
        try {
            currentCall?.let { call ->
                val params = call.params ?: core?.createCallParams(call)
                params?.isVideoEnabled = enabled
                params?.let { call.params = it }
            }
            println("Video ${if (enabled) "enabled" else "disabled"}")
        } catch (e: Exception) {
            println("Toggle video failed: ${e.message}")
        }
    }

    fun holdCall() {
        try {
            currentCall?.pause()
            println("Call held")
        } catch (e: Exception) {
            println("Hold call failed: ${e.message}")
        }
    }

    fun unholdCall() {
        try {
            currentCall?.resume()
            println("Call unheld")
        } catch (e: Exception) {
            println("Unhold call failed: ${e.message}")
        }
    }
    fun getRegistrationStatus(): String {
        return core?.defaultAccount?.let { account ->
            when (account.state) {
                RegistrationState.Ok -> "✅ REGISTERED"
                RegistrationState.Progress -> "🔄 REGISTERING"
                RegistrationState.Failed -> "❌ REGISTRATION FAILED"
                else -> "❓ NOT REGISTERED"
            }
        } ?: "❓ NO ACCOUNT"
    }



}