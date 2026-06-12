package com.amjad.mawaqeeti.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import com.amjad.mawaqeeti.data.model.PrayerTime
import com.example.lavalamp.LavaContainerMode
import com.example.lavalamp.LavaLamp

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvHomeScreen(
    onSettingsClick: () -> Unit,
    onTestClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF03070C))) {
        // Full screen ambient liquid
        LavaLamp(
            modifier = Modifier.fillMaxSize(),
            containerMode = LavaContainerMode.AMBIENT_BACKGROUND,
            speed = 0.5f
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Next Prayer & Countdown
            Column(
                modifier = Modifier.weight(1.5f),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "مواقيتي",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    
                    Spacer(modifier = Modifier.width(32.dp))
                    
                    // Settings Button for TV
                    Surface(
                        onClick = onSettingsClick,
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color.White.copy(alpha = 0.05f),
                            focusedContainerColor = Color.White.copy(alpha = 0.2f)
                        ),
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp))
                    ) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            Text("الإعدادات ⚙️", color = Color.White, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Test Lab Button for TV
                    Surface(
                        onClick = onTestClick,
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color(0xFF64FFDA).copy(alpha = 0.1f),
                            focusedContainerColor = Color(0xFF64FFDA).copy(alpha = 0.3f)
                        ),
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp))
                    ) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            Text("مختبر الاختبار 🧪", color = Color.White, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Surface(
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
                    onClick = {}
                ) {
                    Text(
                        text = uiState.nextPrayer?.let { "الصلاة القادمة: ${it.name}" } ?: "جاري التحميل...",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = uiState.timeLeft,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 120.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = Color.White
                )
                
                Text(
                    text = "متبقي على رفع الأذان",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Right Side: Prayer List
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(32.dp)),
                colors = SurfaceDefaults.colors(containerColor = Color.Black.copy(alpha = 0.4f))
            ) {
                LazyColumn(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "صلوات اليوم",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(uiState.prayers) { prayer ->
                        TvPrayerItem(
                            prayer = prayer,
                            isNext = uiState.nextPrayer?.name == prayer.name
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvPrayerItem(prayer: PrayerTime, isNext: Boolean) {
    val backgroundColor = if (isNext) Color(0xFF64FFDA).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
    val borderColor = if (isNext) Color(0xFF64FFDA) else Color.Transparent

    Surface(
        onClick = {},
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = backgroundColor,
            focusedContainerColor = Color.White.copy(alpha = 0.2f)
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = prayer.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = prayer.time,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            
            if (prayer.isPrayed) {
                Text("✅", fontSize = 24.sp)
            } else if (isNext) {
                Text("🌙", fontSize = 24.sp, color = Color(0xFF64FFDA))
            }
        }
    }
}
