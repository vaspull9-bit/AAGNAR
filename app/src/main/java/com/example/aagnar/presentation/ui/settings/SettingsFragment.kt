package com.example.aagnar.presentation.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.aagnar.R
import com.example.aagnar.util.PerformanceMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var performanceMonitor: PerformanceMonitor
    private lateinit var settingsRecyclerView: RecyclerView  // ← Используем существующий элемент

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
            setupSettings()
        }
    }

    private fun initViews(view: View) {
        performanceMonitor.measure("SettingsFragment.initViews") {
            settingsRecyclerView = view.findViewById(R.id.settingsRecyclerView)  // ← Правильный ID
            // Инициализация других вьюх если нужно
        }
    }

    private fun setupSettings() {
        performanceMonitor.measure("SettingsFragment.setupSettings") {
            // TODO: Настроить RecyclerView адаптер для настроек
            // settingsRecyclerView.adapter = SettingsAdapter()
            // settingsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        performanceMonitor.logPerformanceEvent("SettingsFragment resumed")
    }
}