package com.amjad.mawaqeeti.ui.test

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amjad.mawaqeeti.notification.AlarmReceiver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مختبر التنبيهات", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0E14),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0A0E14)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TestSection(
                title = "1. اختبار فوري",
                description = "تشغيل التنبيه الآن (والتطبيق مفتوح)"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallTestButton("Athan", Modifier.weight(1f)) { triggerTestAlarm(context, "الفجر", 0) }
                SmallTestButton("Past (-1h)", Modifier.weight(1f)) { triggerTestAlarm(context, "الظهر", 0, System.currentTimeMillis() - 3600000) }
                SmallTestButton("Recent (-2m)", Modifier.weight(1f)) { triggerTestAlarm(context, "العصر", 0, System.currentTimeMillis() - 120000) }
            }

            Divider(color = Color.White.copy(alpha = 0.1f))

            TestSection(
                title = "2. اختبار الجدولة (بعد 10 ثوانٍ)",
                description = "اضغط ثم أغلق التطبيق/الشاشة فوراً لتجربة الوصول"
            )

            TestButton(
                text = "جدولة أذان (Athan) بعد 10ث",
                icon = Icons.Default.Timer,
                onClick = { scheduleTestAlarm(context, "المغرب", 0, 10) }
            )

            TestButton(
                text = "جدولة تنبيه 15د (NoTime) بعد 10ث",
                icon = Icons.Default.Timer,
                onClick = { scheduleTestAlarm(context, "العشاء", 15, 10) }
            )

            TestButton(
                text = "جدولة تنبيه ساعة (Bill) بعد 10ث",
                icon = Icons.Default.Timer,
                onClick = { scheduleTestAlarm(context, "الفجر", 60, 10) }
            )

            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

            Text(
                "💡 نصيحة: بعد الضغط على أزرار الجدولة، قم بقفل الهاتف أو الخروج للهوم لتجربة وصول الإشعار في الخلفية.",
                color = Color(0xFF64FFDA).copy(alpha = 0.7f),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun TestSection(title: String, description: String) {
    Column {
        Text(title, color = Color(0xFF64FFDA), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(description, color = Color.Gray, fontSize = 13.sp)
    }
}

@Composable
fun TestButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
        contentPadding = PaddingValues(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF64FFDA))
        Spacer(Modifier.width(12.dp))
        Text(text, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
fun SmallTestButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64FFDA).copy(alpha = 0.1f)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Text(text, color = Color(0xFF64FFDA), fontSize = 12.sp)
    }
}

private fun triggerTestAlarm(context: Context, prayerName: String, minutesBefore: Int, scheduledTime: Long = System.currentTimeMillis()) {
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("PRAYER_NAME", prayerName)
        putExtra("MINUTES_BEFORE", minutesBefore)
        putExtra("SCHEDULED_TIME", scheduledTime)
    }
    context.sendBroadcast(intent)
}

private fun scheduleTestAlarm(context: Context, prayerName: String, minutesBefore: Int, secondsDelayed: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("PRAYER_NAME", prayerName)
        putExtra("MINUTES_BEFORE", minutesBefore)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        System.currentTimeMillis().toInt(), // Unique request code
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerAt = System.currentTimeMillis() + (secondsDelayed * 1000)

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        triggerAt,
        pendingIntent
    )
}
