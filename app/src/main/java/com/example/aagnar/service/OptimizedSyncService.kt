package com.example.aagnar.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.hilt.work.HiltWorker
import androidx.work.*
// import com.example.aagnar.data.repository.CallRepository
// import com.example.aagnar.data.repository.ContactRepository
// import com.example.aagnar.data.repository.MessageRepository
import com.example.aagnar.data.repository.WebSocketRepository
import com.example.aagnar.util.PerformanceMonitor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class OptimizedSyncService : Service() {

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var performanceMonitor: PerformanceMonitor

    @Inject
    lateinit var webSocketRepository: WebSocketRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isManualSyncInProgress = false

    companion object {
        private const val SYNC_WORK_NAME = "sync_work"
        private const val SYNC_INTERVAL_MINUTES = 15L
        private const val SYNC_FLEX_MINUTES = 5L
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        performanceMonitor.logServiceEvent("OptimizedSyncService started")

        when (intent?.action) {
            "SYNC_NOW" -> {
                performanceMonitor.logServiceEvent("Manual sync requested")
                syncNow()
            }
            "SYNC_MESSAGES_ONLY" -> {
                performanceMonitor.logServiceEvent("Messages-only sync requested")
                syncMessagesOnly()
            }
            "STOP_SYNC" -> {
                performanceMonitor.logServiceEvent("Sync stop requested")
                stopSync()
                stopSelf()
            }
            else -> {
                scheduleOptimizedSync()
            }
        }

        return START_STICKY
    }

    private fun scheduleOptimizedSync() {
        performanceMonitor.logServiceEvent("Starting scheduleOptimizedSync")

        val syncConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(false)
            .setRequiresStorageNotLow(true)
            .build()

        val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = SYNC_INTERVAL_MINUTES,
            TimeUnit.MINUTES,
            flexTimeInterval = SYNC_FLEX_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(syncConstraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                10_000L, // 10 секунд
                TimeUnit.MILLISECONDS
            )
            .addTag("background_sync")
            .build()

        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWork
        )

        performanceMonitor.logServiceEvent("Periodic sync scheduled every ${SYNC_INTERVAL_MINUTES}min")
    }

    private fun syncNow() {
        if (isManualSyncInProgress) {
            performanceMonitor.logPerformanceIssue("Manual sync already in progress")
            return
        }

        isManualSyncInProgress = true
        serviceScope.launch {
            performanceMonitor.logServiceEvent("Starting manual sync")

            try {
                val syncNowWork = OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .addTag("manual_sync")
                    .build()

                workManager.enqueue(syncNowWork)
                performanceMonitor.logServiceEvent("Manual sync enqueued successfully")
            } catch (e: Exception) {
                performanceMonitor.logPerformanceIssue("Failed to enqueue manual sync: ${e.message}")

            } finally {
                isManualSyncInProgress = false
                performanceMonitor.logServiceEvent("Manual sync completed")
            }
        }
    }

    private fun syncMessagesOnly() {
        serviceScope.launch {
            performanceMonitor.logServiceEvent("Starting messages-only sync")

            // ВРЕМЕННО: используем SyncWorker вместо MessagesSyncWorker
            val messagesOnlyWork = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag("messages_sync")
                .build()

            workManager.enqueue(messagesOnlyWork)
            performanceMonitor.logServiceEvent("Messages-only sync enqueued")
        }
    }

    private fun stopSync() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
        workManager.cancelAllWorkByTag("manual_sync")
        workManager.cancelAllWorkByTag("messages_sync")
        performanceMonitor.logServiceEvent("All sync work cancelled")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        performanceMonitor.logServiceEvent("OptimizedSyncService destroyed")
    }
}
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val performanceMonitor: PerformanceMonitor
    // ВРЕМЕННО: убираем репозитории
    // private val messageRepository: MessageRepository,
    // private val contactRepository: ContactRepository,
    // private val callRepository: CallRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        performanceMonitor.logServiceEvent("SyncWorker started - attempt ${runAttemptCount + 1}")

        return try {
            // ВРЕМЕННАЯ ЗАГЛУШКА - просто логируем успех
            performanceMonitor.logServiceEvent("SyncWorker completed successfully")

            // Имитируем какую-то работу
            kotlinx.coroutines.delay(1000) // 1 секунда задержки

            Result.success()

        } catch (e: Exception) {
            performanceMonitor.logPerformanceIssue("SyncWorker failed: ${e.message}")

            if (runAttemptCount < 3) {
                performanceMonitor.logServiceEvent("Retrying sync (attempt ${runAttemptCount + 1})")
                Result.retry()
            } else {
                performanceMonitor.logPerformanceIssue("SyncWorker failed after 3 attempts")
                Result.failure()
            }
        }
    }
}