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
        
        // Find the first prayer that hasn't happened yet
        for (prayer in prayers) {
            val prayerTime = LocalTime.parse(prayer.time, formatter)
            if (now.isBefore(prayerTime)) {
                return prayer to false // isNextDay = false
            }
        }
        
        // If all today's prayers passed, next is Fajr of tomorrow
        return PrayerTime("Fajr", nextDayFajr ?: "04:30") to true // isNextDay = true
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

    fun getProgress(prayerStates: Map<String, Boolean>): Int {
        return prayerStates.values.count { it }
    }

    fun getProgressPercentage(prayerStates: Map<String, Boolean>): Float {
        return getProgress(prayerStates) / 5f
    }
}
