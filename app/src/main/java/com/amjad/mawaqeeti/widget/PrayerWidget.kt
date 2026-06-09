package com.amjad.mawaqeeti.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.compose.ui.graphics.Color as ComposeColor
import com.amjad.mawaqeeti.data.local.DataStoreManager
import com.amjad.mawaqeeti.data.model.Timings
import com.amjad.mawaqeeti.data.model.PrayerTime
import com.amjad.mawaqeeti.util.PrayerCalculator
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.time.LocalTime

class PrayerWidget : GlanceAppWidget() {
    companion object {
        val IS_LOADING = booleanPreferencesKey("is_loading")
        val IS_PRAYING_PENDING = booleanPreferencesKey("is_praying_pending")
    }

    override val stateDefinition: GlanceStateDefinition<Preferences> = PreferencesGlanceStateDefinition
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = DataStoreManager(context)
        
        // Fetch data here in the suspend scope, NOT inside provideContent
        val prayerTimesJson = dataStore.prayerTimesJson.first() ?: ""
        val prayerStates = dataStore.prayerStates.first()
        val nextDayFajr = dataStore.nextDayFajrTime.first()
        
        val fOff = dataStore.fajrOffset.first()
        val dOff = dataStore.dhuhrOffset.first()
        val aOff = dataStore.asrOffset.first()
        val mOff = dataStore.maghribOffset.first()
        val iOff = dataStore.ishaOffset.first()
        
        provideContent {
            val prefs = currentState<Preferences>()
            val isLoading = prefs[IS_LOADING] ?: false
            val isPending = prefs[IS_PRAYING_PENDING] ?: false
            
            WidgetContent(
                context, 
                prayerTimesJson, prayerStates, nextDayFajr, 
                fOff, dOff, aOff, mOff, iOff, 
                isLoading, isPending
            )
        }
    }

    @Composable
    private fun WidgetContent(
        context: Context, 
        prayerTimesJson: String, 
        prayerStates: Map<String, Boolean>,
        nextDayFajr: String?,
        fOff: Int, dOff: Int, aOff: Int, mOff: Int, iOff: Int,
        isLoading: Boolean,
        isPending: Boolean
    ) {
        var nextPrayer: PrayerTime? = null
        var timeRemaining = "--:--"
        var isPrayed = false
        
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
                .clickable(actionRunCallback<RefreshActionCallback>())
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
                    if (isPrayed) {
                        Text(
                            text = "تقبل الله منك 🤲",
                            style = TextStyle(color = ColorProvider(successGreen), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        )
                    } else if (isLoading) {
                        Text(
                            text = "جاري التحديث...",
                            style = TextStyle(color = ColorProvider(liquidAccent.copy(alpha = 0.7f)), fontSize = 11.sp)
                        )
                    } else {
                        Text(
                            text = nextPrayer?.name ?: "---",
                            style = TextStyle(color = ColorProvider(liquidAccent), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = GlanceModifier.defaultWeight())
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(if (isPrayed) successGreen.copy(alpha = 0.15f) else glassWhite)
                        .padding(8.dp)
                        .cornerRadius(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isPrayed) "أتممت الصلاة" else timeRemaining,
                        style = TextStyle(
                            color = ColorProvider(if (isPrayed) successGreen else ComposeColor.White),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (!isPrayed) {
                        Text(
                            text = "دقيقة : ساعة", 
                            style = TextStyle(color = ColorProvider(ComposeColor.White.copy(alpha = 0.4f)), fontSize = 10.sp)
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(10.dp))

                Button(
                    text = when {
                        isPending -> "جاري المعالجة..."
                        isPrayed -> "إلغاء التمام"
                        else -> "قمت بالصلاة"
                    },
                    onClick = actionRunCallback<PrayedActionCallback>(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ColorProvider(if (isPrayed || isPending) ComposeColor.White.copy(alpha = 0.1f) else liquidAccent),
                        contentColor = ColorProvider(if (isPrayed || isPending) ComposeColor.White else deepBg)
                    ),
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .cornerRadius(12.dp)
                )
            }
        }
    }
}

class RefreshActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[PrayerWidget.IS_LOADING] = true
        }
        PrayerWidget().update(context, glanceId)
        
        delay(500)

        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[PrayerWidget.IS_LOADING] = false
        }
        PrayerWidget().update(context, glanceId)
    }
}

class PrayedActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[PrayerWidget.IS_PRAYING_PENDING] = true
        }
        PrayerWidget().update(context, glanceId)

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
                
                dataStore.setPrayerPrayed(englishName, !it.isPrayed)
                
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[PrayerWidget.IS_PRAYING_PENDING] = false
                }
                
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(PrayerWidget::class.java)
                glanceIds.forEach { id ->
                    PrayerWidget().update(context, id)
                }
            }
        } else {
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[PrayerWidget.IS_PRAYING_PENDING] = false
            }
            PrayerWidget().update(context, glanceId)
        }
    }
}
