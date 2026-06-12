package com.amjad.mawaqeeti.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.UiModeManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.amjad.mawaqeeti.R
import com.amjad.mawaqeeti.data.local.DataStoreManager
import com.amjad.mawaqeeti.ui.MainActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: return
        val minutesBefore = intent.getIntExtra("MINUTES_BEFORE", 0)
        val scheduledTime = intent.getLongExtra("SCHEDULED_TIME", 0L)

        // Wake up the CPU to ensure the alert processes
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Mawaqeeti:AlarmWakeLock")
        wakeLock?.acquire(10 * 1000L) // Stay awake for 10 seconds

        // Prevent "stacking" of old alarms after TV boot/time sync
        if (scheduledTime != 0L && System.currentTimeMillis() - scheduledTime > 10 * 60 * 1000) {
            return
        }

        val dataStore = DataStoreManager(context)
        runBlocking {
            val soundEnabled = dataStore.adhanEnabled.first()
            val notificationEnabled = dataStore.notificationsEnabled.first()
            
            if (notificationEnabled) {
                showNotification(context, prayerName, minutesBefore, soundEnabled)
            }
        }
    }

    private fun showNotification(context: Context, prayerName: String, minutesBefore: Int, soundEnabled: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Identify the correct sound and unique channel for each phase
        val (soundUri, channelId, channelName) = when (minutesBefore) {
            60, 30 -> Triple(
                Uri.parse("android.resource://${context.packageName}/raw/bill"),
                "prayer_alerts_bill_v3",
                "تنبيهات (ساعة/نصف ساعة)"
            )
            15 -> Triple(
                Uri.parse("android.resource://${context.packageName}/raw/notime"),
                "prayer_alerts_notime_v3",
                "تنبيهات (15 دقيقة)"
            )
            0 -> Triple(
                Uri.parse("android.resource://${context.packageName}/raw/athan"),
                "prayer_alerts_athan_full_v3",
                "تنبيه الأذان (الكامل)"
            )
            else -> Triple(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                "prayer_alerts_default_v3",
                "تنبيهات الأذان"
            )
        }

        val finalSoundUri = if (soundEnabled) soundUri else null

        // Create notification channel (required for Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "قناة تنبيهات مواقيت الصلاة"
                enableLights(true)
                setLightColor(android.graphics.Color.GREEN)
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                if (finalSoundUri != null) {
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    setSound(finalSoundUri, audioAttributes)
                } else {
                    setSound(null, null)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("START_DESTINATION", "adhan_reminder/$prayerName")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            generateRequestCode(prayerName, minutesBefore), 
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Device Type Check
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        val isTV = uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(context.applicationInfo.icon)
            .setContentTitle(if (minutesBefore == 0) "حان الآن وقت أذان $prayerName" else "تنبيه اقتراب صلاة $prayerName")
            .setContentText(if (minutesBefore == 0) "رفع الله قدرك في الدارين" else "بقي $minutesBefore دقيقة على الأذان")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (finalSoundUri != null) {
            builder.setSound(finalSoundUri)
        }

        // IMPORTANT: Only use Full Screen Intent on TV for Athan (minutesBefore == 0)
        if (isTV && minutesBefore == 0) {
            android.util.Log.d("Mawaqeeti", "Launching Full Screen Adhan for $prayerName")
            
            // Force start activity directly for immediate response on TV
            try {
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                context.startActivity(mainIntent)
                android.util.Log.d("Mawaqeeti", "startActivity called successfully")
            } catch (e: Exception) {
                android.util.Log.e("Mawaqeeti", "CRITICAL: Failed to start activity: ${e.message}")
            }
            
            builder.setFullScreenIntent(pendingIntent, true)
        }

        notificationManager.notify(generateRequestCode(prayerName, minutesBefore), builder.build())
    }

    private fun generateRequestCode(prayerName: String, minutesBefore: Int): Int {
        return (prayerName.hashCode() * 31 + minutesBefore).let { if (it < 0) -it else it }
    }
}
