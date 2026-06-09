package com.amjad.mawaqeeti.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amjad.mawaqeeti.data.local.DataStoreManager
import com.amjad.mawaqeeti.data.model.PrayerTime
import com.amjad.mawaqeeti.data.model.Timings
import com.amjad.mawaqeeti.notification.NotificationScheduler
import com.amjad.mawaqeeti.util.PrayerCalculator
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import androidx.glance.appwidget.updateAll
import com.amjad.mawaqeeti.widget.PrayerWidget
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStore: DataStoreManager,
    private val scheduler: NotificationScheduler,
    private val gson: Gson,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<HomeEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        observeData()
        startTimer()
    }

    sealed class HomeEvent {
        data class ShowSnackbar(val message: String) : HomeEvent()
    }

    private fun observeData() {
        combine(
            dataStore.prayerTimesJson,
            dataStore.prayerStates,
            dataStore.nextDayFajrTime,
            dataStore.currentStreak,
            dataStore.bestStreak,
            dataStore.lavaPerformanceMode,
            dataStore.fajrOffset,
            dataStore.dhuhrOffset,
            dataStore.asrOffset,
            dataStore.maghribOffset,
            dataStore.ishaOffset
        ) { args ->
            val json = args[0] as String?
            val states = args[1] as Map<*, *>
            val nextFajr = args[2] as String?
            val currentStreak = args[3] as Int
            val bestStreak = args[4] as Int
            val lavaMode = args[5] as String
            
            val fOff = args[6] as Int
            val dOff = args[7] as Int
            val aOff = args[8] as Int
            val mOff = args[9] as Int
            val iOff = args[10] as Int

            if (json != null) {
                val timings: Timings = gson.fromJson(json, Timings::class.java)
                val prayerList = listOf(
                    PrayerTime("الفجر", PrayerCalculator.applyOffsets(timings.Fajr, fOff),
                        (states["Fajr"] ?: false) as Boolean
                    ),
                    PrayerTime("الظهر", PrayerCalculator.applyOffsets(timings.Dhuhr, dOff),
                        (states["Dhuhr"] ?: false) as Boolean
                    ),
                    PrayerTime("العصر", PrayerCalculator.applyOffsets(timings.Asr, aOff),
                        (states["Asr"] ?: false) as Boolean
                    ),
                    PrayerTime("المغرب", PrayerCalculator.applyOffsets(timings.Maghrib, mOff),
                        (states["Maghrib"] ?: false) as Boolean
                    ),
                    PrayerTime("العشاء", PrayerCalculator.applyOffsets(timings.Isha, iOff),
                        (states["Isha"] ?: false) as Boolean
                    )
                )
                
                val (next, isNextDay) = PrayerCalculator.findNextPrayer(prayerList, nextFajr)
                val progress = PrayerCalculator.getProgress(states)
                
                _uiState.update { 
                    it.copy(
                        prayers = prayerList,
                        nextPrayer = next,
                        isNextDay = isNextDay,
                        progressCount = progress,
                        streak = currentStreak,
                        bestStreak = bestStreak,
                        lavaMode = lavaMode
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                val state = _uiState.value
                state.nextPrayer?.let { next ->
                    val remaining = PrayerCalculator.calculateTimeRemaining(next.time, state.isNextDay)
                    _uiState.update { it.copy(timeLeft = remaining) }
                }
                delay(1000)
            }
        }
    }

    fun togglePrayer(prayerName: String, isPrayed: Boolean) {
        viewModelScope.launch {
            val englishName = when(prayerName) {
                "الفجر" -> "Fajr"
                "الظهر" -> "Dhuhr"
                "العصر" -> "Asr"
                "المغرب" -> "Maghrib"
                "العشاء" -> "Isha"
                else -> prayerName
            }
            dataStore.setPrayerPrayed(englishName, isPrayed)
            PrayerWidget().updateAll(context)
            if (isPrayed) {
                scheduler.cancelAlarmsForPrayer(englishName)
                _eventFlow.emit(HomeEvent.ShowSnackbar("🤲 تقبل الله منك صلاة $prayerName"))
            } else {
                // Reschedule if undone? For now just cancel if prayed.
                // We could reschedule here if needed.
            }
            
            // Check if all prayed
            val currentStates = uiState.value.prayers.associate { it.name to it.isPrayed }.toMutableMap()
            currentStates[prayerName] = isPrayed
            if (currentStates.values.all { it }) {
                _uiState.update { it.copy(showCompletionOverlay = true) }
            } else {
                _uiState.update { it.copy(showCompletionOverlay = false) }
            }
        }
    }

    fun dismissCompletionOverlay() {
        _uiState.update { it.copy(showCompletionOverlay = false) }
    }
}

data class HomeUiState(
    val prayers: List<PrayerTime> = emptyList(),
    val nextPrayer: PrayerTime? = null,
    val isNextDay: Boolean = false,
    val timeLeft: String = "00:00:00",
    val progressCount: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lavaMode: String = "BALANCED",
    val showCompletionOverlay: Boolean = false
)
