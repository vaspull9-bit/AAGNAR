// app/src/main/java/com/example/aagnar/MainActivity.kt
// DeeR_Tuund(C) 2025. AAGNAR v3.3.1
package com.example.aagnar

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aagnar.presentation.ui.call.CallFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.app.AlertDialog

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var matrixService: com.example.aagnar.domain.service.MatrixService

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var menuButton: ImageButton
    private lateinit var actionAbout: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Находим элементы
        toolbar = findViewById(R.id.toolbar)
        toolbarTitle = findViewById(R.id.toolbar_title)
        menuButton = findViewById(R.id.menu_button)
        actionAbout = findViewById(R.id.action_about)

        setupToolbar()
        showMatrixFragment()

        // Тест логина
        lifecycleScope.launch {
            delay(3000)
            val success = matrixService.login(
                "@aagnar_test_789:matrix.org",
                "Test_password123"
            )
            println("LOGIN RESULT: $success")
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)

        menuButton.setOnClickListener {
            showPopupMenu(it)
        }

        actionAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showPopupMenu(anchorView: android.view.View) {
        val popup = PopupMenu(this, anchorView)
        popup.menuInflater.inflate(R.menu.main_popup_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_matrix -> showMatrixFragment()
                R.id.menu_calls -> showCallFragment()
                R.id.menu_settings -> showSettingsFragment()
            }
            true
        }

        popup.show()
    }

    private fun showMatrixFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, com.example.aagnar.presentation.ui.matrix.MatrixFragment())
            .commit()
        toolbarTitle.text = "Matrix чат"
    }

    private fun showCallFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CallFragment())
            .commit()
        toolbarTitle.text = "Звонки"
    }

    private fun showSettingsFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, com.example.aagnar.presentation.ui.settings.SettingsFragment())
            .addToBackStack("settings")
            .commit()
        toolbarTitle.text = "Настройки сервера"
    }

    private fun showAboutDialog() {
        val aboutText = """
            DeeR_Tuund(C) 2025. AAGNAR v3.3.1
            
            Matrix клиент с поддержкой:
            - 💬 Чат и сообщения
            - 📞 VoIP звонки  
            - 🔧 Кастомные серверы
            
            This program is free software: you can redistribute it and/or modify
            it under the terms of the GNU General Public License as published by
            the Free Software Foundation, either version 3 of the License, or
            (at your option) any later version.
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("О программе")
            .setMessage(aboutText)
            .setPositiveButton("OK", null)
            .show()
    }
}