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
        
        // Find the first prayer that occurs in the FUTURE and is NOT prayed
        for (prayer in prayers) {
            val prayerTime = LocalTime.parse(prayer.time, formatter)
            if (now.isBefore(prayerTime)) {
                if (!prayer.isPrayed) {
                    return prayer to false // Valid upcoming unprayed prayer
                } else {
                    // This prayer is in the future but already marked as done, 
                    // so we keep looking for the next one.
                    continue 
                }
            }
        }
        
        // If all today's prayers are in the past OR already prayed, 
        // the next prayer is tomorrow's Fajr
        val tomorrowFajr = prayers.first().copy(name = "الفجر", time = nextDayFajr ?: "04:30", isPrayed = false)
        return tomorrowFajr to true
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
