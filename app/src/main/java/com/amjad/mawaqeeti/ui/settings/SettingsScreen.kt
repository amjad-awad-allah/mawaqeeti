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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.lavalamp.LavaLamp
import com.example.lavalamp.LavaMode

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

    val fOff by viewModel.fajrOffset.collectAsState()
    val dOff by viewModel.dhuhrOffset.collectAsState()
    val aOff by viewModel.asrOffset.collectAsState()
    val mOff by viewModel.maghribOffset.collectAsState()
    val iOff by viewModel.ishaOffset.collectAsState()

    var tempCity by remember(city) { mutableStateOf(city) }
    var tempCountry by remember(country) { mutableStateOf(country) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF03070C))) {
        // Subtle Background Animation
        // lavalamb
        LavaLamp(
            modifier = Modifier.fillMaxSize().alpha(0.2f),
            mode = LavaMode.Vector(customColors = listOf(Color(0xFF1A263B), Color(0xFF0D1B2A))),
            speed = 0.15f
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                    title = { Text("الإعدادات", color = Color.White, fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
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
                SettingsSection(title = "الموقع والمنطقة", icon = Icons.Default.LocationOn) {
                    ModernTextField(value = tempCity, onValueChange = { tempCity = it }, label = "المدينة")
                    Spacer(Modifier.height(12.dp))
                    ModernTextField(value = tempCountry, onValueChange = { tempCountry = it }, label = "الدولة")
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.updateLocation(tempCity, tempCountry, method) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64FFDA))
                    ) {
                        Text("حفظ التغييرات", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                // Offsets Section
                SettingsSection(title = "تعديل التواقيت (دقائق)", icon = Icons.Default.Schedule) {
                    OffsetItem("الفجر", fOff) { viewModel.updateOffset("Fajr", it) }
                    OffsetItem("الظهر", dOff) { viewModel.updateOffset("Dhuhr", it) }
                    OffsetItem("العصر", aOff) { viewModel.updateOffset("Asr", it) }
                    OffsetItem("المغرب", mOff) { viewModel.updateOffset("Maghrib", it) }
                    OffsetItem("العشاء", iOff) { viewModel.updateOffset("Isha", it) }
                }

                // Performance Section
                SettingsSection(title = "أداء الخلفية السائلة", icon = Icons.Default.AutoAwesome) {
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
                                        "FULL" -> "أداء كامل (بصمة بصرية قصوى)"
                                        "BALANCED" -> "متوازن (موصى به)"
                                        else -> "توفير الطاقة"
                                    },
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
