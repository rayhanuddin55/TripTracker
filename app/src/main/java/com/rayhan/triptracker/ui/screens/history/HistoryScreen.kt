package com.rayhan.triptracker.ui.screens.history

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.util.TableInfo
import com.rayhan.triptracker.util.formatDistance
import com.rayhan.triptracker.util.formatDuration
import org.w3c.dom.Text
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
                                    Toast.makeText(context, "Exported to $uri", Toast.LENGTH_LONG)
                                        .show()
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
