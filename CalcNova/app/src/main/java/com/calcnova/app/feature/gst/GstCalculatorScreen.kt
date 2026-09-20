package com.calcnova.app.feature.gst

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GstCalculatorScreen(viewModel: GstCalculatorViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("GST Calculator", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.mode == "ADD",
                        onClick = { viewModel.onModeChange("ADD") },
                        label = { Text("Add GST") }
                    )
                    FilterChip(
                        selected = state.mode == "REMOVE",
                        onClick = { viewModel.onModeChange("REMOVE") },
                        label = { Text("Remove GST") }
                    )
                }

                OutlinedTextField(
                    value = state.amountText,
                    onValueChange = viewModel::onAmountChange,
                    label = { Text(if (state.mode == "ADD") "Base amount (₹)" else "Final amount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("GST rate", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    gstPresets.forEach { rate ->
                        FilterChip(
                            selected = state.gstPercent == rate,
                            onClick = { viewModel.onGstPercentChange(rate) },
                            label = { Text("${rate.toInt()}%") }
                        )
                    }
                }
            }
        }

        if (state.errorMessage != null) {
            Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
        } else if (state.totalAmount.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GstRow("Taxable value", "₹${state.baseAmount}")
                    GstRow("CGST", "₹${state.cgst}")
                    GstRow("SGST", "₹${state.sgst}")
                    GstRow("Total GST", "₹${state.totalGst}")
                    HorizontalDivider()
                    GstRow("Grand total", "₹${state.totalAmount}", emphasize = true)
                }
            }
        }
    }
}

@Composable
private fun GstRow(label: String, value: String, emphasize: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            value,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.SemiBold,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
