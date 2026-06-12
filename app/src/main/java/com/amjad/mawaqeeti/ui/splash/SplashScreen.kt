package com.amjad.mawaqeeti.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lavalamp.LavaContainerMode
import com.example.lavalamp.LavaLamp
import com.example.lavalamp.LavaMode

@Composable
fun SplashScreen(
    onInitializationComplete: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val isReady by viewModel.isReady.collectAsState()

    // WOW Animations
    val transitionState = remember { MutableTransitionState(false) }.apply { targetState = true }
    val transition = updateTransition(transitionState, label = "SplashTransition")
    
    val scale by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow) },
        label = "Scale"
    ) { if (it) 1f else 0.8f }
    
    val alpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 1500) },
        label = "Alpha"
    ) { if (it) 1f else 0f }

    LaunchedEffect(isReady) {
        if (isReady) {
            // Keep the eye-candy for a bit longer if initialization was too fast
            kotlinx.coroutines.delay(1200)
            onInitializationComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF03070C))) {
        // High-Quality Ambient Liquid
        // lavalamb
        LavaLamp(
            modifier = Modifier.fillMaxSize(),
            containerMode = LavaContainerMode.AMBIENT_BACKGROUND ,// No glass bottle,
            speed = 0.3f,
            flowIntensity = 0.2f,

        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .alpha(alpha),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mawaqeeti",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                ),
                color = Color.White
            )
            Text(
                text = "مواقيتي",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp
                ),
                color = Color(0xFF64FFDA).copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFF64FFDA),
                strokeWidth = 2.dp
            )
        }
    }
}
