package com.example.dicto.utils.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * NotificationHelper - Manages foreground service notifications
 *
 * Single Responsibility: Create and manage notification channel and notifications
 * Separation of Concerns: Notification logic separated from service lifecycle
 */
class NotificationHelper(private val context: Service) {

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "floating_translator_channel"
        private const val CHANNEL_NAME = "Floating Translator"
        private const val CHANNEL_DESCRIPTION = "Notification for floating translator service"
    }

    /**
     * Create notification channel (required for Android O+)
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create foreground service notification
     */
    fun createNotification() = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle("Dicto Translator")
        .setContentText("Floating translator is active")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build()
}

