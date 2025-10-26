package com.example.aagnar.presentation.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.aagnar.R
import com.example.aagnar.util.PerformanceMonitor

class SettingsFragment : Fragment() {

    private lateinit var settingsRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        PerformanceMonitor.measure("SettingsFragment.onViewCreated") {
            initViews(view)
            setupRecyclerView()
        }
    }

    private fun initViews(view: View) {
        settingsRecyclerView = view.findViewById(R.id.settingsRecyclerView)
    }

    private fun setupRecyclerView() {
        // TODO: Настроить адаптер настроек
    }
}