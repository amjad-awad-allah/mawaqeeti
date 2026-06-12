package com.amjad.mawaqeeti.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amjad.mawaqeeti.data.model.PrayerTime
import com.example.lavalamp.LavaContainerMode
import com.example.lavalamp.LavaLamp
import com.example.lavalamp.LavaLampStyle
import com.example.lavalamp.LavaMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    onTestClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is HomeViewModel.HomeEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }
    
    val (speed, particles, flowIntensity) = when (uiState.lavaMode) {
        "FULL" -> Triple(0.6f, true, 0.6f)
        "BATTERY_SAVER" -> Triple(0.2f, false, 0.1f)
        else -> Triple(0.4f, false, 0.4f) // BALANCED
    }

    val lavaColors = when (uiState.progressCount) {
        in 0..2 -> listOf(Color(0xFF0D1B2A), Color(0xFF1B263B)) // Calm Blue/Indico
        in 3..4 -> listOf(Color(0xFFE65100), Color(0xFFFF9800)) // Encouraging Orange
        else -> listOf(Color(0xFF2E7D32), Color(0xFFFFD700)) // Glorious Green/Gold for 5/5
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF03070C))) {
        // Universal Liquid Background - Reactive to mode and progress
        // lavalamb
        LavaLamp(
            containerMode = LavaContainerMode.AMBIENT_BACKGROUND ,// No glass bottle,
            modifier = Modifier.fillMaxSize().alpha(0.3f),
            speed = speed,
        )

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                    title = {
                        Text(
                            "مواقيتي",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = onTestClick,
                            modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                        ) {
                            Icon(Icons.Default.BugReport, "Test", tint = Color.White)
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                        ) {
                            Icon(Icons.Default.Settings, "Settings", tint = Color.White)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // The Master Liquid Card with Obstacle Deflection
                Box(modifier = Modifier.padding(vertical = 16.dp)) {
                    LiquidCountdownCard(uiState)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Prayer List Headers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الصلوات المفروضة", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("اليوم", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                uiState.prayers.forEach { prayer ->
                    ModernLiquidPrayerItem(
                        prayer = prayer,
                        isNext = uiState.nextPrayer?.name == prayer.name,
                        onPrayed = { viewModel.togglePrayer(prayer.name, !prayer.isPrayed) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (uiState.showCompletionOverlay) {
            CompletionOverlay(onDismiss = { viewModel.dismissCompletionOverlay() })
        }
    }
}

@Composable
fun LiquidCountdownCard(uiState: HomeUiState) {
    val progressColors = getColorsForProgress(uiState.progressCount)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(40.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(40.dp))
    ) {
        // INTERNAL LIQUID - Acting as the "Soul" of the card
        // lavalamb
        LavaLamp(
            containerMode = LavaContainerMode.AMBIENT_BACKGROUND ,// No glass bottle,
            modifier = Modifier.fillMaxSize(),
            speed = 0.6f
        )
        
        // Glassy Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = uiState.nextPrayer?.let { "الصلاة القادمة: ${it.name}" } ?: "تحميل...",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = uiState.timeLeft,
                style = TextStyle(
                    fontWeight = FontWeight.Black,
                    fontSize = 72.sp,
                    letterSpacing = (-2).sp
                ),
                color = Color.White
            )
        }
    }
}

// QuickStatsSection and StatCard removed temporarily as requested

@Composable
fun ModernLiquidPrayerItem(prayer: PrayerTime, isNext: Boolean, onPrayed: () -> Unit) {
    val cardAlpha = if (prayer.isPrayed) 0.4f else 1f
    val accentColor = if (isNext) Color(0xFF64FFDA) else Color.White
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isNext) Color.White.copy(alpha = 0.05f) else Color.Transparent)
            .clickable { onPrayed() }
            .alpha(cardAlpha)
    ) {
        // If it's the next prayer, add a subtle liquid pulse at the side
        if (isNext) {
            Box(modifier = Modifier.fillMaxHeight().width(6.dp).background(Color(0xFF64FFDA), RoundedCornerShape(bottomStart = 24.dp, topStart = 24.dp)))
        }

        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    color = if (prayer.isPrayed) Color(0xFF64FFDA).copy(alpha = 0.2f) else accentColor.copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (prayer.isPrayed) {
                            Icon(Icons.Default.Done, null, tint = Color(0xFF64FFDA), modifier = Modifier.size(20.dp))
                        } else {
                            Text(prayer.name.take(1), color = accentColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(prayer.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal)
                    Text(prayer.time, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                }
            }
            
            if (isNext && !prayer.isPrayed) {
                Surface(
                    color = Color(0xFF64FFDA).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("الآن", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = Color(0xFF64FFDA), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun getColorsForProgress(count: Int): List<Color> {
    return when {
        count < 2 -> listOf(Color(0xFF0D47A1), Color(0xFF42A5F5)) // Blue deep
        count < 4 -> listOf(Color(0xFF4A148C), Color(0xFFAB47BC)) // Purple deep
        count < 5 -> listOf(Color(0xFFE65100), Color(0xFFFFB74D)) // Orange deep
        else -> listOf(Color(0xFF1B5E20), Color(0xFF66BB6A)) // Green success
    }
}

@Composable
fun CompletionOverlay(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(240.dp).clip(CircleShape)) {
                // lavalamb
                LavaLamp(
                    containerMode = LavaContainerMode.AMBIENT_BACKGROUND ,// No glass bottle,
                    modifier = Modifier.fillMaxSize(),
                    speed = 1f
                )
                Icon(
                    Icons.Default.EmojiEvents, 
                    null, 
                    tint = Color.White, 
                    modifier = Modifier.size(100.dp).align(Alignment.Center)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text("🎉 ما شاء الله", style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Bold)
            Text("أتممت جميع صلوات اليوم بنجاح", color = Color.White.copy(alpha = 0.7f))
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64FFDA)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(56.dp).padding(horizontal = 32.dp)
            ) {
                Text("الحمد لله", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}
