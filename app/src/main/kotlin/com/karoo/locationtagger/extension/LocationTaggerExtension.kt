package com.karoo.locationtagger.extension

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.karoo.locationtagger.MainActivity
import com.karoo.locationtagger.R
import io.hammerhead.karooext.extension.KarooExtension

class LocationTaggerExtension : KarooExtension("karoo-location-tagger", "1.0") {

    override val types by lazy {
        Log.d(TAG, "Creating types list")
        listOf(
            PoiTagDataType("karoo-location-tagger")
        )
    }

    override fun onBonusAction(actionId: String) {
        Log.d(TAG, "onBonusAction called with actionId: $actionId")
        when (actionId) {
            "open-location-tagger" -> openMainActivity()
            else -> Log.w(TAG, "Unknown actionId: $actionId")
        }
    }

    private fun openMainActivity() {
        Log.d(TAG, "openMainActivity called")
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }

        // Try direct launch first
        try {
            startActivity(intent)
            Log.d(TAG, "Direct startActivity succeeded")
            return
        } catch (e: Exception) {
            Log.w(TAG, "Direct startActivity failed, trying notification approach", e)
        }

        // Fallback: use a full-screen notification to launch the activity.
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Location Tagger",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Tag Location")
                .setContentText("Tap to open Location Tagger")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setFullScreenIntent(pendingIntent, true)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Notification posted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Notification approach also failed", e)
        }
    }

    companion object {
        private const val TAG = "LocationTagger"
        private const val CHANNEL_ID = "location_tagger_action"
        private const val NOTIFICATION_ID = 1001
    }
}