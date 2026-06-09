package com.amjad.mawaqeeti.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.action.*
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.amjad.mawaqeeti.data.local.DataStoreManager
import com.amjad.mawaqeeti.data.model.Timings
import com.amjad.mawaqeeti.data.model.PrayerTime
import com.amjad.mawaqeeti.util.PrayerCalculator
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.util.Locale

class PrayerWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent(context)
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val prefs = currentState<Preferences>()
        val prayerTimesJson = prefs[stringPreferencesKey("prayer_times_json")]
        val prayerStatesJson = prefs[stringPreferencesKey("prayer_states")] // Simplified for widget
        
        var nextPrayerName = "---"
        var nextPrayerTime = "--:--"
        
        if (prayerTimesJson != null) {
            val gson = Gson()
            val timings = gson.fromJson(prayerTimesJson, Timings::class.java)
            // Note: In a full implementation we'd parse states too, but let's first get times working
            val prayerList = listOf(
                PrayerTime("الفجر", timings.Fajr, false),
                PrayerTime("الظهر", timings.Dhuhr, false),
                PrayerTime("العصر", timings.Asr, false),
                PrayerTime("المغرب", timings.Maghrib, false),
                PrayerTime("العشاء", timings.Isha, false)
            )
            val (next, _) = PrayerCalculator.findNextPrayer(prayerList, null)
            next?.let {
                nextPrayerName = it.name
                nextPrayerTime = it.time
            }
        }

        // Premium Palette
        val deepBg = ComposeColor(0xFF0A0E14)
        val liquidAccent = ComposeColor(0xFF64FFDA)
        val secondaryAccent = ComposeColor(0xFF2196F3)
        val glassWhite = ComposeColor.White.copy(alpha = 0.15f)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(deepBg)
                .cornerRadius(32.dp)
        ) {
            Box(
                modifier = GlanceModifier
                    .size(150.dp)
                    .background(secondaryAccent.copy(alpha = 0.2f))
                    .cornerRadius(75.dp)
            ) {}

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مواقيتي",
                        style = TextStyle(
                            color = ColorProvider(liquidAccent),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = "• القادمة",
                        style = TextStyle(
                            color = ColorProvider(ComposeColor.White.copy(alpha = 0.5f)),
                            fontSize = 10.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(12.dp))

                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(glassWhite)
                        .padding(12.dp)
                        .cornerRadius(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = nextPrayerName,
                        style = TextStyle(
                            color = ColorProvider(ComposeColor.White),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = nextPrayerTime,
                        style = TextStyle(
                            color = ColorProvider(liquidAccent),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(16.dp))

                Button(
                    text = "تمت الصلاة ✓",
                    onClick = actionRunCallback<PrayedActionCallback>(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ColorProvider(liquidAccent),
                        contentColor = ColorProvider(deepBg)
                    ),
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .cornerRadius(16.dp)
                )
            }
        }
    }
}

class PrayedActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val dataStore = DataStoreManager(context)
        val json = dataStore.prayerTimesJson.first()
        if (json != null) {
            val gson = Gson()
            val timings = gson.fromJson(json, Timings::class.java)
            val states = dataStore.prayerStates.first()
            val prayerList = listOf(
                PrayerTime("Fajr", timings.Fajr, states["Fajr"] ?: false),
                PrayerTime("Dhuhr", timings.Dhuhr, states["Dhuhr"] ?: false),
                PrayerTime("Asr", timings.Asr, states["Asr"] ?: false),
                PrayerTime("Maghrib", timings.Maghrib, states["Maghrib"] ?: false),
                PrayerTime("Isha", timings.Isha, states["Isha"] ?: false)
            )
            
            val (next, _) = PrayerCalculator.findNextPrayer(prayerList, null)
            next?.let {
                dataStore.setPrayerPrayed(it.name, true)
                PrayerWidget().update(context, glanceId)
            }
        }
    }
}
