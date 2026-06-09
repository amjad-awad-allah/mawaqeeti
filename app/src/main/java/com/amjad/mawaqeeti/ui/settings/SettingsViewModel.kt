package com.amjad.mawaqeeti.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amjad.mawaqeeti.data.local.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStoreManager
) : ViewModel() {

    val selectedCity = dataStore.selectedCity.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "Gaza")
    val selectedCountry = dataStore.selectedCountry.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "Palestine")
    val selectedMethod = dataStore.selectedMethod.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 3)
    val lavaMode = dataStore.lavaPerformanceMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "BALANCED")

    val fajrOffset = dataStore.fajrOffset.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    val dhuhrOffset = dataStore.dhuhrOffset.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    val asrOffset = dataStore.asrOffset.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    val maghribOffset = dataStore.maghribOffset.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    val ishaOffset = dataStore.ishaOffset.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    fun updateLocation(city: String, country: String, method: Int) {
        viewModelScope.launch {
            dataStore.updateLocation(city, country, method)
        }
    }

    fun setLavaMode(mode: String) {
        viewModelScope.launch {
            dataStore.setLavaMode(mode)
        }
    }

    fun updateOffset(prayer: String, value: Int) {
        viewModelScope.launch {
            when (prayer) {
                "Fajr" -> dataStore.setOffset("Fajr", value)
                "Dhuhr" -> dataStore.setOffset("Dhuhr", value)
                "Asr" -> dataStore.setOffset("Asr", value)
                "Maghrib" -> dataStore.setOffset("Maghrib", value)
                "Isha" -> dataStore.setOffset("Isha", value)
            }
        }
    }
}
