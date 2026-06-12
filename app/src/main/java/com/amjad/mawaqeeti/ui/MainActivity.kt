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
import androidx.compose.runtime.Composable
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkAndRequestPermissions()
        requestOverlayPermission()

        val isTV = isRunningOnTV()
        val startDestination = intent.getStringExtra("START_DESTINATION") ?: "splash"

        setContent {
            ThemeMawaqeeti {
                MawaqeetiNavigation(isTV = isTV, startDestination = startDestination)
            }
        }
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