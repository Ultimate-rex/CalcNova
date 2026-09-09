package com.calcnova.app.feature.basic

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

data class BasicCalcUiState(
    val expression: String = "",
    val result: String = "",
    val errorMessage: String? = null,
    val isLivePreview: Boolean = false
)

private const val LIVE_PREVIEW_DEBOUNCE_MS = 150L

class BasicCalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val historyStore = JsonHistoryStore(application)
    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow(BasicCalcUiState())
    val uiState: StateFlow<BasicCalcUiState> = _uiState.asStateFlow()

    private var autoCalculateEnabled = true
    private var previewJob: Job? = null

    fun setAutoCalculate(enabled: Boolean) {
        autoCalculateEnabled = enabled
    }

    fun onDigit(symbol: String) {
        _uiState.value = _uiState.value.copy(
            expression = _uiState.value.expression + symbol,
            errorMessage = null
        )
        if (autoCalculateEnabled) scheduleLivePreview()
    }

    fun onClear() {
        previewJob?.cancel()
        _uiState.value = BasicCalcUiState(isLivePreview = false)
    }

    fun onDelete() {
        val current = _uiState.value.expression
        if (current.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(expression = current.dropLast(1), errorMessage = null)
            if (autoCalculateEnabled) scheduleLivePreview() else _uiState.value = _uiState.value.copy(result = "")
        }
    }

    /** Debounced silent evaluation while the user is still typing - never shows errors. */
    private fun scheduleLivePreview() {
        previewJob?.cancel()
        val expression = _uiState.value.expression
        if (expression.isBlank()) {
            _uiState.value = _uiState.value.copy(result = "", isLivePreview = false)
            return
        }
        previewJob = viewModelScope.launch {
            delay(LIVE_PREVIEW_DEBOUNCE_MS)
            val responseJson = withContext(Dispatchers.Default) {
                CalculatorApi.advancedCalculate(AdvancedCalcRequest(expression))
            }
            val success = runCatching { json.decodeFromString<BasicCalcResponse>(responseJson) }.getOrNull()
            if (success != null) {
                _uiState.value = _uiState.value.copy(result = success.result, isLivePreview = true, errorMessage = null)
            }
            // Silently ignore errors here - the expression is probably just incomplete mid-typing.
        }
    }

    fun onEquals() {
        previewJob?.cancel()
        val expression = _uiState.value.expression
        if (expression.isBlank()) return

        viewModelScope.launch {
            val responseJson = withContext(Dispatchers.Default) {
                CalculatorApi.advancedCalculate(AdvancedCalcRequest(expression))
            }

            val success = runCatching { json.decodeFromString<BasicCalcResponse>(responseJson) }.getOrNull()
            if (success != null) {
                _uiState.value = _uiState.value.copy(result = success.result, errorMessage = null, isLivePreview = false)
                withContext(Dispatchers.IO) {
                    historyStore.append(
                        HistoryEntry(
                            calculatorType = "BASIC",
                            expression = success.expression,
                            result = success.result,
                            timestampMillis = System.currentTimeMillis()
                        )
                    )
                }
            } else {
                val error = runCatching { json.decodeFromString<ApiError>(responseJson) }.getOrNull()
                _uiState.value = _uiState.value.copy(
                    result = "",
                    errorMessage = error?.message ?: "Invalid expression",
                    isLivePreview = false
                )
            }
        }
    }
}
