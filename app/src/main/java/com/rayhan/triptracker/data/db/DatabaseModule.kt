package com.rayhan.triptracker.data.db

import android.content.Context
import androidx.room.Room
import com.rayhan.triptracker.data.db.dao.TrackPointDao
import com.rayhan.triptracker.data.db.dao.TripDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "triptracker.db"
        ).build()
    }

    @Provides
    fun provideTripDao(db: AppDatabase): TripDao = db.tripDao()

    @Provides
    fun provideTrackPointDao(db: AppDatabase): TrackPointDao = db.trackPointDao()
}
