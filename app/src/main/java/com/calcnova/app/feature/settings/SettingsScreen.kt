package com.calcnova.app.feature.settings

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()
    var showLegalDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Spacer(Modifier.height(16.dp))
                SettingsSwitchRow(
                    title = "Match wallpaper colors",
                    subtitle = "Material You dynamic color, based on your device theme",
                    checked = settings.useDynamicColor,
                    onCheckedChange = viewModel::setUseDynamicColor
                )
            }
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

        SettingsSection(title = "About") {
            SettingsLinkRow(title = "Version", value = "2.5.0")
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { showLegalDialog = true }) {
                Text("Privacy & Legal information")
            }
        }

        Spacer(Modifier.height(8.dp))
    }

    if (showLegalDialog) {
        LegalInfoDialog(onDismiss = { showLegalDialog = false })
    }
}

@Composable
private fun LegalInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("Privacy & Legal") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "CalcNova works fully offline. It requests no permissions, " +
                        "makes no network requests, and never collects, transmits, " +
                        "or shares any data.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Calculation history and your preferences are stored only on " +
                        "this device, as plain JSON files in the app's private " +
                        "storage. Nothing leaves your phone. You can clear history " +
                        "any time from the History tab.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Full Privacy Policy, Terms of Service, and License (MIT) are " +
                        "included with the app's source code.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
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
private fun SettingsLinkRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
