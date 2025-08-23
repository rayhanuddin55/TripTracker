package com.rayhan.triptracker.ui.screens.tracking

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.rayhan.triptracker.util.toast
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TrackingScreen(padding: PaddingValues) {
    val vm: TrackingViewModel = hiltViewModel<TrackingViewModel>()
    val state by vm.state.collectAsState()

    val context = LocalContext.current

    val notificationPermission = rememberPermissionState(
        Manifest.permission.POST_NOTIFICATIONS
    )

    val locationPermission = rememberMultiplePermissionsState(
        mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val cameraPositionState = rememberCameraPositionState()

    val coroutineScope = rememberCoroutineScope()

    var locationEnabled by remember { mutableStateOf(false) }

    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Initial zoom to current location
    LaunchedEffect(locationPermission.allPermissionsGranted) {
        try {
            val lastLocation = fusedClient.lastLocation.await()
            lastLocation?.let { loc ->
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(
                        LatLng(loc.latitude, loc.longitude),
                        16f
                    )
                )
            }
        } catch (e: SecurityException) {
            Log.e("TrackingScreen", "Location unknown", e)
        }
    }

    // Update isMyLocationEnabled in map
    LaunchedEffect(locationPermission.allPermissionsGranted) {
        if (locationPermission.allPermissionsGranted) {
            locationEnabled = true
        }
    }

    LaunchedEffect(locationPermission.allPermissionsGranted) {
        locationPermission.launchMultiplePermissionRequest()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launchPermissionRequest()
        }
    }

    // Follow User current location on map
    LaunchedEffect(state.currentLatLng) {
        if (locationPermission.allPermissionsGranted && state.currentLatLng != null) {
            coroutineScope.launch {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            state.currentLatLng?.lat ?: 0.0, state.currentLatLng?.lng ?: 0.0
                        ), 16f
                    )
                )
            }
        }
    }

    val speedText = remember(state.speedMps) { "%.1f".format(state.speedMps * 3.6) }
    val distanceText = remember(state.distanceMeters) { "%.0f".format(state.distanceMeters) }
    val timeText = remember(state.elapsedMs) { "${state.elapsedMs / 1000}s" }

    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Box(Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                uiSettings = MapUiSettings(zoomControlsEnabled = true),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = locationEnabled
                )
            )
        }
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Speed: $speedText km/h")
            Text("Distance: $distanceText m")
            Text("Time: ${timeText}")
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    vm.start()
                    context.toast("Tracking started")
                },
                enabled = !state.isTracking,
                modifier = Modifier.weight(1f)
            ) { Text("Start") }
            Button(
                onClick = {
                    vm.pauseResume()
                    val msg = if (state.isPaused) "Tracking resumed" else "Tracking paused"
                    context.toast(msg)
                },
                enabled = state.isTracking,
                modifier = Modifier.weight(1f)
            ) { Text(if (state.isPaused) "Resume" else "Pause") }
            Button(
                onClick = {
                    vm.stop()
                    context.toast("Tracking stopped")
                },
                enabled = state.isTracking,
                modifier = Modifier.weight(1f)
            ) {
                Text("Stop")

            }
        }
    }
}
