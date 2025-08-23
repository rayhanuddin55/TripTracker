package com.rayhan.triptracker.ui.screens.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rayhan.triptracker.util.formatDistance
import com.rayhan.triptracker.util.formatDuration
import com.rayhan.triptracker.util.toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(padding: PaddingValues) {
    val vm: HistoryViewModel = hiltViewModel<HistoryViewModel>()
    val trips by vm.trips.collectAsState()
    val fmt = remember { SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault()) }
    val context = LocalContext.current


    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("Trip History") }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            items(trips) { t ->

                val durationMs = if (t.endTime != null) {
                    (t.endTime - t.startTime - (t.totalPausedMs ?: 0L))
                } else {
                    0L
                }

                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Start: ${fmt.format(Date(t.startTime))}")
                            Text("Duration: ${formatDuration(durationMs)}")
                            Text("Distance: ${formatDistance(t.distanceMeters)}")
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = {
                            vm.export(t.id, onComplete = { uri ->
                                if (uri != null) {
                                    context.toast("Exported to $uri")
                                }
                            })


                        }) {
                            Icon(
                                imageVector = Icons.Filled.ExitToApp,
                                contentDescription = "export"
                            )
                        }
                    }
                }
            }
        }
    }

}
