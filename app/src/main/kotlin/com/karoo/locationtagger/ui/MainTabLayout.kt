package com.karoo.locationtagger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karoo.locationtagger.data.PoiType

enum class Tab {
    NewEntry,
    SavedPois,
}

@Composable
fun MainTabLayout(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(Tab.NewEntry) }
    val locationState by viewModel.locationState.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Tab.entries.forEach { tab ->
                Button(
                    onClick = { selectedTab = tab },
                    colors = if (selectedTab == tab)
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    else
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = when (tab) {
                            Tab.NewEntry -> "New Entry"
                            Tab.SavedPois -> "Saved POIs"
                        },
                        fontSize = 14.sp,
                    )
                }
            }
        }

        // GPS status indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (locationState.hasFix) "GPS ●" else "GPS ○",
                color = if (locationState.hasFix) Color(0xFF4CAF50) else Color(0xFFF44336),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Save status toast
        if (saveStatus != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = saveStatus!!,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 14.sp,
                )
            }
        }

        // Tab content — scrollable
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                Tab.NewEntry -> NewEntryTab(viewModel)
                Tab.SavedPois -> SavedPoisTab(viewModel)
            }
        }
    }
}