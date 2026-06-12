package com.amjad.mawaqeeti.ui

import android.Manifest
import android.app.AlarmManager
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.*
import com.amjad.mawaqeeti.ui.home.HomeScreen
import com.amjad.mawaqeeti.ui.home.TvHomeScreen
import com.amjad.mawaqeeti.ui.home.TvAdhanScreen
import com.amjad.mawaqeeti.ui.settings.SettingsScreen
import com.amjad.mawaqeeti.ui.splash.SplashScreen
import com.amjad.mawaqeeti.ui.theme.ThemeMawaqeeti
import com.amjad.mawaqeeti.ui.test.TestScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle results if needed
    }

    private var showOverlayDialog by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkAndRequestPermissions()
        
        // Check if we need to show the permission dialog (ONLY FOR TV)
        val isTV = isRunningOnTV()
        if (isTV && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            showOverlayDialog = true
        }

        val startDestination = intent.getStringExtra("START_DESTINATION") ?: "splash"

        setContent {
            ThemeMawaqeeti {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MawaqeetiNavigation(isTV = isTV, startDestination = startDestination)

                    if (showOverlayDialog) {
                        PermissionDialog(
                            onDismiss = { showOverlayDialog = false },
                            onConfirm = {
                                showOverlayDialog = false
                                requestOverlayPermission()
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun PermissionDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "تصريح التنبيه التلقائي 🕌",
                    color = Color(0xFF64FFDA),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "لكي يتمكن التطبيق من رفع الأذان وعرض المواقيت تلقائياً حتى أثناء استخدامك لتطبيقات أخرى، نحتاج منك السماح بـ 'الظهور فوق التطبيقات'. \n\nهذا يضمن عدم تفويت أي صلاة بإذن الله.",
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64FFDA))
                ) {
                    Text("اذهب للإعدادات ✅", color = Color(0xFF0D1B2A))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("ليس الآن", color = Color.White.copy(alpha = 0.5f))
                }
            },
            containerColor = Color(0xFF0D1B2A),
            shape = RoundedCornerShape(24.dp)
        )
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }
    }

    private fun isRunningOnTV(): Boolean {
        val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }

        // Check for exact alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }
}

@Composable
fun MawaqeetiNavigation(isTV: Boolean, startDestination: String = "splash") {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable("splash") {
            SplashScreen(onInitializationComplete = {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("home") {
            if (isTV) {
                TvHomeScreen(
                    onSettingsClick = {
                        navController.navigate("settings")
                    },
                    onTestClick = {
                        navController.navigate("test_screen")
                    }
                )
            } else {
                HomeScreen(
                    onSettingsClick = {
                        navController.navigate("settings")
                    },
                    onTestClick = {
                        navController.navigate("test_screen")
                    }
                )
            }
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable("test_screen") {
            TestScreen(onBack = { navController.popBackStack() })
        }
        composable("adhan_reminder/{prayerName}") { backStackEntry ->
            val prayerName = backStackEntry.arguments?.getString("prayerName") ?: ""
            TvAdhanScreen(
                prayerName = prayerName,
                onClose = { 
                    if (!navController.popBackStack()) {
                        navController.navigate("home") {
                            popUpTo("adhan_reminder/{prayerName}") { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}