package com.calcnova.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.calcnova.app.feature.basic.BasicCalculatorScreen
import com.calcnova.app.feature.history.HistoryScreen
import com.calcnova.app.feature.programmer.ProgrammerCalculatorScreen
import com.calcnova.app.feature.scientific.ScientificCalculatorScreen
import com.calcnova.app.feature.settings.SettingsScreen
import com.calcnova.app.feature.settings.SettingsViewModel

private sealed class Destination(val route: String, val label: String) {
    data object Basic : Destination("basic", "Basic")
    data object Scientific : Destination("scientific", "Scientific")
    data object Programmer : Destination("programmer", "Developer")
    data object History : Destination("history", "History")
    data object Settings : Destination("settings", "Settings")
}

private val destinations = listOf(
    Destination.Basic, Destination.Scientific, Destination.Programmer, Destination.History, Destination.Settings
)

@Composable
private fun iconFor(destination: Destination) = when (destination) {
    Destination.Basic -> Icons.Default.Dialpad
    Destination.Scientific -> Icons.Default.Calculate
    Destination.Programmer -> Icons.Default.Memory
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
            startDestination = Destination.Basic.route,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(180)) + slideInHorizontally(initialOffsetX = { it / 8 }) },
            exitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(140)) },
            popEnterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(180)) },
            popExitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(140)) + slideOutHorizontally(targetOffsetX = { it / 8 }) }
        ) {
            composable(Destination.Basic.route) { BasicCalculatorScreen(settingsViewModel) }
            composable(Destination.Scientific.route) { ScientificCalculatorScreen(settingsViewModel) }
            composable(Destination.Programmer.route) { ProgrammerCalculatorScreen(settingsViewModel) }
            composable(Destination.History.route) { HistoryScreen() }
            composable(Destination.Settings.route) { SettingsScreen(settingsViewModel) }
        }
    }
}
