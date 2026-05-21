package com.karoo.locationtagger

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.karoo.locationtagger.theme.AppTheme
import com.karoo.locationtagger.ui.MainTabLayout
import com.karoo.locationtagger.ui.MainViewModel
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.LaunchPinDrop
import io.hammerhead.karooext.models.Symbol
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val karooSystem by lazy { KarooSystemService(this) }
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(karooSystem, application)
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.onLocationPermissionGranted()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        karooSystem.connect { }

        requestLocationPermissionsIfNeeded()

        // Collect navigation events from ViewModel
        lifecycleScope.launch {
            viewModel.navigateEvents.collect { (lat, lng, name) ->
                dispatchPinDrop(lat, lng, name)
            }
        }

        setContent {
            AppTheme {
                MainTabLayout(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        karooSystem.connect { }
        requestLocationPermissionsIfNeeded()
    }

    override fun onStop() {
        karooSystem.disconnect()
        super.onStop()
    }

    private fun requestLocationPermissionsIfNeeded() {
        val fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fineLocation != PackageManager.PERMISSION_GRANTED &&
            coarseLocation != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        } else {
            viewModel.onLocationPermissionGranted()
        }
    }

    fun dispatchPinDrop(lat: Double, lng: Double, name: String) {
        karooSystem.dispatch(
            LaunchPinDrop(
                Symbol.POI(
                    id = "poi-$lat-$lng",
                    lat = lat,
                    lng = lng,
                    type = "generic",
                    name = name,
                )
            )
        )
    }
}