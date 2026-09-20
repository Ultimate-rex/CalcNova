package com.calcnova.app.feature.gst

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.calcnova.app.api.ApiError
import com.calcnova.app.api.CalculatorApi
import com.calcnova.app.api.GstCalcRequest
import com.calcnova.app.api.GstCalcResponse
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

val gstPresets = listOf(0.0, 5.0, 12.0, 18.0, 28.0)

data class GstUiState(
    val amountText: String = "",
    val gstPercent: Double = 18.0,
    val mode: String = "ADD", // ADD or REMOVE
    val baseAmount: String = "",
    val cgst: String = "",
    val sgst: String = "",
    val totalGst: String = "",
    val totalAmount: String = "",
    val errorMessage: String? = null
)

private const val DEBOUNCE_MS = 150L

class GstCalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val historyStore = JsonHistoryStore(application)
    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow(GstUiState())
    val uiState: StateFlow<GstUiState> = _uiState.asStateFlow()

    private var job: Job? = null

    fun onAmountChange(text: String) {
        _uiState.value = _uiState.value.copy(amountText = text, errorMessage = null)
        schedule()
    }

    fun onGstPercentChange(percent: Double) {
        _uiState.value = _uiState.value.copy(gstPercent = percent)
        schedule()
    }

    fun onModeChange(mode: String) {
        _uiState.value = _uiState.value.copy(mode = mode)
        schedule()
    }

    private fun schedule() {
        job?.cancel()
        val state = _uiState.value
        val amount = state.amountText.toDoubleOrNull()
        if (amount == null) {
            _uiState.value = state.copy(baseAmount = "", cgst = "", sgst = "", totalGst = "", totalAmount = "")
            return
        }
        job = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            val responseJson = withContext(Dispatchers.Default) {
                CalculatorApi.gstCalculate(GstCalcRequest(amount, state.gstPercent, state.mode))
            }
            val success = runCatching { json.decodeFromString<GstCalcResponse>(responseJson) }.getOrNull()
            if (success != null) {
                _uiState.value = _uiState.value.copy(
                    baseAmount = success.baseAmount,
                    cgst = success.cgst,
                    sgst = success.sgst,
                    totalGst = success.totalGst,
                    totalAmount = success.totalAmount,
                    errorMessage = null
                )
                withContext(Dispatchers.IO) {
                    historyStore.append(
                        HistoryEntry(
                            calculatorType = "GST",
                            expression = "${state.amountText} @ ${state.gstPercent}% (${state.mode})",
                            result = "Total = ${success.totalAmount}",
                            timestampMillis = System.currentTimeMillis()
                        )
                    )
                }
            } else {
                val error = runCatching { json.decodeFromString<ApiError>(responseJson) }.getOrNull()
                _uiState.value = _uiState.value.copy(errorMessage = error?.message ?: "Invalid input")
            }
        }
    }
}
