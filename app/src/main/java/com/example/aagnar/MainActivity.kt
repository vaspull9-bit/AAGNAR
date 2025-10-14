// MainActivity.kt A AGNAR v3.1.0 с MATRIX - БЕЗ BINDING
package com.example.aagnar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aagnar.presentation.ui.call.CallFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var matrixService: com.example.aagnar.domain.service.MatrixService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbar()
        showMatrixFragment()  // 🔥 ЗАМЕНИТЬ НА MATRIX

        // В MainActivity.kt в onCreate

        lifecycleScope.launch {  // В корутине ✅
            delay(3000)

            try {
                val success = matrixService.login("aagnar_test_789", "Test_password123")  // suspend метод ✅
                println("LOGIN RESULT: $success")
            } catch (e: Exception) {
                println("❌ LOGIN CRASH: ${e.message}")
                e.printStackTrace()
            }
        }

    }

    // ДОБАВЬ ЭТОТ МЕТОД:
    private fun showMatrixFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, com.example.aagnar.presentation.ui.matrix.MatrixFragment())
            .commit()
    }

    private fun showCallFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CallFragment())
            .commit()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)  // 🔥 findViewById
        setSupportActionBar(toolbar)
        supportActionBar?.title = "AAGNAR"

        // Обработчик для ImageButton в layout (правая кнопка "i")
        toolbar.findViewById<android.widget.ImageButton>(R.id.action_about).setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showAboutDialog() {
        val aboutText = """
            DeeR_Tuund(C) 2025. AAGNAR v3.1.0
            
            Браузер для интернета
            
            This program is free software: you can redistribute it and/or modify
            it under the terms of the GNU General Public License as published by
            the Free Software Foundation, either version 3 of the License, or
            (at your option) any later version.
        """.trimIndent()

        android.app.AlertDialog.Builder(this)
            .setTitle("О программе")
            .setMessage(aboutText)
            .setPositiveButton("OK", null)
            .show()
    }
}