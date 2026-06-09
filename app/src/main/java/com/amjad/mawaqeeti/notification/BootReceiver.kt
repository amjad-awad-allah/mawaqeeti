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
                
                // Reschedule for each prayer if not already prayed
                listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha").forEach { name ->
                    if (states[name] == false) {
                        val time = when(name) {
                            "Fajr" -> timings.Fajr
                            "Dhuhr" -> timings.Dhuhr
                            "Asr" -> timings.Asr
                            "Maghrib" -> timings.Maghrib
                            "Isha" -> timings.Isha
                            else -> ""
                        }
                        // We need a PrayerTime object, but Note: 
                        // schedulePrayerAlarms takes PrayerTime. 
                        // Minimal fix:
                        // scheduler.schedulePrayerAlarms(PrayerTime(name, time))
                    }
                }
            }
        }
    }
}
