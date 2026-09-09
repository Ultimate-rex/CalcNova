package com.calcnova.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        SettingsSection(title = "Appearance") {
            Text("Theme", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmented(
                options = listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark"),
                selected = settings.themeMode,
                onSelect = viewModel::setThemeMode
            )
        }

        SettingsSection(title = "Calculation") {
            SettingsSwitchRow(
                title = "Auto-calculate",
                subtitle = "Show the result live while typing, without pressing =",
                checked = settings.autoCalculate,
                onCheckedChange = viewModel::setAutoCalculate
            )
            Spacer(Modifier.height(16.dp))
            Text("Default angle mode", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmented(
                options = listOf("DEG" to "Degrees", "RAD" to "Radians", "GRAD" to "Gradians"),
                selected = settings.defaultAngleMode,
                onSelect = viewModel::setDefaultAngleMode
            )
        }

        SettingsSection(title = "Feedback") {
            SettingsSwitchRow(
                title = "Haptic feedback",
                subtitle = "Vibrate slightly on every button press",
                checked = settings.hapticFeedback,
                onCheckedChange = viewModel::setHapticFeedback
            )
        }

        Spacer(Modifier.weight(1f))

        Text(
            "CalcNova · Phase 1",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SingleChoiceSegmented(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) }
            )
        }
    }
}
