package com.rayhan.triptracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rayhan.triptracker.data.db.dao.TrackPointDao
import com.rayhan.triptracker.data.db.dao.TripDao
import com.rayhan.triptracker.data.db.entity.TrackPoint
import com.rayhan.triptracker.data.db.entity.Trip
import javax.inject.Inject
import javax.inject.Singleton

@Database(entities = [Trip::class, TrackPoint::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun trackPointDao(): TrackPointDao

}
