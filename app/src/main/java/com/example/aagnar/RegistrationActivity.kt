package com.example.aagnar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.aagnar.data.remote.RegistrationClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.example.aagnar.R

class RegistrationActivity : AppCompatActivity() {

    private val registrationClient = RegistrationClient()
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val nicknameInput = findViewById<EditText>(R.id.nicknameInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val nickname = nicknameInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (username.isEmpty()) {
                usernameInput.error = "Введите логин"
                return@setOnClickListener
            }

            scope.launch {
                registerButton.isEnabled = false
                registerButton.text = "Регистрация..."

                val result = registrationClient.register(username, password, nickname)
                handleRegistrationResult(result, username, nickname, password) // ← ДОБАВИЛ password

                registerButton.isEnabled = true
                registerButton.text = "Зарегистрироваться"
            }
        }
    }

    private fun handleRegistrationResult(result: String, username: String, nickname: String, password: String) {
        println("🟢 [REG ACTIVITY] Received result: $result")

        try {
            val json = JSONObject(result)
            val type = json.getString("type")
            println("🟢 [REG ACTIVITY] Result type: $type")

            when (type) {
                "success" -> {
                    println("🟢 [REG ACTIVITY] Registration successful! Starting MainActivity...")

                    // Сохраняем данные
                    val prefs = getSharedPreferences("user", MODE_PRIVATE)
                    prefs.edit()
                        .putString("username", username)
                        .putString("nickname", if (nickname.isNotEmpty()) nickname else username)
                        .putString("password", password)
                        .putBoolean("registered", true)
                        .apply()

                    println("🟢 [REG ACTIVITY] User data saved, starting MainActivity...")

                    // Проверяем что MainActivity существует
                    try {
                        val intent = Intent(this, MainActivity::class.java)
                        println("🟢 [REG ACTIVITY] Intent created: $intent")
                        startActivity(intent)
                        println("🟢 [REG ACTIVITY] startActivity called")
                        finish()
                        println("🟢 [REG ACTIVITY] finish called")
                    } catch (e: Exception) {
                        println("🔴 [REG ACTIVITY] Error starting MainActivity: ${e.message}")
                        e.printStackTrace()

                        // Показываем ошибку пользователю
                        runOnUiThread {
                            android.app.AlertDialog.Builder(this)
                                .setTitle("Ошибка перехода")
                                .setMessage("Не удалось открыть главное приложение: ${e.message}")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }
                }
                "error" -> {
                    val errorMessage = json.getString("message")
                    println("🔴 [REG ACTIVITY] Registration error: $errorMessage")

                    runOnUiThread {
                        android.app.AlertDialog.Builder(this)
                            .setTitle("Ошибка регистрации")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
                else -> {
                    println("🔴 [REG ACTIVITY] Unknown response type: $type")

                    runOnUiThread {
                        android.app.AlertDialog.Builder(this)
                            .setTitle("Ошибка")
                            .setMessage("Неизвестный ответ от сервера: $type")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
        } catch (e: Exception) {
            println("🔴 [REG ACTIVITY] JSON parsing error: ${e.message}")

            runOnUiThread {
                android.app.AlertDialog.Builder(this)
                    .setTitle("Ошибка")
                    .setMessage("Неверный ответ от сервера: $result")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }
}