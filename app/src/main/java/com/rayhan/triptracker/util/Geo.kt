package com.rayhan.triptracker.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class LatLng(val lat: Double, val lng: Double)

// Calculate distance between two lat/lng points in meters using Haversine formula
fun haversineMeters(a: LatLng, b: LatLng): Double {
    val R = 6371000.0
    val dLat = Math.toRadians(b.lat - a.lat)
    val dLng = Math.toRadians(b.lng - a.lng)
    val s1 =
        sin(dLat / 2).pow(2.0) + cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) * sin(dLng / 2).pow(
            2.0
        )
    val c = 2 * atan2(sqrt(s1), sqrt(1 - s1))
    return R * c
}
