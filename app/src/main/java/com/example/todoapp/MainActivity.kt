package com.example.todoapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.ui.theme.TodoAppTheme

/**
 * Set this right before launching an intent that briefly leaves the app
 * (e.g. system battery settings) when the splash-on-resume replay should
 * be skipped just once for that return trip.
 */
object SplashSkipFlag {
    @Volatile
    var skipNextResume: Boolean = false
}

/**
 * Tracks the multi-step reminder setup flow across the activity resume
 * boundary. Checked at the top level (AppNavigation), not inside
 * ProfileScreen, because the Activity can be killed and recreated while
 * the user is away in system Settings on aggressive-memory-management
 * phones (Oppo/Realme/Vivo) - if recreated, ProfileScreen's own composable
 * (and any state local to it) is gone, but this top-level check still runs
 * on every fresh launch and can resume the flow correctly.
 */
object ReminderSetupFlow {
    @Volatile
    var awaitingBatteryStep: Boolean = false
}

fun openBatterySettings(context: Context) {
    try {
        val intent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
    } catch (e: Exception) {
        val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        context.startActivity(fallback)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current
    val hasResumedBefore = remember { booleanArrayOf(false) }

    // null = still checking, true/false = result known
    var hasName by remember { mutableStateOf<Boolean?>(null) }

    // Launcher for the native "Allow/Deny" notification permission dialog
    // (Android 13+ only - on older versions notifications are granted by
    // default at install time, so there's nothing to request).
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* granted or denied - ProfileScreen reflects the actual state on resume */ }

    LaunchedEffect(Unit) {
        val name = TaskRepository.getUserName(context)
        hasName = name.isNotBlank()
    }

    // Once the user is set up (either already had a name, or just finished
    // name entry), automatically trigger the notification permission prompt
    // instead of requiring a manual trip to Profile settings.
    LaunchedEffect(hasName) {
        if (hasName == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val alreadyGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!alreadyGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (ReminderSetupFlow.awaitingBatteryStep) {
                    ReminderSetupFlow.awaitingBatteryStep = false
                    SplashSkipFlag.skipNextResume = true
                    openBatterySettings(context)
                    hasResumedBefore[0] = true
                    return@LifecycleEventObserver
                }
                val shouldSkipThisTime = SplashSkipFlag.skipNextResume
                if (shouldSkipThisTime) {
                    SplashSkipFlag.skipNextResume = false
                } else if (hasResumedBefore[0] && hasName == true) {
                    // App is coming back to foreground (not the very first launch,
                    // and the user already has a name set) - replay splash.
                    navController.navigate("splash") {
                        popUpTo(0)
                    }
                }
                hasResumedBefore[0] = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (hasName == null) {
        // Still loading - render nothing momentarily to avoid a flash of the wrong screen
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (hasName == true) "splash" else "name_entry"
    ) {
        composable(route = "name_entry") {
            NameEntryScreen(
                onNameSaved = {
                    hasName = true
                    navController.navigate("splash") {
                        popUpTo("name_entry") { inclusive = true }
                    }
                }
            )
        }
        composable(route = "splash") {
            SplashScreen(navController = navController)
        }
        composable(route = "todo") {
            MainShellScreen()
        }
    }
}