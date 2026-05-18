package com.karoo.locationtagger.extension

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.util.Log
import com.karoo.locationtagger.MainActivity
import com.karoo.locationtagger.R
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.ViewConfig

/**
 * A DataType field that makes the extension visible in the Karoo ride page field picker.
 *
 * Displays "📍 Tag" on the ride page with a PendingIntent that opens MainActivity when tapped.
 * This provides an alternative to the BonusAction button for launching the app,
 * since BonusAction may not reliably trigger onBonusAction on all Karoo firmware versions.
 */
class PoiTagDataType(extension: String) : DataTypeImpl(extension, "poi-tag") {

    override fun startStream(emitter: Emitter<StreamState>) {
        Log.d(TAG, "startStream called for poi-tag")
        emitter.onNext(
            StreamState.Streaming(
                dataPoint = io.hammerhead.karooext.models.DataPoint(
                    dataTypeId = dataTypeId,
                    values = mapOf(DataType.Field.SINGLE to 0.0),
                )
            )
        )
    }

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        Log.d(TAG, "startView called for poi-tag")
        val remoteViews = RemoteViews(context.packageName, R.layout.poi_tag_field)

        // Add a PendingIntent so tapping this field opens the app
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.poi_tag_text, pendingIntent)

        emitter.updateView(remoteViews)
        emitter.setCancellable {
            Log.d(TAG, "startView cancelled for poi-tag")
        }
    }

    companion object {
        private const val TAG = "PoiTagDataType"
    }
}