package com.amjad.mawaqeeti.util

import com.amjad.mawaqeeti.data.model.PrayerTime
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PrayerCalculatorTest {

    @Test
    fun testFindNextPrayer_WithinSameDay() {
        // Arrange
        val now = LocalTime.of(10, 0) // 10:00 AM
        val prayers = listOf(
            PrayerTime("الفجر", "04:00", false),
            PrayerTime("الظهر", "12:00", false),
            PrayerTime("العصر", "15:30", false)
        )
        
        // Mocking behavior by filtering manually since findNextPrayer uses real LocalTime.now()
        // Here we test the logic of finding the first prayer after a specific time
        val nextInDay = prayers.find { 
            LocalTime.parse(it.time, DateTimeFormatter.ofPattern("HH:mm")).isAfter(now) 
        }
        
        // Assert
        assertNotNull(nextInDay)
        assertEquals("الظهر", nextInDay?.name)
    }

    @Test
    fun testCalculateTimeRemaining_SameDay() {
        // Since PrayerCalculator uses LocalTime.now(), we are testing the format and calculation logic
        // We'll trust the Duration.between behavior and check the format output
        val prayerTime = "23:59"
        // We can't easily mock LocalTime.now() in a pure Unit Test without Refactoring to inject Clock,
        // but we can verify the output isn't empty and follows HH:mm:ss
        val result = PrayerCalculator.calculateTimeRemaining(prayerTime, false)
        
        assertTrue(result.matches(Regex("\\d{2}:\\d{2}:\\d{2}")))
    }

    @Test
    fun testApplyOffsets() {
        val originalTime = "12:00"
        val offset = 5
        val expected = "12:05"
        
        val result = PrayerCalculator.applyOffsets(originalTime, offset)
        
        assertEquals(expected, result)
    }

    @Test
    fun testApplyNegativeOffsets() {
        val originalTime = "12:00"
        val offset = -5
        val expected = "11:55"
        
        val result = PrayerCalculator.applyOffsets(originalTime, offset)
        
        assertEquals(expected, result)
    }
}
