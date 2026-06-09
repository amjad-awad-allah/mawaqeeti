package com.amjad.mawaqeeti.util

import com.amjad.mawaqeeti.data.model.PrayerTime
import java.time.LocalTime
import java.time.Duration
import java.time.format.DateTimeFormatter

object PrayerCalculator {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    fun applyOffsets(time: String, offsetMinutes: Int): String {
        if (offsetMinutes == 0) return time
        val prayerTime = LocalTime.parse(time, formatter)
        return prayerTime.plusMinutes(offsetMinutes.toLong()).format(formatter)
    }

    fun findNextPrayer(prayers: List<PrayerTime>, nextDayFajr: String?): Pair<PrayerTime, Boolean> {
        val now = LocalTime.now()
        
        // Find the first prayer that hasn't happened yet AND isn't prayed
        for (prayer in prayers) {
            val prayerTime = LocalTime.parse(prayer.time, formatter)
            if (now.isBefore(prayerTime) && !prayer.isPrayed) {
                return prayer to false // isNextDay = false
            }
        }
        
        // If all today's prayers are passed or prayed, next is Fajr of tomorrow
        // Note: We use Arabic for consistency if we are in Arabic mode, 
        // but domain model uses English Fajr usually. 
        // Let's use the first prayer from the list but with tomorrow's time.
        val tomorrowFajr = prayers.first().copy(name = "الفجر", time = nextDayFajr ?: "04:30", isPrayed = false)
        return tomorrowFajr to true // isNextDay = true
    }

    fun calculateTimeRemaining(prayerTime: String, isNextDay: Boolean): String {
        val now = LocalTime.now()
        val target = LocalTime.parse(prayerTime, formatter)
        
        val duration = if (isNextDay) {
            val untilMidnight = Duration.between(now, LocalTime.MAX).plusSeconds(1)
            val fromMidnight = Duration.between(LocalTime.MIN, target)
            untilMidnight.plus(fromMidnight)
        } else {
            Duration.between(now, target)
        }
        
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun getProgress(prayerStates: Map<*, *>): Int {
        return prayerStates.values.count { it as Boolean }
    }

    fun getProgressPercentage(prayerStates: Map<String, Boolean>): Float {
        return getProgress(prayerStates) / 5f
    }
}
