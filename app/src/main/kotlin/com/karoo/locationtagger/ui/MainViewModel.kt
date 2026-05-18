package com.karoo.locationtagger.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.karoo.locationtagger.data.GeoUtils
import com.karoo.locationtagger.data.Poi
import com.karoo.locationtagger.data.PoiRepository
import com.karoo.locationtagger.data.PoiType
import com.karoo.locationtagger.extension.consumerFlow
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.OnLocationChanged
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LocationState(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val heading: Double = 0.0,
    val hasFix: Boolean = false,
)

data class PoiWithDistance(
    val poi: Poi,
    val distanceMeters: Double,
    val bearing: Double,
    val relativeDirection: Double,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = PoiRepository(application)
    private val karooSystem = KarooSystemService(application)

    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    val pois = repo.pois

    val poisWithDistance: StateFlow<List<PoiWithDistance>> = combine(pois, _locationState) { poiList, loc ->
        if (!loc.hasFix) {
            poiList.map { PoiWithDistance(it, 0.0, 0.0, 0.0) }
        } else {
            poiList.map { poi ->
                val dist = GeoUtils.distanceMeters(loc.lat, loc.lng, poi.lat, poi.lng)
                val bearing = GeoUtils.bearingDegrees(loc.lat, loc.lng, poi.lat, poi.lng)
                val relDir = GeoUtils.relativeDirection(bearing, loc.heading)
                PoiWithDistance(poi, dist, bearing, relDir)
            }.sortedBy { it.distanceMeters }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedType = MutableStateFlow(PoiType.MTB)
    val selectedType: StateFlow<PoiType> = _selectedType.asStateFlow()

    private val _selectedPotential = MutableStateFlow(1)
    val selectedPotential: StateFlow<Int> = _selectedPotential.asStateFlow()

    private val _saveStatus = MutableStateFlow<String?>(null)
    val saveStatus: StateFlow<String?> = _saveStatus.asStateFlow()

    init {
        karooSystem.connect { }
        viewModelScope.launch {
            karooSystem.consumerFlow<OnLocationChanged>().collect { location ->
                _locationState.value = LocationState(
                    lat = location.lat,
                    lng = location.lng,
                    heading = location.orientation ?: 0.0,
                    hasFix = true,
                )
            }
        }
    }

    fun selectType(type: PoiType) {
        _selectedType.value = type
    }

    fun selectPotential(potential: Int) {
        _selectedPotential.value = potential
    }

    fun savePoi() {
        val loc = _locationState.value
        if (!loc.hasFix) return

        viewModelScope.launch {
            val id = repo.nextId()
            val poi = Poi(
                id = id,
                lat = loc.lat,
                lng = loc.lng,
                type = _selectedType.value,
                potential = _selectedPotential.value,
            )
            repo.addPoi(poi)
            _saveStatus.value = "POI saved!"
            kotlinx.coroutines.delay(2000)
            _saveStatus.value = null
        }
    }

    fun removePoi(id: Long) {
        viewModelScope.launch {
            repo.removePoi(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        karooSystem.disconnect()
    }
}