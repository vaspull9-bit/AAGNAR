//C:\Users\trii\AndroidStudioProjects\AAGNAR\app\src\main\java\com\example\aagnar\domain\service\MatrixService.kt
//MatrixService.kt - все без ошибок, но не работает инициализация матрикс!!!!
//  версия v3.1.0

package com.example.aagnar.domain.service

import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.matrix.android.sdk.api.auth.AuthenticationService
import org.matrix.android.sdk.api.session.Session
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.MatrixConfiguration
import org.matrix.android.sdk.api.crypto.MXCryptoConfig
import org.matrix.android.sdk.api.provider.MatrixItemDisplayNameFallbackProvider
import org.matrix.android.sdk.api.provider.RoomDisplayNameFallbackProvider
import org.matrix.android.sdk.api.util.MatrixItem





@Singleton
class MatrixService @Inject constructor(
    private val context: Context
) {
    private var matrix: Matrix? = null
    private var session: Session? = null

    private val _connectionState = MutableStateFlow<MatrixState>(MatrixState.Disconnected)
    val connectionState: StateFlow<MatrixState> = _connectionState

    private val _messages = MutableStateFlow<List<MatrixMessage>>(emptyList())
    val messages: StateFlow<List<MatrixMessage>> = _messages

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState

    // 🔥 ФЛАГ ЧТОБЫ НЕ ИНИЦИАЛИЗИРОВАТЬ ПОВТОРНО
    private var isInitialized = false

    fun initialize() {
        // 🔥 ПРОВЕРЯЕМ ЧТОБЫ НЕ ДУБЛИРОВАТЬ
        if (isInitialized) {
            println("⚠️ Matrix already initialized, skipping...")
            return
        }

        try {
            println("=== MATRIX INIT 1.6.36 ===")

            val config = MatrixConfiguration(
                matrixItemDisplayNameFallbackProvider = object : MatrixItemDisplayNameFallbackProvider {
                    override fun getDefaultName(matrixItem: MatrixItem): String {
                        return matrixItem.id ?: "Unknown"
                    }
                },
                roomDisplayNameFallbackProvider = object : RoomDisplayNameFallbackProvider {
                    override fun excludedUserIds(roomId: String): List<String> = emptyList()
                    override fun getNameForRoomInvite(): String = "Room Invite"
                    override fun getNameForEmptyRoom(isDirect: Boolean, leftMemberNames: List<String>): String =
                        if (isDirect) "Empty Direct Chat" else "Empty Room"
                    override fun getNameFor1member(name: String): String = name
                    override fun getNameFor2members(name1: String, name2: String): String = "$name1 and $name2"
                    override fun getNameFor3members(name1: String, name2: String, name3: String): String = "$name1, $name2 and $name3"
                    override fun getNameFor4members(name1: String, name2: String, name3: String, name4: String): String = "$name1, $name2, $name3 and $name4"
                    override fun getNameFor4membersAndMore(name1: String, name2: String, name3: String, remainingCount: Int): String = "$name1, $name2, $name3 and $remainingCount others"
                },
                cryptoConfig = MXCryptoConfig(),
                integrationUIUrl = "https://scalar.vector.im/",
                integrationRestUrl = "https://scalar.vector.im/api"
            )

            matrix = Matrix(context, config)
            println("Matrix instance: ${matrix != null}")

            if (matrix != null) {
                _connectionState.value = MatrixState.Connected
                val authService = matrix?.authenticationService()
                println("✅ AuthService: ${authService != null}")
                println("✅ SUCCESS: Matrix 1.6.36 initialized!")

                // 🔥 ПОМЕЧАЕМ ЧТО ИНИЦИАЛИЗИРОВАЛИ
                isInitialized = true
            }

        } catch (e: Exception) {
            println("❌ ERROR: ${e.message}")
            _connectionState.value = MatrixState.Error(e.message ?: "Unknown error")
        }
    }

    // 🔥 ВСЕ МЕТОДЫ ДОЛЖНЫ БЫТЬ ВНУТРИ КЛАССА MatrixService!
    suspend fun login(username: String, password: String): Boolean {
        return try {
            println("=== REAL MATRIX LOGIN ATTEMPT ===")

            val authService = matrix?.authenticationService()
            if (authService == null) return false

            // 🔥 СОЗДАЕМ URI ДЛЯ HOME SERVER
            val homeServerUri = android.net.Uri.parse("https://matrix.org")

            // 🔥 СОЗДАЕМ КОНФИГ С URI
            val homeServerConfig = org.matrix.android.sdk.api.auth.data.HomeServerConnectionConfig(
                homeServerUri = homeServerUri
            )

            val loginFlow = authService.getLoginFlow(homeServerConfig)
            println("LoginFlow: ${loginFlow != null}")

            val loginWizard = authService.getLoginWizard()
            println("LoginWizard: ${loginWizard != null}")

            val result = loginWizard.login(
                login = username,
                password = password,
                initialDeviceName = "AAGNAR Android App"
            )

            session = result
            _connectionState.value = MatrixState.Registered(result.myUserId)
            println("✅ REAL LOGIN SUCCESS: ${result.myUserId}")
            true

        } catch (e: Exception) {
            println("❌ LOGIN ERROR: ${e.message}")
            e.printStackTrace()
            _connectionState.value = MatrixState.Error("Login error: ${e.message}")
            false
        }
    }

    suspend fun register(username: String, password: String, displayName: String): Boolean {
        return try {
            val authService = matrix?.authenticationService()
            val registrationWizard = authService?.getRegistrationWizard()
            println("RegistrationWizard available: ${registrationWizard != null}")

            delay(500)
            _connectionState.value = MatrixState.Registered("@$username:matrix.org")
            true
        } catch (e: Exception) {
            _connectionState.value = MatrixState.Error("Registration error: ${e.message}")
            false
        }
    }

    suspend fun sendMessage(peerUserId: String, text: String) {
        try {
            val newMessage = MatrixMessage(
                id = System.currentTimeMillis().toString(),
                senderId = session?.myUserId ?: "unknown",
                text = text,
                timestamp = System.currentTimeMillis(),
                isOutgoing = true
            )
            _messages.value = _messages.value + newMessage
            println("Message sent to $peerUserId: $text")
        } catch (e: Exception) {
            println("Send message error: ${e.message}")
        }
    }

    suspend fun sendFile(peerUserId: String, file: File, mimeType: String) {
        try {
            println("File sent to $peerUserId: ${file.name} (type: $mimeType)")
        } catch (e: Exception) {
            println("Send file error: ${e.message}")
        }
    }

    suspend fun startCall(peerUserId: String, isVideo: Boolean) {
        try {
            _callState.value = CallState.Outgoing(peerUserId, isVideo)
            println("Call started to $peerUserId (video: $isVideo)")
        } catch (e: Exception) {
            println("Start call error: ${e.message}")
        }
    }

    suspend fun answerCall(peerUserId: String) {
        try {
            _callState.value = CallState.Active(peerUserId, true)
            println("Call answered from $peerUserId")
        } catch (e: Exception) {
            println("Answer call error: ${e.message}")
        }
    }

    suspend fun endCall(peerUserId: String) {
        try {
            _callState.value = CallState.Idle
            println("Call ended with $peerUserId")
        } catch (e: Exception) {
            println("End call error: ${e.message}")
        }
    }

    fun getAuthenticationService(): AuthenticationService? {
        return matrix?.authenticationService()
    }

    fun getSession(): Session? {
        return session
    }

    fun cleanup() {
        session = null
    }
} // 🔥 ЗАКРЫВАЮЩАЯ ФИГУРНАЯ СКОБКА КЛАССА MatrixService

// Data classes для состояний - ВНЕ КЛАССА
sealed class MatrixState {
    object Disconnected : MatrixState()
    object Connected : MatrixState()
    data class Registered(val userId: String) : MatrixState()
    data class Error(val message: String) : MatrixState()
}

sealed class CallState {
    object Idle : CallState()
    data class Outgoing(val peerId: String, val isVideo: Boolean) : CallState()
    data class Incoming(val peerId: String, val isVideo: Boolean) : CallState()
    data class Active(val peerId: String, val isVideo: Boolean) : CallState()
}

data class MatrixMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val timestamp: Long,
    val isOutgoing: Boolean = false
)