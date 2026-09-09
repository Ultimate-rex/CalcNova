package com.calcnova.app.feature.scientific

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text as Material3Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calcnova.app.feature.settings.SettingsViewModel
import com.calcnova.app.ui.components.CalcButton
import com.calcnova.app.ui.components.CalcButtonStyle

private data class SciKey(val label: String, val token: String, val style: CalcButtonStyle)

private val keys = listOf(
    SciKey("sin", "sin(", CalcButtonStyle.FUNCTION), SciKey("cos", "cos(", CalcButtonStyle.FUNCTION), SciKey("tan", "tan(", CalcButtonStyle.FUNCTION), SciKey("DEL", "", CalcButtonStyle.DANGER),
    SciKey("ln", "ln(", CalcButtonStyle.FUNCTION), SciKey("log", "log(", CalcButtonStyle.FUNCTION), SciKey("√", "sqrt(", CalcButtonStyle.FUNCTION), SciKey("AC", "", CalcButtonStyle.DANGER),
    SciKey("(", "(", CalcButtonStyle.FUNCTION), SciKey(")", ")", CalcButtonStyle.FUNCTION), SciKey("^", "^", CalcButtonStyle.FUNCTION), SciKey("!", "!", CalcButtonStyle.FUNCTION),
    SciKey("7", "7", CalcButtonStyle.NUMBER), SciKey("8", "8", CalcButtonStyle.NUMBER), SciKey("9", "9", CalcButtonStyle.NUMBER), SciKey("÷", "/", CalcButtonStyle.OPERATOR),
    SciKey("4", "4", CalcButtonStyle.NUMBER), SciKey("5", "5", CalcButtonStyle.NUMBER), SciKey("6", "6", CalcButtonStyle.NUMBER), SciKey("×", "*", CalcButtonStyle.OPERATOR),
    SciKey("1", "1", CalcButtonStyle.NUMBER), SciKey("2", "2", CalcButtonStyle.NUMBER), SciKey("3", "3", CalcButtonStyle.NUMBER), SciKey("-", "-", CalcButtonStyle.OPERATOR),
    SciKey("π", "π", CalcButtonStyle.NUMBER), SciKey("0", "0", CalcButtonStyle.NUMBER), SciKey(".", ".", CalcButtonStyle.NUMBER), SciKey("+", "+", CalcButtonStyle.OPERATOR),
    SciKey("=", "", CalcButtonStyle.ACCENT)
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScientificCalculatorScreen(
    settingsViewModel: SettingsViewModel,
    viewModel: ScientificCalculatorViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val settings by settingsViewModel.settings.collectAsState()

    LaunchedEffect(settings.autoCalculate) { viewModel.setAutoCalculate(settings.autoCalculate) }
    LaunchedEffect(Unit) { viewModel.setDefaultAngleMode(settings.defaultAngleMode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("DEG", "RAD", "GRAD").forEach { mode ->
                FilterChip(
                    selected = state.angleMode == mode,
                    onClick = { viewModel.onAngleModeChange(mode) },
                    label = { Material3Text(mode) }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            Material3Text(
                text = state.expression.ifBlank { "0" },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))

            AnimatedContent(
                targetState = Pair(state.errorMessage != null, state.errorMessage ?: state.result),
                transitionSpec = { (slideInVertically { it / 3 }) togetherWith (slideOutVertically { -it / 3 }) },
                label = "sciResultAnimation"
            ) { (isError, text) ->
                if (isError) {
                    Material3Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Material3Text(
                        text = text,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isLivePreview) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = keys,
                span = { key -> if (key.label == "=") GridItemSpan(4) else GridItemSpan(1) }
            ) { key ->
                CalcButton(
                    label = key.label,
                    style = key.style,
                    hapticEnabled = settings.hapticFeedback,
                    aspectRatio = if (key.label == "=") 5.5f else 1.15f
                ) {
                    when (key.label) {
                        "AC" -> viewModel.onClear()
                        "DEL" -> viewModel.onDelete()
                        "=" -> viewModel.onEquals()
                        else -> viewModel.insertToken(key.token)
                    }
                }
            }
        }
    }
}
