package domilopment.apkextractor.ui.settings.autoBackup

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.R
import domilopment.apkextractor.autoBackup.AutoBackupService
import domilopment.apkextractor.ui.ScreenConfig
import domilopment.apkextractor.ui.viewModels.SettingsScreenViewModel
import domilopment.apkextractor.utils.MySnackbarVisuals
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import domilopment.apkextractor.ui.navigation.Route

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsAutoBackupScreen(
    model: SettingsScreenViewModel,
    showSnackbar: (MySnackbarVisuals) -> Unit,
    context: Context = LocalContext.current,
    onBackClicked: () -> Unit
) {
    val uiState by model.uiState.collectAsStateWithLifecycle()

    val isSelectAutoBackupApps by remember {
        derivedStateOf {
            uiState.autoBackupService && uiState.autoBackupAppsListState.isNotEmpty()
        }
    }

    val pm = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }

    var batteryOptimization by remember {
        mutableStateOf(pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID))
    }

    val allowNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS) { isPermissionGranted ->
            if (isPermissionGranted) {
                handleAutoBackupService(true, context)
                model.setAutoBackupService(true)
            } else {
                showSnackbar(
                    MySnackbarVisuals(
                        duration = SnackbarDuration.Short,
                        message = context.getString(R.string.auto_backup_notification_permission_request_rejected)
                    )
                )
            }
        }
    } else null

    val ignoreBatteryOptimizationResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val isIgnoringBatteryOptimization =
                (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(
                    BuildConfig.APPLICATION_ID
                )
            batteryOptimization = isIgnoringBatteryOptimization
        }

    LaunchedEffect(key1 = Unit) {
        Route.Screen.SettingsAutoBackup.buttons.onEach { button ->
            when (button) {
                ScreenConfig.ScreenActions.NavigationIcon -> onBackClicked()
                else -> Unit
            }
        }.launchIn(this)
    }

    SettingsAutoBackupContent(
        autoBackupService = uiState.autoBackupService,
        onAutoBackupService = func@{
            if (allowNotifications != null) {
                if (it && !allowNotifications.status.isGranted) {
                    allowNotifications.launchPermissionRequest()
                    return@func
                }
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            handleAutoBackupService(
                it && notificationManager.areNotificationsEnabled(), context
            )
            model.setAutoBackupService(it && notificationManager.areNotificationsEnabled())
        },
        isSelectAutoBackupApps = isSelectAutoBackupApps,
        autoBackupListApps = uiState.autoBackupAppsListState,
        autoBackupList = uiState.autoBackupList,
        onAutoBackupList = model::setAutoBackupList,
        batteryOptimization = batteryOptimization,
        onBatteryOptimization = {
            val isIgnoringBatteryOptimization =
                pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)
            if ((!isIgnoringBatteryOptimization and it) or (isIgnoringBatteryOptimization and !it)) {
                ignoreBatteryOptimizationResult.launch(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        },
    )
}

/**
 * Manage auto backup service start and stop behaviour
 * Start, if it should be running and isn't
 * Stop, if it is running and shouldn't be
 * @param newValue boolean of service should be running
 */
private fun handleAutoBackupService(newValue: Boolean, context: Context) {
    if (newValue and !AutoBackupService.isRunning) context.startForegroundService(
        Intent(
            context, AutoBackupService::class.java
        ).apply {
            action = AutoBackupService.Actions.START.name
        })
    else if (!newValue and AutoBackupService.isRunning) context.startService(
        Intent(
            context, AutoBackupService::class.java
        ).apply {
            action = AutoBackupService.Actions.STOP.name
        })
}