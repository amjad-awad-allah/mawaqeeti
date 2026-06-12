package com.amjad.mawaqeeti.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mawaqeeti_prefs")

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val SELECTED_CITY = stringPreferencesKey("selectedCity")
        val SELECTED_COUNTRY = stringPreferencesKey("selectedCountry")
        val SELECTED_METHOD = intPreferencesKey("selectedMethod")
        val PRAYER_TIMES_JSON = stringPreferencesKey("prayerTimesJson")
        val NEXT_DAY_FAJR_TIME = stringPreferencesKey("nextDayFajrTime")
        val LAST_UPDATE_DATE = stringPreferencesKey("lastPrayerDataUpdateDate")
        
        val FAJR_PRAYED = booleanPreferencesKey("fajrPrayed")
        val DHUHR_PRAYED = booleanPreferencesKey("dhuhrPrayed")
        val ASR_PRAYED = booleanPreferencesKey("asrPrayed")
        val MAGHRIB_PRAYED = booleanPreferencesKey("maghribPrayed")
        val ISHA_PRAYED = booleanPreferencesKey("ishaPrayed")
        
        val CURRENT_STREAK = intPreferencesKey("currentStreak")
        val BEST_STREAK = intPreferencesKey("bestStreak")
        val LAST_COMPLETED_DAY = stringPreferencesKey("lastCompletedDay")
        
        val LAVA_PERFORMANCE_MODE = stringPreferencesKey("lavaPerformanceMode")
        
        // Manual Offsets (Minutes)
        val FAJR_OFFSET = intPreferencesKey("fajrOffset")
        val DHUHR_OFFSET = intPreferencesKey("dhuhrOffset")
        val ASR_OFFSET = intPreferencesKey("asrOffset")
        val MAGHRIB_OFFSET = intPreferencesKey("maghribOffset")
        val ISHA_OFFSET = intPreferencesKey("ishaOffset")
        
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val ADHAN_ENABLED = booleanPreferencesKey("adhan_enabled")
    }

    // Getters
    val selectedCity: Flow<String> = context.dataStore.data.map { it[SELECTED_CITY] ?: "Essen" }
    val selectedCountry: Flow<String> = context.dataStore.data.map { it[SELECTED_COUNTRY] ?: "Germany" }
    val selectedMethod: Flow<Int> = context.dataStore.data.map { it[SELECTED_METHOD] ?: 3 }
    val prayerTimesJson: Flow<String?> = context.dataStore.data.map { it[PRAYER_TIMES_JSON] }
    val nextDayFajrTime: Flow<String?> = context.dataStore.data.map { it[NEXT_DAY_FAJR_TIME] }
    val lastUpdateDate: Flow<String?> = context.dataStore.data.map { it[LAST_UPDATE_DATE] }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    val adhanEnabled: Flow<Boolean> = context.dataStore.data.map { it[ADHAN_ENABLED] ?: true }

    val prayerStates: Flow<Map<String, Boolean>> = context.dataStore.data.map { prefs ->
        mapOf(
            "Fajr" to (prefs[FAJR_PRAYED] ?: false),
            "Dhuhr" to (prefs[DHUHR_PRAYED] ?: false),
            "Asr" to (prefs[ASR_PRAYED] ?: false),
            "Maghrib" to (prefs[MAGHRIB_PRAYED] ?: false),
            "Isha" to (prefs[ISHA_PRAYED] ?: false)
        )
    }

    val currentStreak: Flow<Int> = context.dataStore.data.map { it[CURRENT_STREAK] ?: 0 }
    val bestStreak: Flow<Int> = context.dataStore.data.map { it[BEST_STREAK] ?: 0 }
    val lastCompletedDay: Flow<String?> = context.dataStore.data.map { it[LAST_COMPLETED_DAY] }
    val lavaPerformanceMode: Flow<String> = context.dataStore.data.map { it[LAVA_PERFORMANCE_MODE] ?: "BALANCED" }

    val fajrOffset: Flow<Int> = context.dataStore.data.map { it[FAJR_OFFSET] ?: 0 }
    val dhuhrOffset: Flow<Int> = context.dataStore.data.map { it[DHUHR_OFFSET] ?: 0 }
    val asrOffset: Flow<Int> = context.dataStore.data.map { it[ASR_OFFSET] ?: 0 }
    val maghribOffset: Flow<Int> = context.dataStore.data.map { it[MAGHRIB_OFFSET] ?: 0 }
    val ishaOffset: Flow<Int> = context.dataStore.data.map { it[ISHA_OFFSET] ?: 0 }

    // Setters
    suspend fun updateLocation(city: String, country: String, method: Int) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_CITY] = city
            prefs[SELECTED_COUNTRY] = country
            prefs[SELECTED_METHOD] = method
        }
    }

    suspend fun savePrayerTimes(json: String, nextFajr: String, date: String) {
        context.dataStore.edit { prefs ->
            val lastDate = prefs[LAST_UPDATE_DATE]
            
            prefs[PRAYER_TIMES_JSON] = json
            prefs[NEXT_DAY_FAJR_TIME] = nextFajr
            prefs[LAST_UPDATE_DATE] = date
            
            // Only reset prayer states if the date is actually different from the last update
            if (lastDate != date) {
                resetPrayerStates(prefs)
            }
        }
    }

    private fun resetPrayerStates(prefs: MutablePreferences) {
        prefs[FAJR_PRAYED] = false
        prefs[DHUHR_PRAYED] = false
        prefs[ASR_PRAYED] = false
        prefs[MAGHRIB_PRAYED] = false
        prefs[ISHA_PRAYED] = false
    }

    suspend fun setPrayerPrayed(prayerName: String, prayed: Boolean) {
        context.dataStore.edit { prefs ->
            when (prayerName) {
                "Fajr" -> prefs[FAJR_PRAYED] = prayed
                "Dhuhr" -> prefs[DHUHR_PRAYED] = prayed
                "Asr" -> prefs[ASR_PRAYED] = prayed
                "Maghrib" -> prefs[MAGHRIB_PRAYED] = prayed
                "Isha" -> prefs[ISHA_PRAYED] = prayed
            }
        }
    }

    suspend fun updateStreak(current: Int, best: Int, lastDay: String) {
        context.dataStore.edit { prefs ->
            prefs[CURRENT_STREAK] = current
            prefs[BEST_STREAK] = best
            prefs[LAST_COMPLETED_DAY] = lastDay
        }
    }

    suspend fun setLavaMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[LAVA_PERFORMANCE_MODE] = mode
        }
    }

    suspend fun setOffset(prayerName: String, offset: Int) {
        context.dataStore.edit { prefs ->
            when (prayerName) {
                "Fajr" -> prefs[FAJR_OFFSET] = offset
                "Dhuhr" -> prefs[DHUHR_OFFSET] = offset
                "Asr" -> prefs[ASR_OFFSET] = offset
                "Maghrib" -> prefs[MAGHRIB_OFFSET] = offset
                "Isha" -> prefs[ISHA_OFFSET] = offset
            }
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setAdhanEnabled(enabled: Boolean) {
        context.dataStore.edit { it[ADHAN_ENABLED] = enabled }
    }
}
