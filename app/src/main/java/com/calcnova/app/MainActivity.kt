package com.calcnova.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.calcnova.app.feature.settings.SettingsViewModel
import com.calcnova.app.ui.navigation.CalcNovaApp
import com.calcnova.app.ui.theme.CalcNovaTheme

class MainActivity : ComponentActivity() {

    // Activity-scoped so every screen (including Settings itself) shares one
    // instance - a theme change made in Settings applies instantly app-wide.
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by settingsViewModel.settings.collectAsState()
            val darkTheme = when (settings.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            CalcNovaTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CalcNovaApp(settingsViewModel = settingsViewModel)
                }
            }
        }
    }
}
