package com.calcnova.app.feature.programmer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.calcnova.app.api.ApiError
import com.calcnova.app.api.BaseConvertRequest
import com.calcnova.app.api.BaseConvertResponse
import com.calcnova.app.api.BitwiseRequest
import com.calcnova.app.api.BitwiseResponse
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

data class ProgrammerUiState(
    val inputA: String = "",
    val inputB: String = "",
    val fromBase: String = "DEC",
    val operation: String = "AND",
    val binary: String = "",
    val octal: String = "",
    val hex: String = "",
    val decimal: String = "",
    val bitRow: List<Boolean> = emptyList(),
    val bitwiseResult: String = "",
    val errorMessage: String? = null
)

val bases = listOf("BIN", "OCT", "DEC", "HEX")
val bitwiseOps = listOf("AND", "OR", "XOR", "NOT", "NAND", "NOR", "XNOR", "SHL", "SHR")
val singleOperandOps = setOf("NOT")

private const val DEBOUNCE_MS = 180L

class ProgrammerCalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val historyStore = JsonHistoryStore(application)
    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow(ProgrammerUiState())
    val uiState: StateFlow<ProgrammerUiState> = _uiState.asStateFlow()

    private var convertJob: Job? = null
    private var bitwiseJob: Job? = null

    fun onInputAChange(v: String) {
        _uiState.value = _uiState.value.copy(inputA = v, errorMessage = null)
        scheduleConvert()
        scheduleBitwise()
    }

    fun onInputBChange(v: String) {
        _uiState.value = _uiState.value.copy(inputB = v, errorMessage = null)
        scheduleBitwise()
    }

    fun onFromBaseChange(v: String) {
        _uiState.value = _uiState.value.copy(fromBase = v)
        scheduleConvert()
        scheduleBitwise()
    }

    fun onOperationChange(v: String) {
        _uiState.value = _uiState.value.copy(operation = v)
        scheduleBitwise()
    }

    private fun scheduleConvert() {
        convertJob?.cancel()
        val state = _uiState.value
        if (state.inputA.isBlank()) {
            _uiState.value = state.copy(binary = "", octal = "", hex = "", decimal = "", bitRow = emptyList())
            return
        }
        convertJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            val responseJson = withContext(Dispatchers.Default) {
                CalculatorApi.baseConvert(BaseConvertRequest(state.inputA, state.fromBase))
            }
            val success = runCatching { json.decodeFromString<BaseConvertResponse>(responseJson) }.getOrNull()
            if (success != null) {
                val decimalValue = success.decimal.toLongOrNull() ?: 0L
                _uiState.value = _uiState.value.copy(
                    binary = success.binary,
                    octal = success.octal,
                    hex = success.hex,
                    decimal = success.decimal,
                    bitRow = bitRowFor(decimalValue),
                    errorMessage = null
                )
                withContext(Dispatchers.IO) {
                    historyStore.append(
                        HistoryEntry(
                            calculatorType = "PROGRAMMER",
                            expression = "${state.inputA} ${state.fromBase} -> all bases",
                            result = "DEC=${success.decimal} BIN=${success.binary} HEX=${success.hex}",
                            timestampMillis = System.currentTimeMillis()
                        )
                    )
                }
            }
            // Silently ignore parse errors while the user is still typing.
        }
    }

    private fun scheduleBitwise() {
        bitwiseJob?.cancel()
        val state = _uiState.value
        if (state.inputA.isBlank()) return
        if (state.operation !in singleOperandOps && state.inputB.isBlank()) return

        bitwiseJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            val responseJson = withContext(Dispatchers.Default) {
                CalculatorApi.bitwise(
                    BitwiseRequest(
                        a = state.inputA,
                        b = if (state.operation in singleOperandOps) null else state.inputB,
                        fromBase = state.fromBase,
                        operation = state.operation
                    )
                )
            }
            val success = runCatching { json.decodeFromString<BitwiseResponse>(responseJson) }.getOrNull()
            if (success != null) {
                _uiState.value = _uiState.value.copy(
                    bitwiseResult = "DEC=${success.decimalResult}  BIN=${success.binaryResult}  HEX=${success.hexResult}",
                    errorMessage = null
                )
                withContext(Dispatchers.IO) {
                    historyStore.append(
                        HistoryEntry(
                            calculatorType = "PROGRAMMER",
                            expression = "${state.inputA} ${state.operation} ${state.inputB}",
                            result = success.decimalResult,
                            timestampMillis = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    private fun bitRowFor(decimalValue: Long): List<Boolean> {
        if (decimalValue < 0 || decimalValue > 255) return emptyList()
        return (7 downTo 0).map { bit -> ((decimalValue shr bit) and 1L) == 1L }
    }
}
