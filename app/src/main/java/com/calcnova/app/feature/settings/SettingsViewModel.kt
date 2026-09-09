package com.calcnova.app.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.calcnova.app.api.SettingsData
import com.calcnova.app.core.JsonSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val store = JsonSettingsStore(application)

    private val _settings = MutableStateFlow(store.load())
    val settings: StateFlow<SettingsData> = _settings.asStateFlow()

    private fun update(transform: (SettingsData) -> SettingsData) {
        val updated = transform(_settings.value)
        _settings.value = updated
        viewModelScope.launch(Dispatchers.IO) { store.save(updated) }
    }

    fun setThemeMode(mode: String) = update { it.copy(themeMode = mode) }
    fun setAutoCalculate(enabled: Boolean) = update { it.copy(autoCalculate = enabled) }
    fun setHapticFeedback(enabled: Boolean) = update { it.copy(hapticFeedback = enabled) }
    fun setDefaultAngleMode(mode: String) = update { it.copy(defaultAngleMode = mode) }
}
