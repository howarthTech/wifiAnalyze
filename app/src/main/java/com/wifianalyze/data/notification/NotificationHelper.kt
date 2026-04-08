package com.wifianalyze.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.wifianalyze.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "wifi_alerts"
        const val NOTIFICATION_ID         = 1001
        const val NOTIFICATION_ID_CLEAR   = 1002
        const val NOTIFICATION_ID_CHANNEL = 1003

        fun createChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WiFi Signal Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts when your WiFi signal drops below the configured threshold"
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun sendSignalAlert(ssid: String, rssi: Int, thresholdDbm: Int) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Weak WiFi Signal")
            .setContentText("$ssid: $rssi dBm (threshold: $thresholdDbm dBm)")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your WiFi signal on \"$ssid\" has dropped to $rssi dBm, below your alert threshold of $thresholdDbm dBm."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(buildPendingIntent())
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }

    fun sendSignalClearAlert(ssid: String, rssi: Int, thresholdDbm: Int) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("WiFi Signal Recovered")
            .setContentText("$ssid: $rssi dBm — back above $thresholdDbm dBm threshold")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(buildPendingIntent())
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java).apply {
            cancel(NOTIFICATION_ID)
            notify(NOTIFICATION_ID_CLEAR, notification)
        }
    }

    fun sendChannelRecommendation(ssid: String, currentChannel: Int, suggestedChannel: Int, competingCount: Int) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Channel Congestion Detected")
            .setContentText("$ssid: $competingCount networks on Ch $currentChannel. Try Ch $suggestedChannel.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("\"$ssid\" is on channel $currentChannel with $competingCount other networks. " +
                    "Switching your router to channel $suggestedChannel may improve performance."))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(buildPendingIntent())
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID_CHANNEL, notification)
    }

    private fun hasNotificationPermission() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun buildPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}
