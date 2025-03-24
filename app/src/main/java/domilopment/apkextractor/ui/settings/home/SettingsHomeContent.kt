package domilopment.apkextractor.ui.settings.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.SwipeLeft
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.UpdateDisabled
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.google.android.play.core.appupdate.AppUpdateInfo
import domilopment.apkextractor.R
import domilopment.apkextractor.data.SettingsScreenAppAutoBackUpListState
import domilopment.apkextractor.ui.settings.preferences.DialogPreference
import domilopment.apkextractor.ui.settings.preferences.ListPreference
import domilopment.apkextractor.ui.settings.preferences.MultiSelectListPreference
import domilopment.apkextractor.ui.settings.preferences.Preference
import domilopment.apkextractor.ui.settings.preferences.preferenceCategory
import domilopment.apkextractor.ui.settings.preferences.SeekBarPreference
import domilopment.apkextractor.ui.settings.preferences.SwitchPreferenceCompat
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemBottom
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemMiddle
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemTop

@Composable
fun SettingsHomeContent(
    appUpdateInfo: AppUpdateInfo?,
    isUpdateAvailable: Boolean,
    onUpdateAvailable: () -> Unit,
    onChooseSaveDir: () -> Unit,
    onSaveFileSettings: () -> Unit,
    autoBackupService: Boolean,
    onAutoBackupService: (Boolean) -> Unit,
    isSelectAutoBackupApps: Boolean,
    autoBackupListApps: SettingsScreenAppAutoBackUpListState,
    autoBackupList: Set<String>,
    onAutoBackupList: (Set<String>) -> Unit,
    nightMode: Int,
    onNightMode: (Int) -> Unit,
    dynamicColors: Boolean,
    isDynamicColors: Boolean,
    onDynamicColors: (Boolean) -> Unit,
    language: String,
    languageLocaleDisplayName: String,
    onLanguage: (String) -> Unit,
    rightSwipeAction: String,
    onRightSwipeAction: (String) -> Unit,
    leftSwipeAction: String,
    onLeftSwipeAction: (String) -> Unit,
    swipeActionCustomThreshold: Boolean,
    onSwipeActionCustomThreshold: (Boolean) -> Unit,
    swipeActionThresholdMod: Float,
    onSwipeActionThresholdMod: (Float) -> Unit,
    batteryOptimization: Boolean,
    onBatteryOptimization: (Boolean) -> Unit,
    checkUpdateOnStart: Boolean,
    onCheckUpdateOnStart: (Boolean) -> Unit,
    cacheSize: String,
    onClearCache: () -> Unit,
    analytics: Boolean,
    onAnalytics: (Boolean) -> Unit,
    crashlytics: Boolean,
    onCrashlytics: (Boolean) -> Unit,
    performance: Boolean,
    onPerformance: (Boolean) -> Unit,
    dataCollectionDeleteDialogContent: @Composable (() -> Unit),
    onDeleteFirebaseInstallationsId: () -> Unit,
    onGitHub: () -> Unit,
    onGooglePlay: () -> Unit,
    onAboutSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.testTag("SettingsLazyColumn"),
        state = rememberLazyListState(),
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
            .union(WindowInsets(left = 8.dp, right = 8.dp)).asPaddingValues()
    ) {
        item {
            if (appUpdateInfo != null) Preference(
                name = R.string.update_available_title,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(top = 8.dp),
                summary = R.string.update_available_summary,
                isPreferenceVisible = isUpdateAvailable,
                onClick = onUpdateAvailable
            )
        }

        preferenceCategory(title = R.string.save_header) {
            preferenceCategoryItemTop {
                Preference(
                    name = R.string.choose_save_dir,
                    summary = R.string.choose_save_dir_summary,
                    icon = Icons.Default.Folder,
                    onClick = onChooseSaveDir
                )
            }
            preferenceCategoryItemMiddle {
                Preference(
                    name = R.string.title_screen_save_file_settings,
                    icon = Icons.AutoMirrored.Default.InsertDriveFile,
                    onClick = onSaveFileSettings
                ) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                }
            }
            preferenceCategoryItemMiddle {
                SwitchPreferenceCompat(
                    name = R.string.auto_backup,
                    icon = if (autoBackupService) Icons.Default.Sync else Icons.Default.SyncDisabled,
                    summary = R.string.auto_backup_summary,
                    state = autoBackupService,
                    onClick = onAutoBackupService
                )
            }
            preferenceCategoryItemBottom {
                MultiSelectListPreference(
                    icon = Icons.Default.Checklist,
                    name = stringResource(id = R.string.auto_backup_app_list),
                    enabled = isSelectAutoBackupApps,
                    entries = autoBackupListApps.entries,
                    entryValues = autoBackupListApps.entryValues,
                    summary = stringResource(id = R.string.auto_backup_app_list_summary),
                    state = autoBackupList,
                    onClick = onAutoBackupList
                )
            }
        }

        preferenceCategory(title = R.string.appearance) {
            preferenceCategoryItemTop {
                ListPreference(
                    name = R.string.ui_mode,
                    summary = R.string.ui_mode_summary,
                    icon = Icons.Default.ModeNight,
                    entries = R.array.ui_mode_names,
                    entryValues = R.array.ui_mode_values,
                    state = nightMode,
                    onClick = onNightMode
                )
            }
            preferenceCategoryItemMiddle {
                SwitchPreferenceCompat(
                    name = R.string.use_material_you,
                    summary = R.string.use_material_you_summary,
                    state = dynamicColors,
                    isVisible = isDynamicColors,
                    onClick = onDynamicColors
                )
            }
            preferenceCategoryItemBottom {
                ListPreference(
                    name = stringResource(id = R.string.locale_list_title),
                    icon = Icons.Default.Language,
                    summary = stringResource(
                        id = R.string.locale_list_summary, languageLocaleDisplayName
                    ),
                    entries = stringArrayResource(id = R.array.locale_list_names),
                    entryValues = stringArrayResource(id = R.array.locale_list_values),
                    state = language,
                    onClick = onLanguage
                )
            }
        }

        preferenceCategory(title = R.string.apk_swipe_actions) {
            preferenceCategoryItemTop {
                ListPreference(
                    name = R.string.apk_swipe_action_right_title,
                    icon = Icons.Default.SwipeRight,
                    summary = R.string.apk_swipe_action_right_summary,
                    entries = R.array.apk_swipe_options_entries,
                    entryValues = R.array.apk_swipe_options_values,
                    state = rightSwipeAction,
                    onClick = onRightSwipeAction
                )
            }
            preferenceCategoryItemMiddle {
                ListPreference(
                    name = R.string.apk_swipe_action_left_title,
                    icon = Icons.Default.SwipeLeft,
                    summary = R.string.apk_swipe_action_left_summary,
                    entries = R.array.apk_swipe_options_entries,
                    entryValues = R.array.apk_swipe_options_values,
                    state = leftSwipeAction,
                    onClick = onLeftSwipeAction
                )
            }
            preferenceCategoryItemMiddle {
                SwitchPreferenceCompat(
                    name = R.string.apk_swipe_action_custom_threshold_title,
                    summary = R.string.apk_swipe_action_custom_threshold_summary,
                    state = swipeActionCustomThreshold,
                    onClick = onSwipeActionCustomThreshold
                )
            }
            preferenceCategoryItemBottom {
                SeekBarPreference(
                    enabled = swipeActionCustomThreshold,
                    name = R.string.apk_swipe_action_threshold_title,
                    summary = R.string.apk_swipe_action_threshold_summary,
                    min = 0f,
                    max = 100f,
                    steps = 100,
                    showValue = true,
                    state = swipeActionThresholdMod,
                    onValueChanged = onSwipeActionThresholdMod,
                )
            }
        }

        preferenceCategory(title = R.string.advanced) {
            preferenceCategoryItemTop {
                SwitchPreferenceCompat(
                    name = R.string.ignore_battery_optimization_title,
                    summary = R.string.ignore_battery_optimization_summary,
                    icon = Icons.Default.BatteryStd,
                    state = batteryOptimization,
                    onClick = onBatteryOptimization
                )
            }
            preferenceCategoryItemMiddle {
                SwitchPreferenceCompat(
                    name = R.string.check_update_on_start_title,
                    icon = if (checkUpdateOnStart) Icons.Default.Update else Icons.Default.UpdateDisabled,
                    summary = R.string.check_update_on_start_summary,
                    state = checkUpdateOnStart,
                    onClick = onCheckUpdateOnStart
                )
            }
            preferenceCategoryItemBottom {
                Preference(
                    name = stringResource(id = R.string.clear_cache),
                    icon = Icons.Default.CleaningServices,
                    summary = stringResource(id = R.string.clear_cache_summary, cacheSize),
                    onClick = onClearCache
                )
            }
        }

        preferenceCategory(title = R.string.data_collection_header) {
            preferenceCategoryItemTop {
                SwitchPreferenceCompat(
                    icon = Icons.Default.Analytics,
                    name = R.string.data_collection_analytics,
                    summary = R.string.data_collection_analytics_summary,
                    state = analytics,
                    onClick = onAnalytics
                )
            }
            preferenceCategoryItemMiddle {
                SwitchPreferenceCompat(
                    icon = Icons.Default.BugReport,
                    name = R.string.data_collection_crashlytics,
                    summary = R.string.data_collection_crashlytics_summary,
                    state = crashlytics,
                    onClick = onCrashlytics
                )
            }
            preferenceCategoryItemMiddle {
                SwitchPreferenceCompat(
                    name = R.string.data_collection_perf,
                    summary = R.string.data_collection_perf_summary,
                    state = performance,
                    onClick = onPerformance
                )
            }
            preferenceCategoryItemBottom {
                DialogPreference(
                    name = R.string.data_collection_delete,
                    onConfirm = onDeleteFirebaseInstallationsId,
                    dialogContent = dataCollectionDeleteDialogContent
                )
            }
        }

        preferenceCategory(title = R.string.info_header) {
            preferenceCategoryItemTop {
                Preference(
                    name = R.string.github,
                    icon = ImageVector.vectorResource(id = R.drawable.github_mark_white),
                    summary = R.string.github_summary,
                    onClick = onGitHub
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Default.OpenInNew, contentDescription = null)
                }
            }
            preferenceCategoryItemMiddle {
                Preference(
                    name = R.string.googleplay,
                    icon = Icons.Default.Shop,
                    summary = R.string.googleplay_summary,
                    onClick = onGooglePlay
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Default.OpenInNew, contentDescription = null)
                }
            }
            preferenceCategoryItemBottom {
                Preference(
                    name = stringResource(id = R.string.title_screen_about_settings),
                    icon = Icons.Default.Info,
                    onClick = onAboutSettings
                ) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}
