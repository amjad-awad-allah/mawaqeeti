package com.amjad.mawaqeeti.data.repository

import com.amjad.mawaqeeti.data.local.DataStoreManager
import com.amjad.mawaqeeti.data.remote.AladhanApi
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import androidx.glance.appwidget.updateAll
import com.amjad.mawaqeeti.widget.PrayerWidget
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class PrayerRepository @Inject constructor(
    private val api: AladhanApi,
    private val dataStore: DataStoreManager,
    private val gson: Gson,
    @ApplicationContext private val context: Context
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun refreshPrayerTimesIfNecessary() {
        val today = LocalDate.now()
        val todayStr = today.format(isoFormatter)
        val lastUpdate = dataStore.lastUpdateDate.first()

        if (lastUpdate != todayStr) {
            try {
                val city = dataStore.selectedCity.first()
                val country = dataStore.selectedCountry.first()
                val method = dataStore.selectedMethod.first()

                // Fetch today's times
                val response = api.getTimings(
                    today.format(dateFormatter),
                    city, country, method
                )

                // Fetch tomorrow's Fajr
                val tomorrowResponse = api.getTimings(
                    today.plusDays(1).format(dateFormatter),
                    city, country, method
                )
                
                val nextFajr = tomorrowResponse.data.timings.Fajr
                val json = gson.toJson(response.data.timings)
                
                dataStore.savePrayerTimes(json, nextFajr, todayStr)
                PrayerWidget().updateAll(context)
                
                // Logic for streaks could be added here or in SplashScreenViewModel
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
