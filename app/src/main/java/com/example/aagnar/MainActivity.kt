// MainActivity.kt AAGNAR v1.0.2
// 2:06 28.09.2025 - старт
package com.example.aagnar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.aagnar.databinding.ActivityMainBinding
import com.example.aagnar.presentation.ui.call.CallFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        showCallFragment()  // ДОБАВИТЬ ЭТУ СТРОКУ
    }

    // ДОБАВИТЬ ЭТОТ МЕТОД
    private fun showCallFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CallFragment())  // ← ИСПОЛЬЗОВАТЬ R.id.container
            .commit()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "AAGNAR"

        // Обработчик для ImageButton в layout (правая кнопка "i")
        binding.toolbar.findViewById<android.widget.ImageButton>(R.id.action_about).setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showAboutDialog() {
        val aboutText = """
            DeeR_Tuund(C) 2025. AAGNAR v1.0.2
            
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