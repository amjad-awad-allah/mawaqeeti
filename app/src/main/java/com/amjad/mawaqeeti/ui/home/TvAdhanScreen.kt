package com.amjad.mawaqeeti.ui.home

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.amjad.mawaqeeti.R
import com.example.lavalamp.LavaContainerMode
import com.example.lavalamp.LavaLamp

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvAdhanScreen(
    prayerName: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    
    // Play Adhan sound immediately when screen opens
    val mediaPlayer = remember { 
        MediaPlayer.create(context, R.raw.athan).apply {
            isLooping = false
        }
    }

    DisposableEffect(Unit) {
        mediaPlayer.start()
        onDispose {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF03070C))) {
        // High intensity lava lamp for adhan
        LavaLamp(
            modifier = Modifier.fillMaxSize(),
            containerMode = LavaContainerMode.AMBIENT_BACKGROUND,
            speed = 0.8f
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "حان الآن موعد أذان",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Text(
                text = prayerName,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 150.sp,
                    fontWeight = FontWeight.Black
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = onClose,
                modifier = Modifier.padding(16.dp),
                colors = ButtonDefaults.colors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = Color.White.copy(alpha = 0.3f)
                ),
                shape = ButtonDefaults.shape(androidx.compose.foundation.shape.CircleShape)
            ) {
                Text(
                    text = "إغلاق التنبيه",
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }
        }
    }
}
