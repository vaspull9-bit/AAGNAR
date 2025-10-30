package com.example.aagnar.presentation.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.aagnar.R
import com.example.aagnar.util.PerformanceMonitor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.net.Socket
import java.net.InetSocketAddress
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var performanceMonitor: PerformanceMonitor
    private lateinit var settingsRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return performanceMonitor.measure("SettingsFragment.onCreateView") {
            inflater.inflate(R.layout.fragment_settings, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        performanceMonitor.measure("SettingsFragment.onViewCreated") {
            initViews(view)
            setupServerRegistration()
            loadSavedUserData() // ← ЗАГРУЖАЕМ СОХРАНЕННЫЕ ДАННЫЕ
        }
    }

    private fun initViews(view: View) {
        settingsRecyclerView = view.findViewById(R.id.settingsRecyclerView)
    }

    private fun loadSavedUserData() {
        val prefs = requireContext().getSharedPreferences("user", Context.MODE_PRIVATE)

        val usernameInput = view?.findViewById<EditText>(R.id.usernameInput)
        val nicknameInput = view?.findViewById<EditText>(R.id.nicknameInput)

        // Загружаем сохраненные данные (кроме пароля)
        usernameInput?.setText(prefs.getString("username", ""))
        nicknameInput?.setText(prefs.getString("nickname", ""))
    }

    private fun setupServerRegistration() {
        val prefs = requireContext().getSharedPreferences("user", Context.MODE_PRIVATE)
        val registrationClient = com.example.aagnar.data.remote.RegistrationClient()

        // Кнопка проверки серверов
        val checkServersButton = view?.findViewById<Button>(R.id.checkServersButton)
        checkServersButton?.setOnClickListener {
            checkServerStatus()
        }

        // Кнопка регистрации
        val registerButton = view?.findViewById<Button>(R.id.registerButton)
        registerButton?.setOnClickListener {
            val usernameInput = view?.findViewById<EditText>(R.id.usernameInput)
            val nicknameInput = view?.findViewById<EditText>(R.id.nicknameInput)
            val passwordInput = view?.findViewById<EditText>(R.id.passwordInput)

            val username = usernameInput?.text?.toString()?.trim() ?: ""
            val nickname = nicknameInput?.text?.toString()?.trim() ?: ""
            val password = passwordInput?.text?.toString() ?: ""

            if (username.isEmpty()) {
                usernameInput?.error = "Введите логин"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput?.error = "Введите пароль"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                registerButton.isEnabled = false
                registerButton.text = "Регистрация..."

                try {
                    val result = registrationClient.register(username, password, nickname)

                    if (result.contains("success")) {
                        // СОХРАНЯЕМ ДАННЫЕ ПОЛЬЗОВАТЕЛЯ
                        prefs.edit()
                            .putString("username", username)
                            .putString("nickname", if (nickname.isNotEmpty()) nickname else username)
                            .putString("password", password) // TODO: Захешировать позже
                            .putBoolean("registered", true)
                            .apply()

                        showMessage("✅ Успешная регистрация на сервере")

                        // Обновляем статус
                        val statusText = view?.findViewById<TextView>(R.id.registrationStatus)
                        statusText?.text = "🟢 Зарегистрирован как: $username"
                    } else {
                        showMessage("❌ Ошибка регистрации: $result")
                    }
                } catch (e: Exception) {
                    showMessage("❌ Сервер недоступен: ${e.message}")
                } finally {
                    registerButton.isEnabled = true
                    registerButton.text = "Зарегистрироваться на сервере"
                }
            }
        }

        // Статус регистрации
        updateRegistrationStatus()
    }

    private fun updateRegistrationStatus() {
        val prefs = requireContext().getSharedPreferences("user", Context.MODE_PRIVATE)
        val statusText = view?.findViewById<TextView>(R.id.registrationStatus)

        if (prefs.getBoolean("registered", false)) {
            val username = prefs.getString("username", "")
            val nickname = prefs.getString("nickname", "")
            val displayName = if (!nickname.isNullOrEmpty()) "$username ($nickname)" else username
            statusText?.text = "🟢 Зарегистрирован как: $displayName"
        } else {
            statusText?.text = "🔴 Не зарегистрирован"
        }
    }

    private fun checkServerStatus() {
        lifecycleScope.launch {
            val checkServersButton = view?.findViewById<Button>(R.id.checkServersButton)
            checkServersButton?.isEnabled = false
            checkServersButton?.text = "Проверка..."

            try {
                val webSocketOk = checkWebSocketServer()
                val tcpOk = checkTcpServer()

                updateServerStatusUI(webSocketOk, tcpOk)
            } catch (e: Exception) {
                updateServerStatusUI(false, false)
            } finally {
                checkServersButton?.isEnabled = true
                checkServersButton?.text = "Проверить серверы"
            }
        }
    }

    private suspend fun checkWebSocketServer(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder()
                    .url("ws://192.168.88.240:8889")
                    .build()
                val webSocket = client.newWebSocket(request, object : okhttp3.WebSocketListener() {})
                delay(1000)
                webSocket.close(1000, null)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private suspend fun checkTcpServer(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress("192.168.88.240", 8887), 3000)
                socket.close()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun updateServerStatusUI(webSocketOk: Boolean, tcpOk: Boolean) {
        val webSocketStatus = view?.findViewById<TextView>(R.id.webSocketStatus)
        val tcpStatus = view?.findViewById<TextView>(R.id.tcpStatus)

        webSocketStatus?.text = "WebSocket: ${if (webSocketOk) "🟢" else "🔴"}"
        tcpStatus?.text = "TCP: ${if (tcpOk) "🟢" else "🔴"}"
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        performanceMonitor.logPerformanceEvent("SettingsFragment resumed")
        // При каждом открытии настроек обновляем статус
        updateRegistrationStatus()
    }
}