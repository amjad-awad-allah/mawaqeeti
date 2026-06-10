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
        
        for (prayer in prayers) {
            val prayerTime = LocalTime.parse(prayer.time, formatter)
            if (now.isBefore(prayerTime)) {
                return prayer to false
            }
        }
        
        val tomorrowFajr = prayers.first().copy(name = "الفجر", time = nextDayFajr ?: "04:30", isPrayed = false)
        return tomorrowFajr to true
    }

    fun findActivePrayer(prayers: List<PrayerTime>): PrayerTime? {
        val now = LocalTime.now()
        // The active prayer is the latest one that has already started
        return prayers.lastOrNull { 
            val prayerTime = LocalTime.parse(it.time, formatter)
            now.isAfter(prayerTime) || now == prayerTime
        }
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
