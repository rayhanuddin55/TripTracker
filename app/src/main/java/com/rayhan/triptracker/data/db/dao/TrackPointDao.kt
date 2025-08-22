package com.rayhan.triptracker.data.db.dao

import androidx.room.*
import com.rayhan.triptracker.data.db.entity.TrackPoint
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackPointDao {
    @Insert
    suspend fun insert(point: TrackPoint): Long

    @Query("SELECT * FROM track_points WHERE tripId = :tripId ORDER BY timestamp ASC")
    fun observeByTrip(tripId: Long): Flow<List<TrackPoint>>

    @Query("SELECT * FROM track_points WHERE tripId = :tripId ORDER BY timestamp ASC")
    suspend fun getByTrip(tripId: Long): List<TrackPoint>
}
