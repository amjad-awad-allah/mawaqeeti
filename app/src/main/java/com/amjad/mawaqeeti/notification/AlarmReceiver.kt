package com.amjad.mawaqeeti.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.amjad.mawaqeeti.R
import com.amjad.mawaqeeti.data.local.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: return
        val minutesBefore = intent.getIntExtra("MINUTES_BEFORE", 0)

        val dataStore = DataStoreManager(context)
        runBlocking {
            val notificationsEnabled = dataStore.notificationsEnabled.first()
            val adhanEnabled = dataStore.adhanEnabled.first()

            if (!notificationsEnabled) return@runBlocking

            // If it's Adhan time but Adhan sound is disabled, we change the soundUri or behavior
            val effectiveAdhanEnabled = if (minutesBefore == 0) adhanEnabled else true

            showNotification(context, prayerName, minutesBefore, effectiveAdhanEnabled)

            // For TV/Full Screen Adhan: Launch Activity when it's exactly prayer time
            if (minutesBefore == 0 && adhanEnabled) {
                launchAdhanActivity(context, prayerName)
            }
        }
    }

    private fun launchAdhanActivity(context: Context, prayerName: String) {
        val intent = Intent(context, com.amjad.mawaqeeti.ui.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("START_DESTINATION", "adhan_reminder/$prayerName")
        }
        context.startActivity(intent)
    }

    private fun showNotification(context: Context, prayerName: String, minutesBefore: Int, soundEnabled: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Identify the correct sound and unique channel for each phase
        val (soundUri, channelId, channelName) = when (minutesBefore) {
            60, 30 -> Triple(
                Uri.parse("android.resource://${context.packageName}/raw/bill"),
                "prayer_alerts_bill_v2",
                "تنبيهات (ساعة/نصف ساعة)"
            )
            15 -> Triple(
                Uri.parse("android.resource://${context.packageName}/raw/notime"),
                "prayer_alerts_notime_v2",
                "تنبيهات (15 دقيقة)"
            )
            0 -> Triple(
                Uri.parse("android.resource://${context.packageName}/raw/athan"),
                "prayer_alerts_athan_full_v2",
                "تنبيه الأذان (الكامل)"
            )
            else -> Triple(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                "prayer_alerts_default_v2",
                "تنبيهات الأذان"
            )
        }

        val finalSoundUri = if (soundEnabled) soundUri else null

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "تنبيهات مخصصة لمواعيد الصلاة"
            enableVibration(true)
            if (finalSoundUri != null) {
                setSound(finalSoundUri, audioAttributes)
            } else {
                setSound(null, null)
            }
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
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("تذكير الصلاة - $prayerName")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(1000, 1000, 1000))
            .setAutoCancel(true)

        // For Full Screen Adhan (TV & Phone Alerts)
        if (minutesBefore == 0 && soundEnabled) {
            val fullScreenIntent = Intent(context, com.amjad.mawaqeeti.ui.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("START_DESTINATION", "adhan_reminder/$prayerName")
            }
            val fullScreenPendingIntent = android.app.PendingIntent.getActivity(
                context, 0, fullScreenIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
        }

        if (finalSoundUri != null) {
            builder.setSound(finalSoundUri)
        }

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
