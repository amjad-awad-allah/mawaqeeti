package com.amjad.mawaqeeti.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.amjad.mawaqeeti.data.model.PrayerTime
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarmsForPrayers(prayers: List<PrayerTime>) {
        prayers.forEach { prayer ->
            if (!prayer.isPrayed) {
                scheduleMultiPhaseAlarms(prayer)
            }
        }
    }

    private fun scheduleMultiPhaseAlarms(prayer: PrayerTime) {
        val prayerTime = LocalTime.parse(prayer.time, DateTimeFormatter.ofPattern("HH:mm"))
        
        // Phases: 60, 30, 15, 5 minutes before and 0 (Athan time)
        val phases = listOf(60, 30, 15, 5, 0)
        
        phases.forEach { minutesBefore ->
            val nowTime = LocalTime.now()
            val targetTime = prayerTime.minusMinutes(minutesBefore.toLong())
            
            // Determine if it should be scheduled today or tomorrow
            val scheduleDateTime = if (targetTime.isAfter(nowTime)) {
                LocalDateTime.now().with(targetTime)
            } else {
                LocalDateTime.now().plusDays(1).with(targetTime)
            }
            
            scheduleAlarm(prayer.name, scheduleDateTime, minutesBefore)
        }
    }

    private fun scheduleAlarm(prayerName: String, dateTime: LocalDateTime, minutesBefore: Int) {
        val triggerAt = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("PRAYER_NAME", prayerName)
            putExtra("MINUTES_BEFORE", minutesBefore)
            putExtra("SCHEDULED_TIME", triggerAt)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            generateRequestCode(prayerName, minutesBefore),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Fallback for missing exact alarm permission on Android 14+
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        }
    }

    fun cancelAlarmsForPrayer(prayerName: String) {
        listOf(60, 30, 15, 5, 0).forEach { minutesBefore ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                generateRequestCode(prayerName, minutesBefore),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }

    private fun generateRequestCode(prayerName: String, minutesBefore: Int): Int {
        // Unique enough for the 5 prayers and their phases
        return (prayerName.hashCode() * 31 + minutesBefore).let { if (it < 0) -it else it }
    }
}
