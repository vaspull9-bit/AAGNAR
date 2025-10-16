// app/src/main/java/com/example/aagnar/presentation/ui/settings/SettingsFragment.kt
package com.example.aagnar.presentation.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.aagnar.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject lateinit var matrixService: com.example.aagnar.domain.service.MatrixService
    @Inject lateinit var settingsRepository: com.example.aagnar.data.repository.SettingsRepository

    private lateinit var currentServerText: TextView
    private lateinit var serverInput: EditText
    private lateinit var changeServerButton: Button
    private lateinit var resetButton: Button
    private lateinit var statusText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Находим View элементы
        currentServerText = view.findViewById(R.id.currentServerText)
        serverInput = view.findViewById(R.id.serverInput)
        changeServerButton = view.findViewById(R.id.changeServerButton)
        resetButton = view.findViewById(R.id.resetButton)
        statusText = view.findViewById(R.id.statusText)

        setupUI()
    }

    private fun setupUI() {
        // Показываем текущий сервер
        currentServerText.text = "Текущий сервер: ${settingsRepository.getHomeServer()}"

        // Обработчик кнопки смены сервера
        changeServerButton.setOnClickListener {
            val newServer = serverInput.text.toString().trim()
            if (newServer.isNotEmpty()) {
                lifecycleScope.launch {
                    val success = matrixService.updateHomeServer(newServer)
                    if (success) {
                        currentServerText.text = "Текущий сервер: $newServer"
                        serverInput.text.clear()
                        statusText.text = "✅ Сервер успешно изменен!"
                    } else {
                        statusText.text = "❌ Ошибка смены сервера"
                    }
                }
            }
        }

        // Кнопка сброса на matrix.org
        resetButton.setOnClickListener {
            lifecycleScope.launch {
                val success = matrixService.updateHomeServer("https://matrix.org")
                if (success) {
                    currentServerText.text = "Текущий сервер: https://matrix.org"
                    statusText.text = "✅ Сервер сброшен на matrix.org"
                }
            }
        }
    }
}