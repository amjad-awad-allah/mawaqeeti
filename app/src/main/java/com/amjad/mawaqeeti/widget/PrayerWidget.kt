package com.amjad.mawaqeeti.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.datastore.preferences.core.Preferences
import com.amjad.mawaqeeti.data.local.DataStoreManager
import com.amjad.mawaqeeti.data.model.Timings
import com.amjad.mawaqeeti.data.model.PrayerTime
import com.amjad.mawaqeeti.util.PrayerCalculator
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import java.time.LocalTime

class PrayerWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<Preferences> = PreferencesGlanceStateDefinition
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = DataStoreManager(context)
        val prayerTimesJson = dataStore.prayerTimesJson.first() ?: ""
        val prayerStates = dataStore.prayerStates.first()
        val nextDayFajr = dataStore.nextDayFajrTime.first()
        
        // Fetch offsets to stay in sync with the app
        val fOff = dataStore.fajrOffset.first()
        val dOff = dataStore.dhuhrOffset.first()
        val aOff = dataStore.asrOffset.first()
        val mOff = dataStore.maghribOffset.first()
        val iOff = dataStore.ishaOffset.first()
        
        provideContent {
            WidgetContent(context, prayerTimesJson, prayerStates, nextDayFajr, fOff, dOff, aOff, mOff, iOff)
        }
    }

    @Composable
    private fun WidgetContent(
        context: Context, 
        prayerTimesJson: String, 
        prayerStates: Map<String, Boolean>,
        nextDayFajr: String?,
        fOff: Int, dOff: Int, aOff: Int, mOff: Int, iOff: Int
    ) {
        var nextPrayer: PrayerTime? = null
        var timeRemaining = "--:--"
        var isPrayed = false
        var nextPrayerDayInd = false
        
        if (prayerTimesJson.isNotEmpty()) {
            try {
                val gson = Gson()
                val timings = gson.fromJson(prayerTimesJson, Timings::class.java)
                val prayerList = listOf(
                    PrayerTime("الفجر", PrayerCalculator.applyOffsets(timings.Fajr, fOff), (prayerStates["Fajr"] ?: false)),
                    PrayerTime("الظهر", PrayerCalculator.applyOffsets(timings.Dhuhr, dOff), (prayerStates["Dhuhr"] ?: false)),
                    PrayerTime("العصر", PrayerCalculator.applyOffsets(timings.Asr, aOff), (prayerStates["Asr"] ?: false)),
                    PrayerTime("المغرب", PrayerCalculator.applyOffsets(timings.Maghrib, mOff), (prayerStates["Maghrib"] ?: false)),
                    PrayerTime("العشاء", PrayerCalculator.applyOffsets(timings.Isha, iOff), (prayerStates["Isha"] ?: false))
                )
                val (next, ind) = PrayerCalculator.findNextPrayer(prayerList, nextDayFajr)
                nextPrayer = next
                isPrayed = next?.isPrayed ?: false
                nextPrayerDayInd = ind
                
                next?.let {
                    val remainingFull = PrayerCalculator.calculateTimeRemaining(it.time, ind)
                    timeRemaining = remainingFull.substring(0, 5) // HH:mm
                }
            } catch (e: Exception) { }
        }

        val deepBg = ComposeColor(0xFF0A0E14)
        val liquidAccent = ComposeColor(0xFF64FFDA)
        val successGreen = ComposeColor(0xFF4CAF50)
        val glassWhite = ComposeColor.White.copy(alpha = 0.1f)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(deepBg)
                .cornerRadius(24.dp)
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مواقيتي",
                        style = TextStyle(color = ColorProvider(liquidAccent), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    if (isPrayed) {
                        Text(
                            text = "تقبل الله منك 🤲",
                            style = TextStyle(color = ColorProvider(successGreen), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        )
                    } else {
                        Text(
                            text = "باقي على ${nextPrayer?.name ?: "---"}",
                            style = TextStyle(color = ColorProvider(ComposeColor.White.copy(alpha = 0.6f)), fontSize = 10.sp)
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(glassWhite)
                        .padding(8.dp)
                        .cornerRadius(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isPrayed) "أتممت الصلاة" else timeRemaining,
                        style = TextStyle(
                            color = ColorProvider(if (isPrayed) successGreen else ComposeColor.White),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (!isPrayed) {
                        Text(
                            text = "ساعات : دقائق",
                            style = TextStyle(color = ColorProvider(ComposeColor.White.copy(alpha = 0.4f)), fontSize = 9.sp)
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(12.dp))

                Button(
                    text = if (isPrayed) "تمت بنجاح ✓" else "قمت بالصلاة",
                    onClick = actionRunCallback<PrayedActionCallback>(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ColorProvider(if (isPrayed) successGreen.copy(alpha = 0.2f) else liquidAccent),
                        contentColor = ColorProvider(if (isPrayed) successGreen else deepBg)
                    ),
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .cornerRadius(12.dp)
                )
            }
        }
    }
}

class PrayedActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val dataStore = DataStoreManager(context)
        
        val json = dataStore.prayerTimesJson.first()
        val fOff = dataStore.fajrOffset.first()
        val dOff = dataStore.dhuhrOffset.first()
        val aOff = dataStore.asrOffset.first()
        val mOff = dataStore.maghribOffset.first()
        val iOff = dataStore.ishaOffset.first()
        val states = dataStore.prayerStates.first()
        val nextDayFajr = dataStore.nextDayFajrTime.first()

        if (json != null) {
            val gson = Gson()
            val timings = gson.fromJson(json, Timings::class.java)
            val prayerList = listOf(
                PrayerTime("الفجر", PrayerCalculator.applyOffsets(timings.Fajr, fOff), (states["Fajr"] ?: false)),
                PrayerTime("الظهر", PrayerCalculator.applyOffsets(timings.Dhuhr, dOff), (states["Dhuhr"] ?: false)),
                PrayerTime("العصر", PrayerCalculator.applyOffsets(timings.Asr, aOff), (states["Asr"] ?: false)),
                PrayerTime("المغرب", PrayerCalculator.applyOffsets(timings.Maghrib, mOff), (states["Maghrib"] ?: false)),
                PrayerTime("العشاء", PrayerCalculator.applyOffsets(timings.Isha, iOff), (states["Isha"] ?: false))
            )
            
            val (next, _) = PrayerCalculator.findNextPrayer(prayerList, nextDayFajr)
            next?.let {
                val englishName = when(it.name) {
                    "الفجر" -> "Fajr"
                    "الظهر" -> "Dhuhr"
                    "العصر" -> "Asr"
                    "المغرب" -> "Maghrib"
                    "العشاء" -> "Isha"
                    else -> it.name
                }
                // TOGGLE state
                dataStore.setPrayerPrayed(englishName, !it.isPrayed)
                PrayerWidget().update(context, glanceId)
            }
        }
    }
}
