package com.amjad.mawaqeeti.widget

import android.content.Context
import android.widget.RemoteViews
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.amjad.mawaqeeti.R
import com.amjad.mawaqeeti.data.local.DataStoreManager
import com.amjad.mawaqeeti.data.model.Timings
import com.amjad.mawaqeeti.data.model.PrayerTime
import com.amjad.mawaqeeti.util.PrayerCalculator
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.Duration

class PrayerWidget : GlanceAppWidget() {
    companion object {
        val IS_LOADING = booleanPreferencesKey("is_loading")
        val IS_P_PENDING = booleanPreferencesKey("is_praying_pending")
    }

    override val stateDefinition: GlanceStateDefinition<Preferences> = PreferencesGlanceStateDefinition
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStore = DataStoreManager(context)
        provideContent {
            val prayerTimesJson = dataStore.prayerTimesJson.collectAsState(initial = "").value ?: ""
            val prayerStates = dataStore.prayerStates.collectAsState(initial = emptyMap()).value
            val nextDayFajr = dataStore.nextDayFajrTime.collectAsState(initial = null).value
            
            val prefs = currentState<Preferences>()
            val isLoading = prefs[IS_LOADING] ?: false
            val isPending = prefs[IS_P_PENDING] ?: false
            
            WidgetContent(
                context, 
                prayerTimesJson, prayerStates, nextDayFajr, 
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
        isLoading: Boolean,
        isPending: Boolean
    ) {
        var nextPrayer: PrayerTime? = null
        var activePrayer: PrayerTime? = null
        var targetTimeMillis: Long = 0
        
        if (prayerTimesJson.isNotEmpty()) {
            try {
                val gson = Gson()
                val timings = gson.fromJson(prayerTimesJson, Timings::class.java)
                val prayerList = listOf(
                    PrayerTime("الفجر", timings.Fajr, (prayerStates["Fajr"] ?: false)),
                    PrayerTime("الظهر", timings.Dhuhr, (prayerStates["Dhuhr"] ?: false)),
                    PrayerTime("العصر", timings.Asr, (prayerStates["Asr"] ?: false)),
                    PrayerTime("المغرب", timings.Maghrib, (prayerStates["Maghrib"] ?: false)),
                    PrayerTime("العشاء", timings.Isha, (prayerStates["Isha"] ?: false))
                )
                val result = PrayerCalculator.findNextPrayer(prayerList, nextDayFajr)
                val next = result.first
                val isNextDay = result.second
                
                nextPrayer = next
                activePrayer = PrayerCalculator.findActivePrayer(prayerList)
                
                val prayerTime = LocalTime.parse(next.time, DateTimeFormatter.ofPattern("HH:mm"))
                    var nextPrayerDateTime = java.time.LocalDateTime.now()
                        .withHour(prayerTime.hour)
                        .withMinute(prayerTime.minute)
                        .withSecond(0)
                    
                    if (isNextDay) { // Tomorrow
                        nextPrayerDateTime = nextPrayerDateTime.plusDays(1)
                    }
                    
                    val duration = Duration.between(java.time.LocalDateTime.now(), nextPrayerDateTime)
                    targetTimeMillis = SystemClock.elapsedRealtime() + duration.toMillis()
                } catch (e: Exception) { }
        }

        val isPrayed = activePrayer?.isPrayed ?: false
        val canShowButton = activePrayer != null && !isPrayed

        // UI Config
        val white = ComposeColor.White
        val auraGreen = ComposeColor(0xFF64FFDA)
        val glassBg = ComposeColor.Black.copy(alpha = 0.5f)
        
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(32.dp)
                .background(ImageProvider(R.drawable.bg))
                .clickable(actionRunCallback<RefreshActionCallback>())
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(glassBg)
                    .padding(12.dp), // Reduced from 16.dp
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = nextPrayer?.name ?: "---",
                    style = TextStyle(color = ColorProvider(auraGreen), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = GlanceModifier.height(4.dp)) // Reduced from 12.dp

                // LIVE CHRONOMETER (The Countdown)
                if (targetTimeMillis > SystemClock.elapsedRealtime()) {
                    Box(modifier = GlanceModifier.wrapContentSize()) {
                        AndroidRemoteViews(
                            remoteViews = RemoteViews(context.packageName, R.layout.widget_countdown_layout).apply {
                                setChronometer(R.id.chronometer, targetTimeMillis, null, true)
                            },
                            containerViewId = R.id.countdown_container,
                            content = {}
                        )
                    }
                } else {
                    Text(
                        text = "00:00:00",
                        style = TextStyle(color = ColorProvider(white), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp)) // Reduced from 16.dp

                // Done Button
                if (canShowButton) {
                    Button(
                        text = if (isPending) "جاري..." else "أديت الصلاة ✅",
                        onClick = actionRunCallback<PrayedActionCallback>(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = ColorProvider(auraGreen),
                            contentColor = ColorProvider(ComposeColor(0xFF0D1B2A))
                        ),
                        modifier = GlanceModifier.height(50.dp).fillMaxWidth().cornerRadius(16.dp)
                    )
                } else if (isPrayed) {
                    Text(
                        text = "تقبل الله منك 🤲",
                        style = TextStyle(color = ColorProvider(auraGreen), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    )
                }

                if (isLoading) {
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    CircularProgressIndicator(modifier = GlanceModifier.size(14.dp), color = ColorProvider(white))
                }
            }
        }
    }
}

class RefreshActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, glanceId) { it[PrayerWidget.IS_LOADING] = true }
        PrayerWidget().update(context, glanceId)
        delay(600)
        updateAppWidgetState(context, glanceId) { it[PrayerWidget.IS_LOADING] = false }
        PrayerWidget().update(context, glanceId)
    }
}

class PrayedActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val dataStore = DataStoreManager(context)
        updateAppWidgetState(context, glanceId) { it[PrayerWidget.IS_P_PENDING] = true }
        PrayerWidget().update(context, glanceId)
        val json = dataStore.prayerTimesJson.first()
        if (json != null) {
            val timings = Gson().fromJson(json, Timings::class.java)
            val states = dataStore.prayerStates.first()
            val prayerList = listOf(
                PrayerTime("الفجر", timings.Fajr, (states["Fajr"] ?: false)),
                PrayerTime("الظهر", timings.Dhuhr, (states["Dhuhr"] ?: false)),
                PrayerTime("العصر", timings.Asr, (states["Asr"] ?: false)),
                PrayerTime("المغرب", timings.Maghrib, (states["Maghrib"] ?: false)),
                PrayerTime("العشاء", timings.Isha, (states["Isha"] ?: false))
            )
            PrayerCalculator.findActivePrayer(prayerList)?.let { active ->
                val englishName = when(active.name) {
                    "الفجر" -> "Fajr" "الظهر" -> "Dhuhr" "العصر" -> "Asr" "المغرب" -> "Maghrib" "العشاء" -> "Isha" else -> active.name
                }
                dataStore.setPrayerPrayed(englishName, true)
            }
        }
        updateAppWidgetState(context, glanceId) { it[PrayerWidget.IS_P_PENDING] = false }
        val manager = GlanceAppWidgetManager(context)
        manager.getGlanceIds(PrayerWidget::class.java).forEach { PrayerWidget().update(context, it) }
    }
}
