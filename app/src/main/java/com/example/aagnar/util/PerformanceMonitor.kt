package com.example.aagnar.util

import android.os.Build
import android.util.Log
import com.example.aagnar.BuildConfig

/**
 * Монитор производительности для AAGNAR v4.0.6-1
 * Оптимизирован для P2P коммуникаций и реального времени
 */
object PerformanceMonitor {

    private const val WARNING_THRESHOLD_MS = 16L // 60 FPS
    private val performanceData = mutableMapOf<String, Long>()

    fun <T> measure(blockName: String, block: () -> T): T {
        val startTime = System.nanoTime()
        try {
            return block()
        } finally {
            val duration = (System.nanoTime() - startTime) / 1_000_000
            performanceData[blockName] = duration

            if (duration > WARNING_THRESHOLD_MS) {
                Log.w("Performance", "$blockName took ${duration}ms")
            }

            if (BuildConfig.DEBUG) {
                logPerformanceData()
            }
        }
    }

    fun startTrace(traceName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            androidx.tracing.Trace.beginSection(traceName)
        }
    }

    fun endTrace() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            androidx.tracing.Trace.endSection()
        }
    }

    private fun logPerformanceData() {
        if (performanceData.isNotEmpty()) {
            val avgPerformance = performanceData.values.average()
            Log.d("Performance", "Average performance: ${avgPerformance}ms")

            performanceData.entries.sortedByDescending { it.value }.take(5).forEach { (name, time) ->
                Log.d("Performance", "Slowest: $name - ${time}ms")
            }
        }
    }

    fun getMemoryInfo(): String {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val maxMemory = runtime.maxMemory() / (1024 * 1024)

        return "Memory: ${usedMemory}MB / ${maxMemory}MB"
    }

    // Новые методы для интеграции с вашим проектом
    fun logRecyclerViewPerformance(adapterName: String, itemCount: Int, bindTime: Long) {
        if (bindTime > WARNING_THRESHOLD_MS) {
            Log.w("Performance", "Slow RecyclerView: $adapterName - ${bindTime}ms for $itemCount items")
        }
    }

    fun logNetworkRequest(url: String, duration: Long, success: Boolean) {
        if (duration > 1000) { // 1 second threshold
            Log.w("Performance", "Slow network: $url - ${duration}ms")
        }
    }

    fun logDatabaseOperation(operation: String, duration: Long, entityCount: Int = 0) {
        if (duration > 100) { // 100ms threshold
            Log.w("Performance", "Slow DB: $operation - ${duration}ms for $entityCount entities")
        }
    }

    fun logWebSocketEvent(event: String, duration: Long? = null) {
        if (duration != null && duration > 100) {
            Log.w("Performance", "Slow WebSocket: $event - ${duration}ms")
        }
    }

    fun logPerformanceIssue(message: String) {
        Log.w("Performance", "Issue: $message")
    }

    fun logServiceEvent(event: String) {
        Log.i("Performance", "Service: $event")
    }

    fun logPerformanceEvent(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("Performance", message)
        }
    }

    fun clearData() {
        performanceData.clear()
    }
}