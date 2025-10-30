package com.example.aagnar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aagnar.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        setupUI()
    }

    private fun setupUI() {
        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val signInButton = findViewById<Button>(R.id.signInButton)
        val backButton = findViewById<Button>(R.id.backButton)

        // TODO: Загрузить список сохраненных аккаунтов

        signInButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (username.isEmpty()) {
                usernameInput.error = "Введите логин"
                return@setOnClickListener
            }

            // Временная реализация - всегда успешный вход
            CoroutineScope(Dispatchers.Main).launch {
                signInButton.isEnabled = false
                signInButton.text = "Вход..."

                // TODO: Реальная проверка пароля
                val success = true // временно всегда true

                if (success) {
                    saveSession(username)
                    Toast.makeText(this@SignInActivity, "✅ Успешный вход", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@SignInActivity, "❌ Неверный логин или пароль", Toast.LENGTH_SHORT).show()
                }

                signInButton.isEnabled = true
                signInButton.text = "Войти"
            }
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, AccountSelectionActivity::class.java))
            finish()
        }
    }

    private fun saveSession(accountId: String) {
        val sessionPrefs = getSharedPreferences("session", Context.MODE_PRIVATE)
        sessionPrefs.edit()
            .putString("current_account_id", accountId)
            .putBoolean("logged_in", true)
            .putLong("session_expires", System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000) // 30 дней
            .apply()
    }
}