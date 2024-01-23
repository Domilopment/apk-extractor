package domilopment.apkextractor.ui.settings

import android.Manifest
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.DocumentsContract
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.material.color.DynamicColors
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.R
import domilopment.apkextractor.autoBackup.AutoBackupService
import domilopment.apkextractor.ui.viewModels.SettingsScreenViewModel
import domilopment.apkextractor.utils.MySnackbarVisuals
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.settings.SettingsManager
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    model: SettingsScreenViewModel,
    showSnackbar: (MySnackbarVisuals) -> Unit,
    chooseSaveDir: ManagedActivityResultLauncher<Uri?, Uri?>,
    context: Context = LocalContext.current,
    appUpdateManager: AppUpdateManager,
    inAppUpdateResultLauncher: ActivityResultLauncher<IntentSenderRequest>
) {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    val apps by model.autoBackupAppsListState.collectAsState()
    val autoBackupService = model.autoBackupService.collectAsState()
    val autoBackupList = model.autoBackupList.collectAsState()
    val batteryOptimization = remember {
        mutableStateOf(pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID))
    }

    var appUpdateInfo: AppUpdateInfo? by remember {
        mutableStateOf(null)
    }

    val isSelectAutoBackupApps by remember {
        derivedStateOf {
            autoBackupService.value && apps.isNotEmpty()
        }
    }

    val isUpdateAvailable by remember {
        derivedStateOf {
            appUpdateInfo?.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo?.isUpdateTypeAllowed(
                AppUpdateType.FLEXIBLE
            ) == true
        }
    }

    val language = remember {
        mutableStateOf(AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag() ?: "default")
    }

    val ignoreBatteryOptimizationResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val isIgnoringBatteryOptimization =
                (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(
                    BuildConfig.APPLICATION_ID
                )
            batteryOptimization.value = isIgnoringBatteryOptimization
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

    LaunchedEffect(key1 = appUpdateManager.appUpdateInfo, block = {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            appUpdateInfo = info
        }
    })

    SettingsContent(
        appUpdateInfo = appUpdateInfo,
        isUpdateAvailable = isUpdateAvailable,
        onUpdateAvailable = remember(appUpdateManager, appUpdateInfo, inAppUpdateResultLauncher) {
            {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo!!,
                    inAppUpdateResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            }
        },
        onChooseSaveDir = remember(model.saveDir, chooseSaveDir) {
            {
                val pickerInitialUri = model.saveDir.value?.let {
                    DocumentsContract.buildDocumentUriUsingTree(
                        it, DocumentsContract.getTreeDocumentId(it)
                    )
                }
                chooseSaveDir.launch(pickerInitialUri)
            }
        },
        appSaveName = model.saveName.collectAsState(),
        onAppSaveName = remember { model::setAppSaveName },
        autoBackupService = autoBackupService,
        onAutoBackupService = remember(allowNotifications, context) {
            func@{
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
            }
        },
        isSelectAutoBackupApps = isSelectAutoBackupApps,
        autoBackupListApps = apps,
        autoBackupList = autoBackupList,
        onAutoBackupList = remember { model::setAutoBackupList },
        nightMode = model.nightMode.collectAsState(),
        onNightMode = remember {
            {
                model.setNightMode(it)
                SettingsManager.changeUIMode(it)
            }
        },
        dynamicColors = model.useMaterialYou.collectAsState(),
        isDynamicColors = remember { DynamicColors.isDynamicColorAvailable() },
        onDynamicColors = remember { model::setUseMaterialYou },
        language = language,
        languageLocaleDisplayName = remember(language) {
            when (language.value) {
                null, "default" -> context.getString(R.string.locale_list_default)
                Locale.ENGLISH.toLanguageTag() -> context.getString(R.string.locale_list_en)
                Locale.GERMANY.toLanguageTag() -> context.getString(R.string.locale_list_de_de)
                else -> context.getString(
                    R.string.locale_list_not_supported, Locale.forLanguageTag(language.value).displayName
                )
            }
        },
        onLanguage = remember { SettingsManager::setLocale },
        rightSwipeAction = model.rightSwipeAction.collectAsState(),
        onRightSwipeAction = remember { model::setRightSwipeAction },
        leftSwipeAction = model.leftSwipeAction.collectAsState(),
        onLeftSwipeAction = remember { model::setLeftSwipeAction },
        swipeActionThresholdMod = model.swipeActionThresholdMod.collectAsState(),
        onSwipeActionThresholdMod = remember { model::setSwipeActionThresholdMod },
        batteryOptimization = batteryOptimization,
        onBatteryOptimization = remember(pm, ignoreBatteryOptimizationResult) {
            {
                val isIgnoringBatteryOptimization =
                    pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)
                if ((!isIgnoringBatteryOptimization and it) or (isIgnoringBatteryOptimization and !it)) {
                    ignoreBatteryOptimizationResult.launch(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                }
            }
        },
        checkUpdateOnStart = model.checkUpdateOnStart.collectAsState(),
        onCheckUpdateOnStart = remember { model::setCheckUpdateOnStart },
        onClearCache = remember(context) {
            {
                if (context.cacheDir?.deleteRecursively() == true) Toast.makeText(
                    context, context.getString(R.string.clear_cache_success), Toast.LENGTH_SHORT
                ).show()
                else Toast.makeText(
                    context, context.getString(R.string.clear_cache_failed), Toast.LENGTH_SHORT
                ).show()
            }
        },
        onGitHub = remember(context) {
            {
                CustomTabsIntent.Builder().build().launchUrl(
                    context, Uri.parse("https://github.com/domilopment/apk-extractor")
                )
            }
        },
        onGooglePlay = remember(context) {
            {
                try {
                    Intent(Intent.ACTION_VIEW).apply {
                        try {
                            val packageInfo = Utils.getPackageInfo(
                                context.packageManager, "com.android.vending"
                            )
                            setPackage(packageInfo.packageName)
                        } catch (e: PackageManager.NameNotFoundException) {
                            // If Play Store is not installed
                        }
                        data =
                            Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                    }.also {
                        context.startActivity(it)
                    }
                } catch (e: ActivityNotFoundException) { // If Play Store is Installed, but deactivated
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                        )
                    )
                }
            }
        },
        onPrivacyPolicy = remember(context) {
            {
                CustomTabsIntent.Builder().build().launchUrl(
                    context, Uri.parse("https://sites.google.com/view/domilopment/privacy-policy")
                )
            }
        })
}

/**
 * Manage auto backup service start and stop behaviour
 * Start, if it should be running and isn't
 * Stop, if it is running and shouldn't be
 * @param newValue boolean of service should be running
 */
private fun handleAutoBackupService(newValue: Boolean, context: Context) {
    if (newValue and !AutoBackupService.isRunning) context.startForegroundService(
        Intent(context, AutoBackupService::class.java)
    )
    else if (!newValue and AutoBackupService.isRunning) context.stopService(
        Intent(context, AutoBackupService::class.java)
    )
}