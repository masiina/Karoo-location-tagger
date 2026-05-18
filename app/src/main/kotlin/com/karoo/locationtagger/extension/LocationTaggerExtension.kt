package com.karoo.locationtagger.extension

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
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
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to start activity, may need SYSTEM_ALERT_WINDOW permission", e)
                    // On Android 10+ background activity launches are restricted.
                    // If startActivity fails, direct the user to grant overlay permission.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && !Settings.canDrawOverlays(this)) {
                        val permIntent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            android.net.Uri.parse("package:$packageName")
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(permIntent)
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "LocationTagger"
    }
}