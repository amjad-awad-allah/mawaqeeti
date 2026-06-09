package com.amjad.mawaqeeti.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.amjad.mawaqeeti.data.local.DataStoreManager
import com.amjad.mawaqeeti.data.model.Timings
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var dataStore: DataStoreManager
    @Inject lateinit var scheduler: NotificationScheduler
    @Inject lateinit var gson: Gson

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val json = dataStore.prayerTimesJson.first() ?: return@launch
                val timings = gson.fromJson(json, Timings::class.java)
                val states = dataStore.prayerStates.first()
                
                val fOff = dataStore.fajrOffset.first()
                val dOff = dataStore.dhuhrOffset.first()
                val aOff = dataStore.asrOffset.first()
                val mOff = dataStore.maghribOffset.first()
                val iOff = dataStore.ishaOffset.first()

                val prayerList = listOf(
                    com.amjad.mawaqeeti.data.model.PrayerTime("الفجر", com.amjad.mawaqeeti.util.PrayerCalculator.applyOffsets(timings.Fajr, fOff), (states["Fajr"] ?: false)),
                    com.amjad.mawaqeeti.data.model.PrayerTime("الظهر", com.amjad.mawaqeeti.util.PrayerCalculator.applyOffsets(timings.Dhuhr, dOff), (states["Dhuhr"] ?: false)),
                    com.amjad.mawaqeeti.data.model.PrayerTime("العصر", com.amjad.mawaqeeti.util.PrayerCalculator.applyOffsets(timings.Asr, aOff), (states["Asr"] ?: false)),
                    com.amjad.mawaqeeti.data.model.PrayerTime("المغرب", com.amjad.mawaqeeti.util.PrayerCalculator.applyOffsets(timings.Maghrib, mOff), (states["Maghrib"] ?: false)),
                    com.amjad.mawaqeeti.data.model.PrayerTime("العشاء", com.amjad.mawaqeeti.util.PrayerCalculator.applyOffsets(timings.Isha, iOff), (states["Isha"] ?: false))
                )
                
                scheduler.scheduleAlarmsForPrayers(prayerList)
            }
        }
    }
}
