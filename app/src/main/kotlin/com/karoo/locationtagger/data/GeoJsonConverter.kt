package com.karoo.locationtagger.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Converts a list of POIs into a GeoJSON FeatureCollection string
 * suitable for uploading to Spatix.io.
 */

@Serializable
data class GeoJsonFeatureCollection(
    val type: String = "FeatureCollection",
    val features: List<GeoJsonFeature>,
)

@Serializable
data class GeoJsonFeature(
    val type: String = "Feature",
    val geometry: GeoJsonGeometry,
    val properties: Map<String, kotlinx.serialization.json.JsonElement>,
)

@Serializable
data class GeoJsonGeometry(
    val type: String = "Point",
    val coordinates: List<Double>,
)

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

fun List<Poi>.toGeoJsonFeatureCollection(): String {
    val features = this.map { poi ->
        GeoJsonFeature(
            geometry = GeoJsonGeometry(
                coordinates = listOf(poi.lng, poi.lat)
            ),
            properties = mapOf(
                "id" to kotlinx.serialization.json.JsonPrimitive(poi.id),
                "name" to kotlinx.serialization.json.JsonPrimitive(poi.displayName),
                "poiType" to kotlinx.serialization.json.JsonPrimitive(poi.type.name),
                "potential" to kotlinx.serialization.json.JsonPrimitive(poi.potential),
            ),
        )
    }
    return json.encodeToString(GeoJsonFeatureCollection(features = features))
}