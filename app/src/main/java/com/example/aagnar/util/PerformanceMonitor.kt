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
            androidx.tracing.trace.beginSection(traceName)
        }
    }

    fun endTrace() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            androidx.tracing.trace.endSection()
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
}