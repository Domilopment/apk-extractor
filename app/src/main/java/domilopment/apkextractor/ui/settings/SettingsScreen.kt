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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
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

    val apps by model.applications.collectAsState()
    val autoBackupService = model.autoBackupService.collectAsState()
    val autoBackupList = model.autoBackupList.collectAsState()
    val batteryOptimization = remember {
        mutableStateOf(pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID))
    }

    var appUpdateInfo: AppUpdateInfo? by remember {
        mutableStateOf(null)
    }

    val localeMap = remember {
        mapOf(
            null to context.getString(R.string.locale_list_default),
            Locale.ENGLISH.toLanguageTag() to context.getString(R.string.locale_list_en),
            Locale.GERMANY.toLanguageTag() to context.getString(R.string.locale_list_de_de)
        ).withDefault {
            context.getString(
                R.string.locale_list_not_supported, Locale.forLanguageTag(it!!).displayName
            )
        }
    }

    val isSelectAutoBackupApps by remember {
        derivedStateOf {
            autoBackupService.value && apps.isNotEmpty()
        }
    }
    val selectAutoBackupAppsEntries by remember {
        derivedStateOf {
            apps.keys.toTypedArray()
        }
    }
    val selectAutoBackupAppsEntryValues by remember {
        derivedStateOf {
            apps.values.toTypedArray()
        }
    }

    val isUpdateAvailable by remember {
        derivedStateOf {
            appUpdateInfo?.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo?.isUpdateTypeAllowed(
                AppUpdateType.FLEXIBLE
            ) == true
        }
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

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (appUpdateInfo != null) Preference(name = R.string.update_available_title,
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)
            ),
            summary = R.string.update_available_summary,
            isPreferenceVisible = isUpdateAvailable,
            onClick = {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo!!,
                    inAppUpdateResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            })
        PreferenceCategory(title = R.string.save_header) {
            Preference(name = R.string.choose_save_dir,
                summary = R.string.choose_save_dir_summary,
                icon = Icons.Default.Folder,
                onClick = {
                    val pickerInitialUri = model.saveDir.value?.let {
                        DocumentsContract.buildDocumentUriUsingTree(
                            it, DocumentsContract.getTreeDocumentId(it)
                        )
                    }
                    chooseSaveDir.launch(pickerInitialUri)
                })
            APKNamePreference(
                name = R.string.app_save_name,
                summary = R.string.app_save_name_summary,
                entries = R.array.app_save_name_names,
                entryValues = R.array.app_save_name_values,
                state = model.saveName.collectAsState(),
                onClick = model::setAppSaveName
            )
            SwitchPreferenceCompat(name = R.string.auto_backup,
                summary = R.string.auto_backup_summary,
                state = autoBackupService,
                onClick = {
                    if (allowNotifications != null) {
                        if (it && !allowNotifications.status.isGranted) {
                            allowNotifications.launchPermissionRequest()
                            return@SwitchPreferenceCompat
                        }
                    }

                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    handleAutoBackupService(
                        it && notificationManager.areNotificationsEnabled(), context
                    )
                    model.setAutoBackupService(it && notificationManager.areNotificationsEnabled())
                })
            MultiSelectListPreference(
                name = stringResource(id = R.string.auto_backup_app_list),
                enabled = isSelectAutoBackupApps,
                entries = selectAutoBackupAppsEntries,
                entryValues = selectAutoBackupAppsEntryValues,
                summary = stringResource(id = R.string.auto_backup_app_list_summary),
                state = autoBackupList,
                onClick = model::setAutoBackupList
            )
        }
        PreferenceCategory(title = R.string.appearance) {
            ListPreference(name = R.string.ui_mode,
                summary = R.string.ui_mode_summary,
                icon = Icons.Default.ModeNight,
                entries = R.array.ui_mode_names,
                entryValues = R.array.ui_mode_values,
                state = model.nightMode.collectAsState(),
                onClick = {
                    model.setNightMode(it)
                    SettingsManager.changeUIMode(it)
                })
            SwitchPreferenceCompat(
                name = R.string.use_material_you,
                summary = R.string.use_material_you_summary,
                state = model.useMaterialYou.collectAsState(),
                isVisible = remember {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                },
                onClick = model::setUseMaterialYou
            )
            ListPreference(name = stringResource(id = R.string.locale_list_title),
                summary = stringResource(
                    id = R.string.locale_list_summary,
                    localeMap.getValue(AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag())
                ),
                entries = stringArrayResource(id = R.array.locale_list_names),
                entryValues = stringArrayResource(id = R.array.locale_list_values),
                state = model.language.collectAsState(),
                onClick = {
                    model.setLanguage(it)
                    SettingsManager.setLocale(it)
                })
        }
        PreferenceCategory(title = R.string.advanced) {
            ListPreference(
                name = R.string.apk_swipe_action_right_title,
                summary = R.string.apk_swipe_action_right_summary,
                entries = R.array.apk_swipe_options_entries,
                entryValues = R.array.apk_swipe_options_values,
                state = model.rightSwipeAction.collectAsState(),
                onClick = model::setRightSwipeAction
            )
            ListPreference(
                name = R.string.apk_swipe_action_left_title,
                summary = R.string.apk_swipe_action_left_summary,
                entries = R.array.apk_swipe_options_entries,
                entryValues = R.array.apk_swipe_options_values,
                state = model.leftSwipeAction.collectAsState(),
                onClick = model::setLeftSwipeAction
            )
            SwitchPreferenceCompat(name = R.string.ignore_battery_optimization_title,
                summary = R.string.ignore_battery_optimization_summary,
                icon = Icons.Default.BatteryStd,
                state = batteryOptimization,
                onClick = {
                    val isIgnoringBatteryOptimization =
                        pm.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)
                    if ((!isIgnoringBatteryOptimization and it) or (isIgnoringBatteryOptimization and !it)) {
                        ignoreBatteryOptimizationResult.launch(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    }
                })
            SwitchPreferenceCompat(
                name = R.string.check_update_on_start_title,
                summary = R.string.check_update_on_start_summary,
                state = model.checkUpdateOnStart.collectAsState(),
                onClick = model::setCheckUpdateOnStart
            )
            Preference(name = R.string.clear_cache,
                summary = R.string.clear_cache_summary,
                onClick = {
                    if (context.cacheDir?.deleteRecursively() == true) Toast.makeText(
                        context, context.getString(R.string.clear_cache_success), Toast.LENGTH_SHORT
                    ).show()
                    else Toast.makeText(
                        context, context.getString(R.string.clear_cache_failed), Toast.LENGTH_SHORT
                    ).show()
                })
        }
        PreferenceCategory(title = R.string.info_header) {
            Preference(name = R.string.github, summary = R.string.github_summary, onClick = {
                CustomTabsIntent.Builder().build().launchUrl(
                    context, Uri.parse("https://github.com/domilopment/apk-extractor")
                )
            })
            Preference(
                name = R.string.googleplay,
                summary = R.string.googleplay_summary,
                onClick = {
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
                })
            Preference(name = R.string.privacy_policy_title, onClick = {
                CustomTabsIntent.Builder().build().launchUrl(
                    context, Uri.parse("https://sites.google.com/view/domilopment/privacy-policy")
                )
            })
            Preference(name = stringResource(id = R.string.version, BuildConfig.VERSION_NAME),
                enabled = false,
                onClick = {})
        }
    }
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