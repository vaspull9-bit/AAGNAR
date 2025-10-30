package com.example.aagnar


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aagnar.R

class AccountSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_selection)

        // Проверяем авто-вход
        if (shouldAutoLogin()) {
            autoLogin()
            return
        }

        setupUI()
    }

    private fun shouldAutoLogin(): Boolean {
        val sessionPrefs = getSharedPreferences("session", Context.MODE_PRIVATE)
        val autoLogin = sessionPrefs.getBoolean("auto_login_enabled", true) // по умолчанию ВКЛ
        val loggedIn = sessionPrefs.getBoolean("logged_in", false)
        val accountId = sessionPrefs.getString("current_account_id", null)

        return autoLogin && loggedIn && accountId != null
    }

    private fun autoLogin() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setupUI() {
        // Кнопка "Войти"
        findViewById<Button>(R.id.signInButton).setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        // Кнопка "Создать аккаунт"
        findViewById<Button>(R.id.createAccountButton).setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        // Переключатель авто-входа
        val autoLoginSwitch = findViewById<Switch>(R.id.autoLoginSwitch)
        val sessionPrefs = getSharedPreferences("session", Context.MODE_PRIVATE)
        autoLoginSwitch.isChecked = sessionPrefs.getBoolean("auto_login_enabled", true)

        autoLoginSwitch.setOnCheckedChangeListener { _, isChecked ->
            sessionPrefs.edit().putBoolean("auto_login_enabled", isChecked).apply()
            Toast.makeText(this, if (isChecked) "Автовход включен" else "Автовход выключен", Toast.LENGTH_SHORT).show()
        }
    }
}