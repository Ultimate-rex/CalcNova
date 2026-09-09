package com.calcnova.app.feature.programmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calcnova.app.feature.settings.SettingsViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgrammerCalculatorScreen(
    settingsViewModel: SettingsViewModel,
    viewModel: ProgrammerCalculatorViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Developer Calculator", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    bases.forEach { base ->
                        FilterChip(
                            selected = state.fromBase == base,
                            onClick = { viewModel.onFromBaseChange(base) },
                            label = { Text(base) }
                        )
                    }
                }

                OutlinedTextField(
                    value = state.inputA,
                    onValueChange = viewModel::onInputAChange,
                    label = { Text("Value (in ${state.fromBase})") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (state.decimal.isNotBlank()) {
                    ConversionRow("DEC", state.decimal)
                    ConversionRow("BIN", state.binary)
                    ConversionRow("OCT", state.octal)
                    ConversionRow("HEX", state.hex)
                }

                if (state.bitRow.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("8-bit view", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    BitRow(bits = state.bitRow)
                }
            }
        }

        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Bitwise Operation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    bitwiseOps.forEach { op ->
                        FilterChip(
                            selected = state.operation == op,
                            onClick = { viewModel.onOperationChange(op) },
                            label = { Text(op) }
                        )
                    }
                }

                if (state.operation !in singleOperandOps) {
                    OutlinedTextField(
                        value = state.inputB,
                        onValueChange = viewModel::onInputBChange,
                        label = { Text("Value B (in ${state.fromBase})") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (state.bitwiseResult.isNotBlank()) {
                    Text(
                        "Result: ${state.bitwiseResult}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (state.errorMessage != null) {
            Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ConversionRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BitRow(bits: List<Boolean>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        bits.forEachIndexed { _, isOn ->
            val bgColor = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val fgColor = if (isOn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .background(bgColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isOn) "1" else "0", color = fgColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}
