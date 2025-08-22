package com.rayhan.triptracker.ui.screens.tracking

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayhan.triptracker.service.TrackingService
import com.rayhan.triptracker.service.TrackingSession
import com.rayhan.triptracker.util.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackingState(
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val distanceMeters: Double = 0.0,
    val elapsedMs: Long = 0L,
    val speedMps: Float = 0f,
    val currentLatLng: LatLng? = null,
    val startTime: Long? = null,
)

@HiltViewModel
class TrackingViewModel @Inject constructor(private val app: Application) : ViewModel() {
    private val _state = MutableStateFlow(TrackingState())
    val state: StateFlow<TrackingState> = _state.asStateFlow()

    private var accumulatedTimeMs = 0L
    private var lastStartTime = 0L

    init {
        //Get updated value from service
        viewModelScope.launch {
            TrackingSession.updates.collect { update ->
                _state.value = _state.value.copy(
                    speedMps = update.speedMps,
                    distanceMeters = update.distanceMeters,
                    currentLatLng = update.currentLatLng,

                    )
            }
        }

        // Update time
        viewModelScope.launch {
            while (true) {
                if (_state.value.isTracking && !_state.value.isPaused) {
                    _state.value = _state.value.copy(
                        elapsedMs = accumulatedTimeMs + (System.currentTimeMillis() - lastStartTime)
                    )
                }
                delay(1000)
            }
        }
    }

    fun start() {
        app.startService(Intent(app, TrackingService::class.java).apply {
            action = TrackingService.ACTION_START
        })
        lastStartTime = System.currentTimeMillis()
        _state.value = _state.value.copy(
            isTracking = true,
            isPaused = false,
            startTime = lastStartTime,
        )
    }

    fun pauseResume() {
        val paused = !_state.value.isPaused
        app.startService(Intent(app, TrackingService::class.java).apply {
            action = if (paused) TrackingService.ACTION_PAUSE else TrackingService.ACTION_RESUME
        })

        val currentTime = System.currentTimeMillis()

        if (paused) {
            accumulatedTimeMs += currentTime - lastStartTime
        } else {
            lastStartTime = currentTime
        }

        _state.value = _state.value.copy(isPaused = paused)
    }

    fun stop() {
        app.startService(Intent(app, TrackingService::class.java).apply {
            action = TrackingService.ACTION_STOP
        })
        _state.value = TrackingState()
    }

}
