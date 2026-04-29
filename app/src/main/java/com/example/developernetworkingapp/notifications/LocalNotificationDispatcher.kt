package com.example.developernetworkingapp.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.developernetworkingapp.R
import com.example.developernetworkingapp.data.repository.NotificationDispatcher
import kotlin.random.Random

class LocalNotificationDispatcher(
    private val context: Context
) : NotificationDispatcher {
    override fun showLocalNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val notification = NotificationCompat.Builder(context, NotificationChannels.DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(Random.nextInt(1000, 9999), notification)
    }
}

object NotificationChannels {
    const val DEFAULT_CHANNEL_ID = "devconnect_alerts"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = android.app.NotificationChannel(
            DEFAULT_CHANNEL_ID,
            "DevConnect Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Project reminders and collaboration alerts"
        }
        manager.createNotificationChannel(channel)
    }
}
