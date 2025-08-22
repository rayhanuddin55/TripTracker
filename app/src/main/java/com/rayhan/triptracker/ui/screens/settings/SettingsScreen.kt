package com.rayhan.triptracker.ui.screens.settings

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(padding: PaddingValues) {
    val vm: SettingsViewModel = hiltViewModel<SettingsViewModel>()
    val interval by vm.interval.collectAsState()
    val bg by vm.background.collectAsState()
    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Location update interval")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 5, 10).forEach { sec ->
                    Log.d("interval", "interval: $interval")
                    FilterChip(
                        selected = interval == sec,
                        onClick = { vm.setInterval(sec) },
                        label = { Text("${sec}s") }
                    )
                }
            }
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Allow background tracking")
                Switch(checked = bg, onCheckedChange = { vm.setBackground(it) })
            }
        }
    }
}
