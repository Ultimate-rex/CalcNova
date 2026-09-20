package com.calcnova.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.calcnova.app.feature.history.HistoryScreen
import com.calcnova.app.feature.hub.CalculatorHubScreen
import com.calcnova.app.feature.settings.SettingsScreen
import com.calcnova.app.feature.settings.SettingsViewModel

private sealed class Destination(val route: String, val label: String) {
    data object Calculator : Destination("calculator", "Calculator")
    data object History : Destination("history", "History")
    data object Settings : Destination("settings", "Settings")
}

private val destinations = listOf(Destination.Calculator, Destination.History, Destination.Settings)

@Composable
private fun iconFor(destination: Destination) = when (destination) {
    Destination.Calculator -> Icons.Default.Calculate
    Destination.History -> Icons.Default.History
    Destination.Settings -> Icons.Default.Settings
}

@Composable
fun CalcNovaApp(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination

                destinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(iconFor(destination), contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Calculator.route,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(animationSpec = tween(180)) + slideInHorizontally(initialOffsetX = { it / 8 }) },
            exitTransition = { fadeOut(animationSpec = tween(140)) },
            popEnterTransition = { fadeIn(animationSpec = tween(180)) },
            popExitTransition = { fadeOut(animationSpec = tween(140)) + slideOutHorizontally(targetOffsetX = { it / 8 }) }
        ) {
            composable(Destination.Calculator.route) { CalculatorHubScreen(settingsViewModel) }
            composable(Destination.History.route) { HistoryScreen() }
            composable(Destination.Settings.route) { SettingsScreen(settingsViewModel) }
        }
    }
}
