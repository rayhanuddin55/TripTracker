package com.rayhan.triptracker.ui.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rayhan.triptracker.data.repo.ExportRepository
import com.rayhan.triptracker.data.repo.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    app: Application,
    private val repo: TripRepository,
    private val exportRepository: ExportRepository
) :
    AndroidViewModel(app) {

    val trips = repo.observeTrips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun export(tripId: Long, onComplete: (String?) -> Unit = {}) {
        viewModelScope.launch {
            try {
                // Fetch the points for the trip
                val points = repo.points(tripId)
                if (points.isEmpty()) {
                    onComplete(null)
                    return@launch
                }

                // Export to CSV
                val fileName = exportRepository.exportTripToCsv(tripId, points)
                onComplete(fileName)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(null)
            }
        }
    }


}
