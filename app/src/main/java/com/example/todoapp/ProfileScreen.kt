package com.example.todoapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onNameChanged: (String) -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    var name by remember { mutableStateOf("") }
    var savedMessageVisible by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    fun openNotificationSettings() {
        // Mark that the next time the app resumes (whether this exact
        // composable is still alive or the Activity got recreated), it
        // should continue straight on to battery settings.
        ReminderSetupFlow.awaitingBatteryStep = true
        SplashSkipFlag.skipNextResume = true
        val intent = Intent().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            } else {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        context.startActivity(intent)
    }

    fun refreshNotificationStatus() {
        notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    LaunchedEffect(Unit) {
        name = TaskRepository.getUserName(context)
        refreshNotificationStatus()
    }

    // Just refresh the displayed status when returning to this screen -
    // the battery-settings continuation itself is handled at the top
    // level (MainActivity/AppNavigation), since this composable may not
    // survive the round trip on some phones.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshNotificationStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {
        Text(
            text = "Profile",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = PurpleDark
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Your name",
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                savedMessageVisible = false
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurpleDark,
                focusedLabelColor = PurpleDark,
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                cursorColor = PurpleDark
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val trimmed = name.trim()
                if (trimmed.isNotEmpty()) {
                    scope.launch {
                        TaskRepository.saveUserName(context, trimmed)
                        onNameChanged(trimmed)
                        savedMessageVisible = true
                    }
                }
            },
            enabled = name.trim().isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save", color = Color.White)
        }

        if (savedMessageVisible) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "✓ Saved!",
                color = Color(0xFF43A047),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(color = PurpleBg, thickness = 1.dp)
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Task reminders",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark
                )
                Text(
                    text = if (notificationsEnabled) "Enabled" else "Disabled",
                    fontSize = 13.sp,
                    color = if (notificationsEnabled) Color(0xFF43A047) else Color.Gray
                )
            }
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { openNotificationSettings() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PurpleDark
                )
            )
        }
    }
}