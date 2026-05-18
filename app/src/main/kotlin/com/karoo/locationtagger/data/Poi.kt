package com.karoo.locationtagger.data

import kotlinx.serialization.Serializable

@Serializable
data class Poi(
    val id: Long,
    val lat: Double,
    val lng: Double,
    val type: PoiType,
    val potential: Int,
) {
    val displayName: String
        get() = "${type.name} $potential"
}

enum class PoiType {
    MTB,
    ROAD,
}