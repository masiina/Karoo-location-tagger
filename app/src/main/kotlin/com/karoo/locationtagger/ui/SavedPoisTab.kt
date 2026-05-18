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
import kotlinx.coroutines.delay

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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
    var confirmDelete by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (confirmDelete) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Type + dots + direction
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${poi.type.name} ${"●".repeat(poi.potential)}${"○".repeat(3 - poi.potential)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dirArrow,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (hasGpsFix) distText else "No GPS",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Delete / Confirm
            if (confirmDelete) {
                IconButton(
                    onClick = {
                        onDelete()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("✓", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            } else {
                IconButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(4.dp))
                ) {
                    Text("✕", fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                }
            }

            // Navigate
            Button(
                onClick = {
                    confirmDelete = false
                    onNavigate()
                },
                enabled = hasGpsFix,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text("Go", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Auto-reset confirm state after 3 seconds
    LaunchedEffect(confirmDelete) {
        if (confirmDelete) {
            delay(3000)
            confirmDelete = false
        }
    }
}