package com.rayhan.triptracker.data.repo

import com.rayhan.triptracker.data.db.AppDatabase
import com.rayhan.triptracker.data.db.entity.TrackPoint
import com.rayhan.triptracker.data.db.entity.Trip
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(private val db: AppDatabase) {
    fun observeTrips(): Flow<List<Trip>> = db.tripDao().observeTrips()
    suspend fun getTrips(): List<Trip> = db.tripDao().getTrips()

    suspend fun startTrip(startTime: Long = System.currentTimeMillis()): Long {
        return db.tripDao().insert(Trip(startTime = startTime))
    }

    suspend fun appendPoint(p: TrackPoint) {
        db.trackPointDao().insert(p)
    }

    suspend fun endTrip(tripId: Long, endTime: Long, distanceMeters: Double, pausedMs: Long) {
        db.tripDao().getById(tripId)?.let {
            db.tripDao().update(
                it.copy(
                    endTime = endTime,
                    distanceMeters = distanceMeters,
                    totalPausedMs = pausedMs
                )
            )
        }
    }

    fun observePoints(tripId: Long) = db.trackPointDao().observeByTrip(tripId)
    suspend fun points(tripId: Long) = db.trackPointDao().getByTrip(tripId)
}
