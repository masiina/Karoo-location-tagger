package com.karoo.locationtagger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.karoo.locationtagger.theme.AppTheme
import com.karoo.locationtagger.ui.MainTabLayout
import com.karoo.locationtagger.ui.MainViewModel
import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.models.LaunchPinDrop
import io.hammerhead.karooext.models.Symbol

class MainActivity : ComponentActivity() {

    private val karooSystem by lazy { KarooSystemService(this) }
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(karooSystem, application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        karooSystem.connect { }

        setContent {
            AppTheme {
                MainTabLayout(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        karooSystem.connect { }
    }

    override fun onStop() {
        karooSystem.disconnect()
        super.onStop()
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