@AndroidEntryPoint
class OptimizedSyncService : Service() {

    private val workManager = WorkManager.getInstance(applicationContext)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scheduleOptimizedSync()
        return START_STICKY
    }

    private fun scheduleOptimizedSync() {
        // Используем WorkManager для оптимизированной синхронизации
        val syncConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(false) // Не требовать зарядку для экономии UX
            .build()

        val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15, TimeUnit.MINUTES, // Реже в фоне
            flexTimeInterval = 5, TimeUnit.MINUTES
        )
            .setConstraints(syncConstraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "sync_work",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWork
        )
    }
}

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val messageRepository: MessageRepository,
    private val contactRepository: ContactRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Оптимизация: синхронизируем только непрочитанные сообщения
            val unsyncedMessages = messageRepository.getUnsyncedMessages()
            if (unsyncedMessages.isNotEmpty()) {
                syncMessages(unsyncedMessages)
            }

            // Синхронизируем контакты раз в день
            if (shouldSyncContacts()) {
                syncContacts()
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun syncMessages(messages: List<Message>) {
        // Пакетная синхронизация
        messageRepository.syncMessages(messages)
    }

    private suspend fun syncContacts() {
        contactRepository.syncContacts()
    }

    private fun shouldSyncContacts(): Boolean {
        val lastSync = getLastContactSyncTime()
        return System.currentTimeMillis() - lastSync > 24 * 60 * 60 * 1000 // 24 часа
    }

    private fun getLastContactSyncTime(): Long {
        val prefs = applicationContext.getSharedPreferences("sync", Context.MODE_PRIVATE)
        return prefs.getLong("last_contact_sync", 0)
    }
}