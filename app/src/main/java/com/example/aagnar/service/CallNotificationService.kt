package com.example.aagnar.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.aagnar.R

class CallNotificationService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "call_channel"

        fun startService(context: Context, contactName: String, isVideoCall: Boolean) {
            val intent = Intent(context, CallNotificationService::class.java).apply {
                putExtra("contact_name", contactName)
                putExtra("is_video_call", isVideoCall)
            }
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, CallNotificationService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val contactName = intent?.getStringExtra("contact_name") ?: "Unknown"
        val isVideoCall = intent?.getBooleanExtra("is_video_call", true) ?: true

        val notification = createNotification(contactName, isVideoCall)
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Входящие звонки",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о входящих звонках"
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contactName: String, isVideoCall: Boolean): Notification {
        val callType = if (isVideoCall) "Видеозвонок" else "Аудиозвонок"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Входящий $callType")
            .setContentText("$contactName вызывает вас")
            .setSmallIcon(R.drawable.ic_call)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(null, true)
            .setAutoCancel(true)
            .build()
    }
}