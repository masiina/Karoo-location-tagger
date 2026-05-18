package com.karoo.locationtagger.extension

import android.content.Intent
import com.karoo.locationtagger.MainActivity
import io.hammerhead.karooext.extension.KarooExtension

class LocationTaggerExtension : KarooExtension("karoo-location-tagger", "1.0") {

    override val types by lazy {
        listOf(
            PoiTagDataType("karoo-location-tagger")
        )
    }

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