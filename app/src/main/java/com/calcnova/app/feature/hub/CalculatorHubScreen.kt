package com.calcnova.app.feature.hub

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calcnova.app.feature.basic.BasicCalculatorScreen
import com.calcnova.app.feature.gst.GstCalculatorScreen
import com.calcnova.app.feature.programmer.ProgrammerCalculatorScreen
import com.calcnova.app.feature.scientific.ScientificCalculatorScreen
import com.calcnova.app.feature.settings.SettingsViewModel

private val tabTitles = listOf("Basic", "Scientific", "Developer", "GST")

/**
 * One "Calculator" destination in the bottom nav, with its four modes as
 * tabs at the top - keeps the bottom navigation bar to three clean items
 * (Calculator / History / Settings) instead of cramming every calculator
 * mode in there.
 */
@Composable
fun CalculatorHubScreen(settingsViewModel: SettingsViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            edgePadding = 16.dp
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "calculatorTabSwitch",
            modifier = Modifier.fillMaxSize()
        ) { tab ->
            when (tab) {
                0 -> BasicCalculatorScreen(settingsViewModel)
                1 -> ScientificCalculatorScreen(settingsViewModel)
                2 -> ProgrammerCalculatorScreen(settingsViewModel)
                else -> GstCalculatorScreen()
            }
        }
    }
}
