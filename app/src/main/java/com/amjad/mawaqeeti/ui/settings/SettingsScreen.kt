package com.amjad.mawaqeeti.ui.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lavalamp.LavaLamp
import com.example.lavalamp.LavaMode
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.amjad.mawaqeeti.R
import com.amjad.mawaqeeti.util.LocaleUtils
import com.amjad.mawaqeeti.widget.PrayerWidgetReceiver
import com.example.lavalamp.LavaContainerMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val city by viewModel.selectedCity.collectAsState()
    val country by viewModel.selectedCountry.collectAsState()
    val method by viewModel.selectedMethod.collectAsState()
    val lavaMode by viewModel.lavaMode.collectAsState()
    val languageCode by viewModel.languageCode.collectAsState()

    val fOff by viewModel.fajrOffset.collectAsState()
    val dOff by viewModel.dhuhrOffset.collectAsState()
    val aOff by viewModel.asrOffset.collectAsState()
    val mOff by viewModel.maghribOffset.collectAsState()
    val iOff by viewModel.ishaOffset.collectAsState()

    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val adhanEnabled by viewModel.adhanEnabled.collectAsState()

    var tempCity by remember(city) { mutableStateOf(city) }
    var tempCountry by remember(country) { mutableStateOf(country) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF03070C))) {
        // Subtle Background Animation
        LavaLamp(
            containerMode = LavaContainerMode.AMBIENT_BACKGROUND ,// No glass bottle,
            modifier = Modifier.fillMaxSize().alpha(0.2f),
            speed = 0.15f
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                    title = { Text(stringResource(R.string.settings_title), color = Color.White, fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Location Section
                SettingsSection(title = stringResource(R.string.location_section), icon = Icons.Default.LocationOn) {
                    ModernTextField(value = tempCity, onValueChange = { tempCity = it }, label = stringResource(R.string.city_label))
                    Spacer(Modifier.height(12.dp))
                    ModernTextField(value = tempCountry, onValueChange = { tempCountry = it }, label = stringResource(R.string.country_label))
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.updateLocation(tempCity, tempCountry, method) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64FFDA))
                    ) {
                        Text(stringResource(R.string.save_changes), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                // Offsets Section
                SettingsSection(title = stringResource(R.string.offsets_section), icon = Icons.Default.Schedule) {
                    OffsetItem(stringResource(R.string.prayer_fajr), fOff) { viewModel.updateOffset("Fajr", it) }
                    OffsetItem(stringResource(R.string.prayer_dhuhr), dOff) { viewModel.updateOffset("Dhuhr", it) }
                    OffsetItem(stringResource(R.string.prayer_asr), aOff) { viewModel.updateOffset("Asr", it) }
                    OffsetItem(stringResource(R.string.prayer_maghrib), mOff) { viewModel.updateOffset("Maghrib", it) }
                    OffsetItem(stringResource(R.string.prayer_isha), iOff) { viewModel.updateOffset("Isha", it) }
                }

                // Performance Section
                SettingsSection(title = stringResource(R.string.performance_section), icon = Icons.Default.AutoAwesome) {
                    val modes = listOf("FULL", "BALANCED", "BATTERY_SAVER")
                    modes.forEach { mode ->
                        val isSelected = lavaMode == mode
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.setLavaMode(mode) },
                            color = if (isSelected) Color(0xFF64FFDA).copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) BorderStroke(1.dp, Color(0xFF64FFDA).copy(alpha = 0.3f)) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setLavaMode(mode) },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF64FFDA))
                                )
                                Text(
                                    text = when(mode) {
                                        "FULL" -> stringResource(R.string.mode_full)
                                        "BALANCED" -> stringResource(R.string.mode_balanced)
                                        else -> stringResource(R.string.mode_saver)
                                    },
                                    color = if (isSelected) Color(0xFF64FFDA) else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Notifications Section
                SettingsSection(title = stringResource(R.string.notifications_section), icon = Icons.Default.Notifications) {
                    ToggleSettingItem(
                        title = stringResource(R.string.enable_notifications),
                        subtitle = stringResource(R.string.enable_notifications_sub),
                        checked = notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                    )
                    Spacer(Modifier.height(12.dp))
                    ToggleSettingItem(
                        title = stringResource(R.string.enable_adhan),
                        subtitle = stringResource(R.string.enable_adhan_sub),
                        checked = adhanEnabled,
                        onCheckedChange = { viewModel.setAdhanEnabled(it) }
                    )
                }

                // Widget Management Section
                val context = LocalContext.current
                SettingsSection(title = stringResource(R.string.widget_section), icon = Icons.Default.Widgets) {
                    Text(
                        stringResource(R.string.widget_desc),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = { requestPinAppWidget(context) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.add_widget), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Language Section
                SettingsSection(title = stringResource(R.string.language_section), icon = Icons.Default.Language) {
                    Text(
                        stringResource(R.string.language_restart_note),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    listOf("en" to stringResource(R.string.language_english),
                           "ar" to stringResource(R.string.language_arabic)).forEach { (code, label) ->
                        val isSelected = languageCode == code
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.setLanguageCode(code)
                                    LocaleUtils.restartApp(context)
                                },
                            color = if (isSelected) Color(0xFF64FFDA).copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) BorderStroke(1.dp, Color(0xFF64FFDA).copy(alpha = 0.3f)) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setLanguageCode(code)
                                        LocaleUtils.restartApp(context)
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF64FFDA))
                                )
                                Text(
                                    text = label,
                                    color = if (isSelected) Color(0xFF64FFDA) else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

fun requestPinAppWidget(context: Context) {
    val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
    val myProvider = ComponentName(context, PrayerWidgetReceiver::class.java)

    if (appWidgetManager.isRequestPinAppWidgetSupported) {
        appWidgetManager.requestPinAppWidget(myProvider, null, null)
    } else {
        Toast.makeText(context, context.getString(R.string.widget_not_supported), Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun SettingsSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFF64FFDA).copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = title, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(Modifier.height(16.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

@Composable
fun ModernTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.4f)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedBorderColor = Color(0xFF64FFDA),
            unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            cursorColor = Color(0xFF64FFDA),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

@Composable
fun OffsetItem(label: String, value: Int, onUpdate: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onUpdate(value - 1) },
                modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape).size(36.dp)
            ) { Icon(Icons.Default.Remove, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
            
            Text(
                text = "${if (value >= 0) "+" else ""}$value د",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = if (value != 0) Color(0xFF64FFDA) else Color.White,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(
                onClick = { onUpdate(value + 1) },
                modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape).size(36.dp)
            ) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
fun ToggleSettingItem(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF64FFDA),
                checkedTrackColor = Color(0xFF64FFDA).copy(alpha = 0.3f),
                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}
