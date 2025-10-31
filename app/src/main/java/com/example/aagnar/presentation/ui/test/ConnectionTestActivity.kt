// presentation/ui/test/ConnectionTestActivity.kt
package com.example.aagnar.presentation.ui.test

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aagnar.R
import com.example.aagnar.domain.repository.WebSocketRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject



@AndroidEntryPoint
class ConnectionTestActivity : AppCompatActivity() {

    @Inject
    lateinit var webSocketRepository: WebSocketRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection_test)

        // Тест подключения
        findViewById<Button>(R.id.btn_connect).setOnClickListener {
            connectToServer()
        }

        // Тест отправки сообщения
        findViewById<Button>(R.id.btn_send_test).setOnClickListener {
            sendTestMessage()
        }

        // Тест приглашения
        findViewById<Button>(R.id.btn_invite).setOnClickListener {
            sendInvite()
        }
    }

    private fun connectToServer() {
        lifecycleScope.launch {
            val username = "test_user_${Random().nextInt(1000)}"
            (webSocketRepository as? com.example.aagnar.data.repository.WebSocketRepository)?.connect(username)
            Toast.makeText(this@ConnectionTestActivity, "Connected as $username", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendTestMessage() {
        lifecycleScope.launch {
            webSocketRepository.sendMessage("test_recipient", "Тестовое сообщение", "test_${System.currentTimeMillis()}")
            Toast.makeText(this@ConnectionTestActivity, "Сообщение отправлено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendInvite() {
        lifecycleScope.launch {
            val targetUser = findViewById<EditText>(R.id.et_target_user).text.toString()
            if (targetUser.isNotEmpty()) {
                (webSocketRepository as? com.example.aagnar.data.repository.WebSocketRepository)?.sendContactInvite(targetUser)
                Toast.makeText(this@ConnectionTestActivity, "Приглашение отправлено $targetUser", Toast.LENGTH_SHORT).show()
            }
        }
    }
}