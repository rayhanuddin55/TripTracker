package com.rayhan.triptracker.service

import com.rayhan.triptracker.util.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Using this in service to emit value to vm
object TrackingSession {
    private val _updates = MutableStateFlow(TrackingUpdate())
    val updates: StateFlow<TrackingUpdate> = _updates.asStateFlow()

    fun emit(update: TrackingUpdate) {
        _updates.value = update
    }
}

data class TrackingUpdate(
    val speedMps: Float = 0f,
    val distanceMeters: Double = 0.0,
    val currentLatLng: LatLng? = null,
)