@file:OptIn(kotlinx.coroutines.FlowPreview::class)

package com.karoo.locationtagger.ui

import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.karoo.locationtagger.data.GeoUtils
import com.karoo.locationtagger.data.Poi
import com.karoo.locationtagger.data.PoiRepository
import com.karoo.locationtagger.data.PoiType
import com.karoo.locationtagger.data.toGeoJsonFeatureCollection
import com.karoo.locationtagger.extension.consumerFlow
import com.karoo.locationtagger.network.MapUploader
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.OnLocationChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LocationState(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val heading: Double = 0.0,
    val hasFix: Boolean = false,
    val timestampMs: Long = 0L,
) {
    /** Age of this location fix in seconds. Returns null if no fix. */
    val ageSeconds: Long?
        get() = if (hasFix && timestampMs > 0) (System.currentTimeMillis() - timestampMs) / 1000 else null
}

data class PoiWithDistance(
    val poi: Poi,
    val distanceMeters: Double,
    val bearing: Double,
    val relativeDirection: Double,
)

sealed class UploadState {
    data object Idle : UploadState()
    data object Uploading : UploadState()
    data class Success(val url: String, val qrBitmap: Bitmap) : UploadState()
    data class Error(val message: String) : UploadState()
}

class MainViewModel(
    application: Application,
    private val karooSystem: KarooSystemService,
) : AndroidViewModel(application) {

    private companion object {
        const val TAG = "MainViewModel"
        const val STALE_THRESHOLD_MS = 10_000L // 10 seconds
    }

    private val repo = PoiRepository(application)

    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    val pois = repo.pois

    val poisWithDistance: StateFlow<List<PoiWithDistance>> = combine(
        pois,
        _locationState.debounce(300).distinctUntilChanged()
    ) { poiList, loc ->
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

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    // Android LocationManager as fallback GPS source
    private val androidLocationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val androidLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d(TAG, "Android LocationManager: lat=${location.latitude}, lng=${location.longitude}")
            updateLocation(location.latitude, location.longitude, location.bearing.toDouble())
        }

        @Deprecated("Required for API <29 compatibility")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private var isListeningAndroidLocation = false

    init {
        viewModelScope.launch {
            repo.initialize()
        }
        viewModelScope.launch {
            karooSystem.consumerFlow<OnLocationChanged>().collect { location ->
                Log.d(TAG, "Karoo OnLocationChanged: lat=${location.lat}, lng=${location.lng}")
                updateLocation(location.lat, location.lng, location.orientation ?: 0.0)
            }
        }
        // Also subscribe to Android GPS as fallback — KarooSystemService may be slow to connect
        startAndroidLocationUpdates()
    }

    private fun updateLocation(lat: Double, lng: Double, heading: Double) {
        _locationState.value = LocationState(
            lat = lat,
            lng = lng,
            heading = heading,
            hasFix = true,
            timestampMs = System.currentTimeMillis(),
        )
    }

    private fun startAndroidLocationUpdates() {
        try {
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
            )
            for (provider in providers) {
                if (androidLocationManager.isProviderEnabled(provider)) {
                    // Try to get last known location immediately
                    androidLocationManager.getLastKnownLocation(provider)?.let { loc ->
                        if (!_locationState.value.hasFix) {
                            Log.d(TAG, "Android lastKnownLocation: lat=${loc.latitude}, lng=${loc.longitude}")
                            updateLocation(loc.latitude, loc.longitude, loc.bearing.toDouble())
                        }
                    }
                    androidLocationManager.requestLocationUpdates(
                        provider, 1000L, 1f, androidLocationListener
                    )
                    isListeningAndroidLocation = true
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "No location permission for Android LocationManager", e)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start Android location updates", e)
        }
    }

    fun selectType(type: PoiType) {
        _selectedType.value = type
    }

    fun onLocationPermissionGranted() {
        if (!isListeningAndroidLocation) {
            startAndroidLocationUpdates()
        }
    }

    fun selectPotential(potential: Int) {
        _selectedPotential.value = potential
    }

    fun savePoi() {
        val loc = _locationState.value
        if (!loc.hasFix) return

        // Warn if location is stale (older than 10 seconds)
        val age = loc.ageSeconds
        if (age != null && age > 10) {
            _saveStatus.value = "⚠ GPS is ${age}s old — move to refresh"
            viewModelScope.launch {
                kotlinx.coroutines.delay(3000)
                _saveStatus.value = null
            }
            return
        }

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

    fun uploadPois() {
        if (poisWithDistance.value.isEmpty()) return

        _uploadState.value = UploadState.Uploading
        viewModelScope.launch {
            val currentPois = poisWithDistance.value.map { it.poi }
            val geoJson = currentPois.toGeoJsonFeatureCollection()
            val result = withContext(Dispatchers.IO) {
                MapUploader.uploadMap("Karoo POIs", geoJson)
            }

            if (result.isFailure) {
                val errorMsg = result.exceptionOrNull()?.message ?: "Upload failed"
                _uploadState.value = UploadState.Error(errorMsg)
                return@launch
            }

            val url = result.getOrThrow()
            val qrBitmap = generateQrBitmap(url)
            _uploadState.value = UploadState.Success(url, qrBitmap)
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }

    private suspend fun generateQrBitmap(content: String): Bitmap {
        return withContext(Dispatchers.Default) {
            val size = 512
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            bitmap
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (isListeningAndroidLocation) {
            androidLocationManager.removeUpdates(androidLocationListener)
        }
    }

    class Factory(
        private val karooSystem: KarooSystemService,
        private val application: Application,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(application, karooSystem) as T
        }
    }
}