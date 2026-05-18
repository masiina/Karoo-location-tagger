package com.karoo.locationtagger.extension

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.karoo.locationtagger.MainActivity

class OpenAppReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "OpenAppReceiver onReceive: ${intent.action}")
        if (intent.action == LocationTaggerExtension.ACTION_OPEN_APP) {
            val activityIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        or Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
            context.startActivity(activityIntent)
            Log.d(TAG, "MainActivity launched from BroadcastReceiver")
        }
    }

    companion object {
        private const val TAG = "LocationTagger"
    }
}