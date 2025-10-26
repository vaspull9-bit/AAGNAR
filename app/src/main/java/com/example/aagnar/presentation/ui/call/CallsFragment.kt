package com.example.aagnar.presentation.ui.calls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aagnar.R
import com.example.aagnar.util.PerformanceMonitor

class CallsFragment : Fragment() {

    private lateinit var emptyState: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        PerformanceMonitor.measure("CallsFragment.onCreateView") {
            return inflater.inflate(R.layout.fragment_calls, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        PerformanceMonitor.measure("CallsFragment.onViewCreated") {
            initViews(view)
            loadCallsHistory()
        }
    }

    private fun initViews(view: View) {
        PerformanceMonitor.measure("CallsFragment.initViews") {
            emptyState = view.findViewById(R.id.emptyState)
        }
    }

    private fun loadCallsHistory() {
        PerformanceMonitor.measure("CallsFragment.loadCallsHistory") {
            // TODO: Реализовать загрузку истории звонков
            showEmptyState(true)
        }
    }

    private fun showEmptyState(show: Boolean) {
        emptyState.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        PerformanceMonitor.logPerformanceEvent("CallsFragment resumed")
    }
}