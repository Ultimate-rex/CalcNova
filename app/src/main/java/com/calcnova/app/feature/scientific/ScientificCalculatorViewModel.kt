package com.calcnova.app.feature.scientific

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.calcnova.app.api.AdvancedCalcRequest
import com.calcnova.app.api.ApiError
import com.calcnova.app.api.BasicCalcResponse
import com.calcnova.app.api.CalculatorApi
import com.calcnova.app.api.HistoryEntry
import com.calcnova.app.core.JsonHistoryStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

data class ScientificUiState(
    val expression: String = "",
    val result: String = "",
    val errorMessage: String? = null,
    val isLivePreview: Boolean = false,
    val angleMode: String = "DEG"
)

private const val LIVE_PREVIEW_DEBOUNCE_MS = 150L

class ScientificCalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val historyStore = JsonHistoryStore(application)
    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow(ScientificUiState())
    val uiState: StateFlow<ScientificUiState> = _uiState.asStateFlow()

    private var autoCalculateEnabled = true
    private var previewJob: Job? = null

    fun setAutoCalculate(enabled: Boolean) {
        autoCalculateEnabled = enabled
    }

    fun setDefaultAngleMode(mode: String) {
        _uiState.value = _uiState.value.copy(angleMode = mode)
    }

    fun onAngleModeChange(mode: String) {
        _uiState.value = _uiState.value.copy(angleMode = mode)
        if (autoCalculateEnabled) scheduleLivePreview()
    }

    /** Insert an arbitrary token (a digit, an operator, or a function opener like "sin("). */
    fun insertToken(token: String) {
        _uiState.value = _uiState.value.copy(
            expression = _uiState.value.expression + token,
            errorMessage = null
        )
        if (autoCalculateEnabled) scheduleLivePreview()
    }

    fun onClear() {
        previewJob?.cancel()
        _uiState.value = _uiState.value.copy(expression = "", result = "", errorMessage = null, isLivePreview = false)
    }

    fun onDelete() {
        val current = _uiState.value.expression
        if (current.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(expression = current.dropLast(1), errorMessage = null)
            if (autoCalculateEnabled) scheduleLivePreview() else _uiState.value = _uiState.value.copy(result = "")
        }
    }

    private fun scheduleLivePreview() {
        previewJob?.cancel()
        val state = _uiState.value
        if (state.expression.isBlank()) {
            _uiState.value = state.copy(result = "", isLivePreview = false)
            return
        }
        previewJob = viewModelScope.launch {
            delay(LIVE_PREVIEW_DEBOUNCE_MS)
            val responseJson = withContext(Dispatchers.Default) {
                CalculatorApi.advancedCalculate(AdvancedCalcRequest(state.expression, state.angleMode))
            }
            val success = runCatching { json.decodeFromString<BasicCalcResponse>(responseJson) }.getOrNull()
            if (success != null) {
                _uiState.value = _uiState.value.copy(result = success.result, isLivePreview = true, errorMessage = null)
            }
        }
    }

    fun onEquals() {
        previewJob?.cancel()
        val state = _uiState.value
        if (state.expression.isBlank()) return

        viewModelScope.launch {
            val responseJson = withContext(Dispatchers.Default) {
                CalculatorApi.advancedCalculate(AdvancedCalcRequest(state.expression, state.angleMode))
            }
            val success = runCatching { json.decodeFromString<BasicCalcResponse>(responseJson) }.getOrNull()
            if (success != null) {
                _uiState.value = _uiState.value.copy(result = success.result, errorMessage = null, isLivePreview = false)
                withContext(Dispatchers.IO) {
                    historyStore.append(
                        HistoryEntry(
                            calculatorType = "SCIENTIFIC",
                            expression = success.expression,
                            result = success.result,
                            timestampMillis = System.currentTimeMillis()
                        )
                    )
                }
            } else {
                val error = runCatching { json.decodeFromString<ApiError>(responseJson) }.getOrNull()
                _uiState.value = _uiState.value.copy(result = "", errorMessage = error?.message ?: "Invalid expression", isLivePreview = false)
            }
        }
    }
}
