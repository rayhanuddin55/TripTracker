package com.rayhan.triptracker.ui.navigation

import android.graphics.drawable.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rayhan.triptracker.ui.screens.history.HistoryScreen
import com.rayhan.triptracker.ui.screens.settings.SettingsScreen
import com.rayhan.triptracker.ui.screens.tracking.TrackingScreen

enum class Destinations(val route: String, val label: String, val icon: ImageVector) {
    Tracking("tracking", "Tracking", Icons.Filled.LocationOn),
    History("history", "History", Icons.Filled.Menu),
    Settings("settings", "Settings", Icons.Filled.Settings)
}

@Composable
fun TripNavHost() {
    val navController = rememberNavController()
    val items = listOf(Destinations.Tracking, Destinations.History, Destinations.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val current = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        selected = current == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        },
                        label = { Text(item.label) },
                        icon = { Icon(imageVector = item.icon, contentDescription = item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = Destinations.Tracking.route) {
            composable(Destinations.Tracking.route) { TrackingScreen(padding) }
            composable(Destinations.History.route) { HistoryScreen(padding) }
            composable(Destinations.Settings.route) { SettingsScreen(padding) }
        }
    }
}
