package com.rayhan.triptracker.ui.screens.tracking

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rayhan.triptracker.service.TrackingService
import com.rayhan.triptracker.service.TrackingSession
import com.rayhan.triptracker.util.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    private var timerJob: Job? = null

    init {
        //Get updated value from service
        viewModelScope.launch {
            TrackingSession.updates.collect { update ->
                _state.update {
                    _state.value.copy(
                        speedMps = update.speedMps,
                        distanceMeters = update.distanceMeters,
                        currentLatLng = update.currentLatLng,

                        )
                }
            }
        }
    }

    //Start timer when tracking starts
    @OptIn(ObsoleteCoroutinesApi::class)
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            ticker(delayMillis = 1000, initialDelayMillis = 0).consumeEach {
                _state.update { state ->
                    if (state.isTracking && !state.isPaused) {
                        state.copy(
                            elapsedMs = accumulatedTimeMs + (System.currentTimeMillis() - lastStartTime)
                        )
                    } else state
                }

            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // Tracking Start
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
        startTimer()
    }

    // Tracking pause handle
    fun pauseResume() {
        val paused = !_state.value.isPaused
        app.startService(Intent(app, TrackingService::class.java).apply {
            action = if (paused) TrackingService.ACTION_PAUSE else TrackingService.ACTION_RESUME
        })

        updateAccumulatedTime(paused)

        _state.value = _state.value.copy(isPaused = paused)
    }

    // Find accumulated time to calculate paused time
    private fun updateAccumulatedTime(paused: Boolean) {
        val now = System.currentTimeMillis()
        if (paused) {
            accumulatedTimeMs += now - lastStartTime
        } else {
            lastStartTime = now
        }
    }

    // Tracking stop
    fun stop() {
        app.startService(Intent(app, TrackingService::class.java).apply {
            action = TrackingService.ACTION_STOP
        })

        accumulatedTimeMs = 0L
        lastStartTime = 0L
        stopTimer()
        _state.value = TrackingState()
    }

}
