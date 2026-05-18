package com.karoo.locationtagger.extension

import android.content.Context
import android.widget.RemoteViews
import com.karoo.locationtagger.R
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataType
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.ViewConfig

/**
 * A minimal DataType that makes the extension visible in the Karoo ride page field picker.
 *
 * This field displays "📍 Tag" on the ride page using a RemoteViews layout.
 * The actual tap-to-open behavior is handled by the BonusAction
 * ("Tag Location") which appears as a separate action button in the field picker.
 *
 * This DataType exists because the Karoo system requires at least one DataType
 * for an extension to appear in the field picker menu.
 */
class PoiTagDataType(extension: String) : DataTypeImpl(extension, "poi-tag") {

    override fun startStream(emitter: Emitter<StreamState>) {
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
        val remoteViews = RemoteViews(context.packageName, R.layout.poi_tag_field)
        emitter.updateView(remoteViews)
        emitter.setCancellable { /* no-op */ }
    }
}