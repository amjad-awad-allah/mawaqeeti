package com.amjad.mawaqeeti.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.amjad.mawaqeeti.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: return
        val minutesBefore = intent.getIntExtra("MINUTES_BEFORE", 0)

        showNotification(context, prayerName, minutesBefore)
    }

    private fun showNotification(context: Context, prayerName: String, minutesBefore: Int) {
        val channelId = "prayer_alerts"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Prayer Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val message = when (minutesBefore) {
            60 -> "⏳ باقي ساعة على صلاة $prayerName"
            30 -> "🔔 باقي نصف ساعة على صلاة $prayerName"
            15 -> "⚠️ باقي 15 دقيقة على صلاة $prayerName"
            5 -> "🚨 الأذان بعد 5 دقائق! استعد لصلاة $prayerName"
            else -> "اقتربت صلاة $prayerName"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("تذكير الصلاة")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(prayerName.hashCode() + minutesBefore, notification)
    }
}
