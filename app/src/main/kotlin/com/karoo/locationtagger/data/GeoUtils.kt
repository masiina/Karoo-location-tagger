package com.karoo.locationtagger.data

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GeoUtils {

    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Haversine distance between two points in meters.
     */
    fun distanceMeters(fromLat: Double, fromLng: Double, toLat: Double, toLng: Double): Double {
        val dLat = Math.toRadians(toLat - fromLat)
        val dLng = Math.toRadians(toLng - fromLng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(fromLat)) * cos(Math.toRadians(toLat)) *
            sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Initial bearing in degrees from north (0-360).
     */
    fun bearingDegrees(fromLat: Double, fromLng: Double, toLat: Double, toLng: Double): Double {
        val dLng = Math.toRadians(toLng - fromLng)
        val lat1 = Math.toRadians(fromLat)
        val lat2 = Math.toRadians(toLat)
        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        var bearing = Math.toDegrees(atan2(y, x))
        if (bearing < 0) bearing += 360.0
        return bearing
    }

    /**
     * Returns relative direction in degrees: 0 = straight ahead, positive = right, negative = left.
     */
    fun relativeDirection(bearing: Double, heading: Double): Double {
        var rel = bearing - heading
        if (rel > 180) rel -= 360
        if (rel < -180) rel += 360
        return rel
    }

    fun formatDistance(meters: Double): String {
        return when {
            meters < 1000 -> "%.0f m".format(meters)
            meters < 10000 -> "%.1f km".format(meters / 1000)
            else -> "%.0f km".format(meters / 1000)
        }
    }

    fun formatDirection(degrees: Double): String {
        return when {
            degrees < -157.5 || degrees >= 157.5 -> "Behind"
            degrees < -112.5 -> "Left"
            degrees < -67.5 -> "Front-Left"
            degrees < -22.5 -> "Slight Left"
            degrees < 22.5 -> "Ahead"
            degrees < 67.5 -> "Slight Right"
            degrees < 112.5 -> "Right"
            else -> "Front-Right"
        }
    }

    fun directionArrow(degrees: Double): String {
        return when {
            degrees < -157.5 || degrees >= 157.5 -> "↓"
            degrees < -112.5 -> "↙"
            degrees < -67.5 -> "⬅"
            degrees < -22.5 -> "↖"
            degrees < 22.5 -> "⬆"
            degrees < 67.5 -> "↗"
            degrees < 112.5 -> "➡"
            else -> "↘"
        }
    }
}