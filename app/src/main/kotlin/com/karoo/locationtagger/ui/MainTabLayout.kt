package com.karoo.locationtagger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karoo.locationtagger.data.PoiType

enum class Tab(val label: String) {
    NewEntry("Tag"),
    SavedPois("Points"),
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
        // Browser-style tab bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                .height(44.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Tab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val bgColor = if (isSelected)
                    MaterialTheme.colorScheme.background
                else
                    MaterialTheme.colorScheme.surfaceVariant
                val contentColor = if (isSelected)
                    MaterialTheme.colorScheme.onBackground
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(
                            RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                        )
                        .background(bgColor)
                        .clickable { selectedTab = tab },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = contentColor,
                    )
                }
            }
        }

        // GPS status indicator — icon + text, never color alone
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (locationState.hasFix) "📍 GPS ready" else "⏳ Acquiring GPS",
                color = if (locationState.hasFix) Color(0xFF4CAF50) else Color(0xFFFFA000),
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