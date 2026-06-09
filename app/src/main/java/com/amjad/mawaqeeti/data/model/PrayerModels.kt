package com.amjad.mawaqeeti.data.model

import com.google.gson.annotations.SerializedName

data class AladhanResponse(
    val code: Int,
    val status: String,
    val data: PrayerData
)

data class PrayerData(
    val timings: Timings,
    val date: DateInfo
)

data class Timings(
    val Fajr: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String
)

data class DateInfo(
    val readable: String,
    val timestamp: String,
    val hijri: HijriDate,
    val gregorian: GregorianDate
)

data class HijriDate(
    val date: String,
    val month: HijriMonth,
    val year: String
)

data class HijriMonth(
    val number: Int,
    val en: String,
    val ar: String
)

data class GregorianDate(
    val date: String,
    val day: String,
    val month: GregorianMonth,
    val year: String
)

data class GregorianMonth(
    val number: Int,
    val en: String
)

// Domain Model
data class PrayerTime(
    val name: String,
    val time: String, // HH:mm
    val isPrayed: Boolean = false
)
