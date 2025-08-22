package com.rayhan.triptracker.ui.screens.tracking

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
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
            Text("Speed: ${"%.1f".format(state.speedMps * 3.6)} km/h")
            Text("Distance: ${"%.0f".format(state.distanceMeters)} m")
            Text("Time: ${state.elapsedMs / 1000}s")
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
                    Toast.makeText(context, "Tracking started", Toast.LENGTH_SHORT).show()
                },
                enabled = !state.isTracking,
                modifier = Modifier.weight(1f)
            ) { Text("Start") }
            Button(
                onClick = {
                    vm.pauseResume()
                    val msg = if (state.isPaused) "Tracking resumed" else "Tracking paused"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                enabled = state.isTracking,
                modifier = Modifier.weight(1f)
            ) { Text(if (state.isPaused) "Resume" else "Pause") }
            Button(
                onClick = {
                    vm.stop()
                    Toast.makeText(context, "Tracking stopped", Toast.LENGTH_SHORT).show()
                },
                enabled = state.isTracking,
                modifier = Modifier.weight(1f)
            ) {
                Text("Stop")

            }
        }
    }
}
