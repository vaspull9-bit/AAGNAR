package com.example.aagnar.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import androidx.work.*
import com.example.aagnar.data.repository.CallRepository
import com.example.aagnar.data.repository.ContactRepository
import com.example.aagnar.data.repository.MessageRepository
import com.example.aagnar.data.repository.WebSocketRepository
import com.example.aagnar.util.PerformanceMonitor
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
        private const val CONTACT_SYNC_PREFS = "sync"
        private const val LAST_CONTACT_SYNC_KEY = "last_contact_sync"

        // Константы как в исходном коде
        private const val SYNC_INTERVAL_MINUTES = 15L
        private const val SYNC_FLEX_MINUTES = 5L
        private const val CONTACT_SYNC_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 часа
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

    // Сохраняем вашу основную логику планирования
    private fun scheduleOptimizedSync() {
        performanceMonitor.startTrace("scheduleOptimizedSync")

        // Используем ваши constraints с небольшими улучшениями
        val syncConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(false) // Сохраняем вашу логику - не требовать зарядку
            .setRequiresStorageNotLow(true) // Новое: проверяем место в хранилище
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
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag("background_sync")
            .build()

        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Сохраняем вашу политику
            syncWork
        )

        performanceMonitor.stopTrace("scheduleOptimizedSync")
        performanceMonitor.logServiceEvent("Periodic sync scheduled every ${SYNC_INTERVAL_MINUTES}min")
    }

    // Новый метод для ручной синхронизации
    private fun syncNow() {
        if (isManualSyncInProgress) {
            performanceMonitor.logPerformanceIssue("Manual sync already in progress")
            return
        }

        isManualSyncInProgress = true
        serviceScope.launch {
            performanceMonitor.startTrace("manualSync")

            try {
                // Используем WorkManager для немедленной синхронизации
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
                performanceMonitor.logError("Failed to enqueue manual sync: ${e.message}")
            } finally {
                isManualSyncInProgress = false
                performanceMonitor.stopTrace("manualSync")
            }
        }
    }

    // Новый метод для синхронизации только сообщений
    private fun syncMessagesOnly() {
        serviceScope.launch {
            performanceMonitor.startTrace("messagesOnlySync")

            val messagesOnlyWork = OneTimeWorkRequestBuilder<MessagesSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag("messages_sync")
                .build()

            workManager.enqueue(messagesOnlyWork)
            performanceMonitor.stopTrace("messagesOnlySync")
        }
    }

    // Сохраняем вашу логику остановки синхронизации
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
        performanceMonitor.flushMetrics()
    }
}

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val messageRepository: MessageRepository,
    private val contactRepository: ContactRepository,
    private val callRepository: CallRepository,
    private val performanceMonitor: PerformanceMonitor
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        performanceMonitor.startTrace("SyncWorker.doWork")
        performanceMonitor.logServiceEvent("SyncWorker started - attempt ${runAttemptCount + 1}")

        return try {
            // Мониторинг производительности
            performanceMonitor.startTrace("getUnsyncedMessages")
            val unsyncedMessages = messageRepository.getUnsyncedMessages()
            performanceMonitor.stopTrace("getUnsyncedMessages")
            performanceMonitor.logDatabaseOperation("getUnsyncedMessages", 0, unsyncedMessages.size)

            // Сохраняем вашу оптимизацию: синхронизируем только непрочитанные сообщения
            if (unsyncedMessages.isNotEmpty()) {
                performanceMonitor.logServiceEvent("Syncing ${unsyncedMessages.size} unsynced messages")
                syncMessages(unsyncedMessages)
            } else {
                performanceMonitor.logServiceEvent("No unsynced messages to sync")
            }

            // Сохраняем вашу логику синхронизации контактов раз в день
            if (shouldSyncContacts()) {
                performanceMonitor.logServiceEvent("Syncing contacts (24h interval)")
                syncContacts()
            } else {
                performanceMonitor.logServiceEvent("Skipping contacts sync (not yet due)")
            }

            // Новая функция: синхронизация истории звонков
            if (shouldSyncCalls()) {
                performanceMonitor.logServiceEvent("Syncing call history")
                syncCalls()
            }

            // Обновляем время последней успешной синхронизации
            updateLastSyncTime()

            performanceMonitor.logServiceEvent("SyncWorker completed successfully")
            performanceMonitor.stopTrace("SyncWorker.doWork")
            Result.success()

        } catch (e: Exception) {
            performanceMonitor.logError("SyncWorker failed: ${e.message}")
            performanceMonitor.stopTrace("SyncWorker.doWork")

            // Сохраняем вашу логику повторных попыток
            if (runAttemptCount < 3) {
                performanceMonitor.logServiceEvent("Retrying sync (attempt ${runAttemptCount + 1})")
                Result.retry()
            } else {
                performanceMonitor.logPerformanceIssue("SyncWorker failed after 3 attempts")
                Result.failure()
            }
        }
    }

    // Сохраняем ваш метод синхронизации сообщений с улучшениями
    private suspend fun syncMessages(messages: List<Message>) {
        performanceMonitor.startTrace("syncMessages")

        try {
            // Пакетная синхронизация (как в вашем коде)
            messageRepository.syncMessages(messages)
            performanceMonitor.logDatabaseOperation("syncMessages", 0, messages.size)

        } catch (e: Exception) {
            performanceMonitor.logError("Message sync failed: ${e.message}")
            throw e
        } finally {
            performanceMonitor.stopTrace("syncMessages")
        }
    }

    // Сохраняем ваш метод синхронизации контактов
    private suspend fun syncContacts() {
        performanceMonitor.startTrace("syncContacts")

        try {
            contactRepository.syncContacts()
            updateLastContactSyncTime()
            performanceMonitor.logServiceEvent("Contacts sync completed")

        } catch (e: Exception) {
            performanceMonitor.logError("Contact sync failed: ${e.message}")
            throw e
        } finally {
            performanceMonitor.stopTrace("syncContacts")
        }
    }

    // Новый метод для синхронизации звонков
    private suspend fun syncCalls() {
        performanceMonitor.startTrace("syncCalls")

        try {
            callRepository.syncCalls()
            updateLastCallSyncTime()
            performanceMonitor.logServiceEvent("Calls sync completed")

        } catch (e: Exception) {
            performanceMonitor.logError("Call sync failed: ${e.message}")
            // Не бросаем исключение, чтобы не прерывать всю синхронизацию
        } finally {
            performanceMonitor.stopTrace("syncCalls")
        }
    }

    // Сохраняем вашу логику проверки необходимости синхронизации контактов
    private fun shouldSyncContacts(): Boolean {
        val lastSync = getLastContactSyncTime()
        val shouldSync = System.currentTimeMillis() - lastSync > CONTACT_SYNC_INTERVAL_MS
        performanceMonitor.logServiceEvent("Should sync contacts: $shouldSync (last sync: $lastSync)")
        return shouldSync
    }

    // Новый метод для проверки синхронизации звонков
    private fun shouldSyncCalls(): Boolean {
        val lastSync = getLastCallSyncTime()
        return System.currentTimeMillis() - lastSync > CONTACT_SYNC_INTERVAL_MS // Та же периодичность
    }

    // Сохраняем ваши методы работы с SharedPreferences
    private fun getLastContactSyncTime(): Long {
        val prefs = applicationContext.getSharedPreferences(CONTACT_SYNC_PREFS, Context.MODE_PRIVATE)
        return prefs.getLong(LAST_CONTACT_SYNC_KEY, 0)
    }

    private fun updateLastContactSyncTime() {
        val prefs = applicationContext.getSharedPreferences(CONTACT_SYNC_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putLong(LAST_CONTACT_SYNC_KEY, System.currentTimeMillis()).apply()
    }

    private fun getLastCallSyncTime(): Long {
        val prefs = applicationContext.getSharedPreferences(CONTACT_SYNC_PREFS, Context.MODE_PRIVATE)
        return prefs.getLong("last_call_sync", 0)
    }

    private fun updateLastCallSyncTime() {
        val prefs = applicationContext.getSharedPreferences(CONTACT_SYNC_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putLong("last_call_sync", System.currentTimeMillis()).apply()
    }

    private fun updateLastSyncTime() {
        val prefs = applicationContext.getSharedPreferences(CONTACT_SYNC_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putLong("last_successful_sync", System.currentTimeMillis()).apply()
    }
}

// Новый Worker для синхронизации только сообщений
@HiltWorker
class MessagesSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val messageRepository: MessageRepository,
    private val performanceMonitor: PerformanceMonitor
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        performanceMonitor.startTrace("MessagesSyncWorker.doWork")

        return try {
            val unsyncedMessages = messageRepository.getUnsyncedMessages()
            performanceMonitor.logServiceEvent("Messages-only sync: ${unsyncedMessages.size} messages")

            if (unsyncedMessages.isNotEmpty()) {
                messageRepository.syncMessages(unsyncedMessages)
                performanceMonitor.logServiceEvent("Messages-only sync completed successfully")
            }

            performanceMonitor.stopTrace("MessagesSyncWorker.doWork")
            Result.success()
        } catch (e: Exception) {
            performanceMonitor.logError("Messages-only sync failed: ${e.message}")
            performanceMonitor.stopTrace("MessagesSyncWorker.doWork")
            Result.retry()
        }
    }
}