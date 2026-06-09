package com.amjad.mawaqeeti.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
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

        // Identify the correct sound based on timing
        val soundUri = when (minutesBefore) {
            60, 30 -> Uri.parse("android.resource://${context.packageName}/raw/bill")
            15 -> Uri.parse("android.resource://${context.packageName}/raw/notime")
            else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        val channelName = "تنبيهات الصلاة"
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "تنبيهات مخصصة لمواعيد الصلاة"
            enableVibration(true)
            // Note: Since Android 8.0, sound is set on the channel. 
            // For dynamic sounds per notification, we might need multiple channels or re-create it.
            // But for these fixed phases, we will set it in the builder as well for compatibility.
            setSound(soundUri, null)
        }
        notificationManager.createNotificationChannel(channel)

        val message = when (minutesBefore) {
            60 -> "⏳ باقي ساعة على صلاة $prayerName"
            30 -> "🔔 باقي نصف ساعة على صلاة $prayerName"
            15 -> "⚠️ باقي 15 دقيقة على صلاة $prayerName"
            5 -> "🚨 الأذان بعد 5 دقائق! استعد لصلاة $prayerName"
            else -> "حان الآن موعد صلاة $prayerName"
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("تذكير الصلاة - $prayerName")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(longArrayOf(1000, 1000, 1000))
            .setAutoCancel(true)

        // Limit 'notime' alert to 5 seconds
        if (minutesBefore == 15) {
            builder.setTimeoutAfter(5000)
        }

        val notification = builder.build()

        if (minutesBefore != 15) {
            notification.flags = notification.flags or NotificationCompat.FLAG_INSISTENT
        }

        notificationManager.notify(prayerName.hashCode() + minutesBefore, notification)
    }
}
