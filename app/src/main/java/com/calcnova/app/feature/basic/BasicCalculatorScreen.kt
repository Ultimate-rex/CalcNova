package com.calcnova.app.feature.basic

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
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

private data class Key(val label: String, val style: CalcButtonStyle)

private val keys = listOf(
    Key("AC", CalcButtonStyle.DANGER), Key("DEL", CalcButtonStyle.FUNCTION), Key("%", CalcButtonStyle.FUNCTION), Key("÷", CalcButtonStyle.OPERATOR),
    Key("7", CalcButtonStyle.NUMBER), Key("8", CalcButtonStyle.NUMBER), Key("9", CalcButtonStyle.NUMBER), Key("×", CalcButtonStyle.OPERATOR),
    Key("4", CalcButtonStyle.NUMBER), Key("5", CalcButtonStyle.NUMBER), Key("6", CalcButtonStyle.NUMBER), Key("-", CalcButtonStyle.OPERATOR),
    Key("1", CalcButtonStyle.NUMBER), Key("2", CalcButtonStyle.NUMBER), Key("3", CalcButtonStyle.NUMBER), Key("+", CalcButtonStyle.OPERATOR),
    Key("(", CalcButtonStyle.FUNCTION), Key("0", CalcButtonStyle.NUMBER), Key(")", CalcButtonStyle.FUNCTION), Key(".", CalcButtonStyle.NUMBER),
    Key("=", CalcButtonStyle.ACCENT)
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BasicCalculatorScreen(
    settingsViewModel: SettingsViewModel,
    viewModel: BasicCalculatorViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val settings by settingsViewModel.settings.collectAsState()

    LaunchedEffect(settings.autoCalculate) {
        viewModel.setAutoCalculate(settings.autoCalculate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = state.expression.ifBlank { "0" },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            AnimatedContent(
                targetState = Pair(state.errorMessage != null, state.errorMessage ?: state.result),
                transitionSpec = {
                    (slideInVertically { it / 3 }) togetherWith (slideOutVertically { -it / 3 })
                },
                label = "resultAnimation"
            ) { (isError, text) ->
                if (isError) {
                    Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isLivePreview) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = keys,
                span = { key -> if (key.label == "=") GridItemSpan(4) else GridItemSpan(1) }
            ) { key ->
                CalcButton(
                    label = key.label,
                    style = key.style,
                    hapticEnabled = settings.hapticFeedback,
                    aspectRatio = if (key.label == "=") 4.5f else 1.3f
                ) {
                    when (key.label) {
                        "AC" -> viewModel.onClear()
                        "DEL" -> viewModel.onDelete()
                        "=" -> viewModel.onEquals()
                        else -> viewModel.onDigit(key.label)
                    }
                }
            }
        }
    }
}
