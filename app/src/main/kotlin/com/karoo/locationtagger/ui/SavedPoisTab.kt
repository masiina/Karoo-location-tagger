package com.karoo.locationtagger.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karoo.locationtagger.MainActivity
import com.karoo.locationtagger.data.GeoUtils

@Composable
fun SavedPoisTab(viewModel: MainViewModel) {
    val poisWithDistance by viewModel.poisWithDistance.collectAsState()
    val locationState by viewModel.locationState.collectAsState()
    val context = LocalContext.current

    if (poisWithDistance.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No POIs saved yet",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        poisWithDistance.forEach { poiWithDist ->
            val poi = poiWithDist.poi

            PoiCard(
                poiWithDistance = poiWithDist,
                hasGpsFix = locationState.hasFix,
                onNavigate = {
                    (context as? MainActivity)?.dispatchPinDrop(
                        lat = poi.lat,
                        lng = poi.lng,
                        name = poi.displayName,
                    )
                },
                onDelete = { viewModel.removePoi(poi.id) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun PoiCard(
    poiWithDistance: PoiWithDistance,
    hasGpsFix: Boolean,
    onNavigate: () -> Unit,
    onDelete: () -> Unit,
) {
    val poi = poiWithDistance.poi
    val distText = if (hasGpsFix) GeoUtils.formatDistance(poiWithDistance.distanceMeters) else "—"
    val dirArrow = if (hasGpsFix) GeoUtils.directionArrow(poiWithDistance.relativeDirection) else "—"
    val dirLabel = if (hasGpsFix) GeoUtils.formatDirection(poiWithDistance.relativeDirection) else "—"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${poi.type.name} — Potential ${poi.potential}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(4.dp))
                ) {
                    Text("✕", fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
                }
            }

            Text(
                text = "%.5f, %.5f".format(poi.lat, poi.lng),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dirArrow,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = distText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dirLabel,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Button(
                    onClick = onNavigate,
                    enabled = hasGpsFix,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                ) {
                    Text("Navigate", fontSize = 14.sp)
                }
            }
        }
    }
}