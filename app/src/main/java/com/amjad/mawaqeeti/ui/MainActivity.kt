package com.amjad.mawaqeeti.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.amjad.mawaqeeti.ui.home.HomeScreen
import com.amjad.mawaqeeti.ui.settings.SettingsScreen
import com.amjad.mawaqeeti.ui.splash.SplashScreen
import com.amjad.mawaqeeti.ui.theme.ThemeMawaqeeti
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThemeMawaqeeti {
                MawaqeetiNavigation()
            }
        }
    }
}

@Composable
fun MawaqeetiNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onInitializationComplete = {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("home") {
            HomeScreen(
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}