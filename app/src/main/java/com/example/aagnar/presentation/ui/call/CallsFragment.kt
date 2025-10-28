package com.example.aagnar.presentation.ui.calls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aagnar.R
import com.example.aagnar.util.PerformanceMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint  // ← ДОБАВЬТЕ ЭТУ АННОТАЦИЮ!
class CallsFragment : Fragment() {

    // Инжектируйте PerformanceMonitor через Hilt
    @Inject
    lateinit var performanceMonitor: PerformanceMonitor
    private lateinit var emptyState: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return performanceMonitor.measure("CallsFragment.onCreateView") {
            inflater.inflate(R.layout.fragment_calls, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        performanceMonitor.measure("calls_load") {
            initViews(view)
            loadCallsHistory()
        }
    }

    private fun initViews(view: View) {
        performanceMonitor.measure("CallsFragment.initViews") {
            emptyState = view.findViewById(R.id.emptyState)
        }
    }

    private fun loadCallsHistory() {
        performanceMonitor.measure("CallsFragment.loadCallsHistory") {
            // TODO: Реализовать загрузку истории звонков
            showEmptyState(true)
        }
    }

    private fun showEmptyState(show: Boolean) {
        emptyState.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        performanceMonitor.logPerformanceEvent("CallsFragment resumed")
    }
}