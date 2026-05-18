package com.karoo.locationtagger.extension

import android.content.Intent
import android.util.Log
import io.hammerhead.karooext.extension.KarooExtension

class LocationTaggerExtension : KarooExtension("karoo-location-tagger", "1.0") {

    override val types by lazy {
        listOf(PoiTagDataType("karoo-location-tagger"))
    }

    override fun onBonusAction(actionId: String) {
        Log.d(TAG, "onBonusAction called: $actionId")
        when (actionId) {
            "open-location-tagger" -> {
                // Use broadcast to launch activity — avoids Android 10+
                // background activity launch restriction that makes direct
                // startActivity from a Service context unreliable.
                val intent = Intent(ACTION_OPEN_APP).apply {
                    setPackage(packageName)
                    addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                }
                sendBroadcast(intent)
                Log.d(TAG, "Broadcast sent for open-location-tagger")
            }
            else -> Log.w(TAG, "Unknown actionId: $actionId")
        }
    }

    companion object {
        private const val TAG = "LocationTagger"
        const val ACTION_OPEN_APP = "com.karoo.locationtagger.OPEN_APP"
    }
}