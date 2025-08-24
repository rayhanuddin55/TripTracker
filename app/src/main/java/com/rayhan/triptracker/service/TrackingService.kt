package com.rayhan.triptracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.rayhan.triptracker.MainActivity
import com.rayhan.triptracker.R
import com.rayhan.triptracker.data.db.entity.TrackPoint
import com.rayhan.triptracker.data.repo.SettingsRepository
import com.rayhan.triptracker.data.repo.TripRepository
import com.rayhan.triptracker.util.LatLng
import com.rayhan.triptracker.util.haversineMeters
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class TrackingService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var fused: FusedLocationProviderClient
    private var locationUpdateJob: Job? = null

    @Inject
    lateinit var tripRepo: TripRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private var currentTripId: Long? = null
    private var lastLatLng: LatLng? = null
    private var distanceMeters: Double = 0.0
    private var startTime: Long = 0L
    private var paused: Boolean = false

    private val stationaryWindowMs = 120_000L
    private var lastMoveTime = System.currentTimeMillis()

    private var lastPauseStart: Long? = null
    private var totalPausedMs: Long = 0L

    override fun onCreate() {
        super.onCreate()
        fused = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_PAUSE -> pauseTracking(true)
            ACTION_RESUME -> pauseTracking(false)
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        scope.launch {
            val allowBackground = settingsRepository.backgroundEnabled.first()

            if (!allowBackground) {
                stopSelf()
            }
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun startTracking() {
        if (currentTripId != null) return
        scope.launch {
            currentTripId = tripRepo.startTrip()
            startTime = System.currentTimeMillis()
            lastMoveTime = startTime
            startForeground(NOTIF_ID, buildNotification())
            requestLocationUpdates()
        }
    }


    private fun pauseTracking(pause: Boolean) {
        paused = pause
        updateNotification()

        val currentTime = System.currentTimeMillis()

        if (pause) {
            fused.removeLocationUpdates(locationCb)
            lastPauseStart = currentTime
        } else {
            // Resume
            lastPauseStart?.let {
                totalPausedMs += currentTime - it
            }
            lastPauseStart = null
            requestLocationUpdates()
        }
    }

    private fun stopTracking() {
        scope.launch {
            fused.removeLocationUpdates(locationCb)
            val id = currentTripId
            if (id != null) {
                tripRepo.endTrip(id, System.currentTimeMillis(), distanceMeters, totalPausedMs)
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun requestLocationUpdates() {
        locationUpdateJob?.cancel()
        locationUpdateJob = scope.launch {
            settingsRepository.intervalSeconds.distinctUntilChanged().collect { intervalSec ->
                if (currentTripId == null) return@collect
                Log.d("TrackingService", "Requesting location updates every $intervalSec seconds")

                fused.removeLocationUpdates(locationCb)

                val req =
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalSec * 1000L)
//                        .setMinUpdateIntervalMillis(1000L)
                        .setMinUpdateDistanceMeters(1f)
                        .build()
                try {
                    fused.requestLocationUpdates(req, locationCb, mainLooper)
                } catch (e: SecurityException) {
                    Log.e("TrackingService", "Request Update Error", e)
                }
                updateNotification()
            }

        }

    }

    private val locationCb = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            scope.launch {
                if (paused) return@launch
                val id = currentTripId ?: return@launch
                val loc: Location = result.lastLocation ?: return@launch

                val currentTime = System.currentTimeMillis()

                val cur = LatLng(loc.latitude, loc.longitude)
                val last = lastLatLng
                if (last != null) {
                    val d = haversineMeters(last, cur) // Calculate distance in meters
                    if (d >= 2.0) {
                        distanceMeters += d
                        lastMoveTime = currentTime
                    }
                }
                lastLatLng = cur

                // Save points. Will export to csv file.
                tripRepo.appendPoint(
                    TrackPoint(
                        tripId = id,
                        timestamp = currentTime,
                        lat = cur.lat,
                        lng = cur.lng,
                        speedMps = loc.speed
                    )
                )

                // send update to vm with latest data
                TrackingSession.emit(
                    TrackingUpdate(
                        speedMps = loc.speed,
                        distanceMeters = distanceMeters,
                        currentLatLng = cur
                    )
                )

                if (currentTime - lastMoveTime > stationaryWindowMs) {
                    stopTracking()
                }

                Log.d("TrackingService", "Location: $cur")

                updateNotification()
            }
        }
    }

    private fun createNotificationChannel() {
        val mgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = getString(R.string.notification_channel_desc) }
        mgr.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

//        val stopAction = NotificationCompat.Action(
//            0, getString(R.string.notification_stop),
//            PendingIntent.getService(this, 2, Intent(this, TrackingService::class.java).apply {
//                action = ACTION_STOP
//            }, PendingIntent.FLAG_IMMUTABLE)
//        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(getString(R.string.notification_title))
            .setContentIntent(pendingIntent)
//            .addAction(stopAction)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val mgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(NOTIF_ID, buildNotification())
    }

    companion object {
        const val CHANNEL_ID = "tracking_channel"
        const val NOTIF_ID = 1001

        const val ACTION_START = "com.rayhan.triptracker.action.START"
        const val ACTION_PAUSE = "com.rayhan.triptracker.action.PAUSE"
        const val ACTION_RESUME = "com.rayhan.triptracker.action.RESUME"
        const val ACTION_STOP = "com.rayhan.triptracker.action.STOP"
    }
}
