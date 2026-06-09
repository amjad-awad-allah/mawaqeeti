package com.amjad.mawaqeeti.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amjad.mawaqeeti.data.local.DataStoreManager
import com.amjad.mawaqeeti.data.repository.PrayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: PrayerRepository,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            // 1. Refresh times if needed
            repository.refreshPrayerTimesIfNecessary()
            
            // 2. Logic for Streak
            updateStreakLogic()
            
            // 3. Mark as ready
            _isReady.value = true
        }
    }

    private suspend fun updateStreakLogic() {
        val today = LocalDate.now()
        val lastCompletedDayStr = dataStore.lastCompletedDay.first()
        
        if (lastCompletedDayStr != null) {
            val lastCompletedDay = LocalDate.parse(lastCompletedDayStr)
            val daysBetween = ChronoUnit.DAYS.between(lastCompletedDay, today)
            
            if (daysBetween > 1) {
                // Streak broken
                val best = dataStore.bestStreak.first()
                dataStore.updateStreak(0, best, lastCompletedDayStr)
            }
        }
    }
}
