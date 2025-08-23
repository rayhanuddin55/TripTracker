package com.rayhan.triptracker.util

// m or km
fun formatDistance(meters: Double): String =
    if (meters < 1000) "${"%.0f".format(meters)} m" else "${"%.2f".format(meters / 1000)} km"

// h:m:s
fun formatDuration(ms: Long): String {
    val s = ms / 1000
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return "%02d:%02d:%02d".format(h, m, sec)
}
