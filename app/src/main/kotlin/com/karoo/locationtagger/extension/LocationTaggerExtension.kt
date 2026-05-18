package com.karoo.locationtagger.extension

import android.content.Intent
import com.karoo.locationtagger.MainActivity
import io.hammerhead.karooext.extension.KarooExtension

class LocationTaggerExtension : KarooExtension("karoo-location-tagger", "1.0") {

    override fun onBonusAction(actionId: String) {
        when (actionId) {
            "open-location-tagger" -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }
}