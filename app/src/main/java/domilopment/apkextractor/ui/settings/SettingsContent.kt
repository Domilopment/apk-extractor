package domilopment.apkextractor.ui.settings

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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.SwipeLeft
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.UpdateDisabled
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
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.R
import domilopment.apkextractor.data.SettingsScreenAppAutoBackUpListState
import domilopment.apkextractor.ui.settings.preferences.APKNamePreference
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
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.settings.Spacer
import domilopment.apkextractor.utils.settings.getNameResId

@Composable
fun SettingsContent(
    appUpdateInfo: AppUpdateInfo?,
    isUpdateAvailable: Boolean,
    onUpdateAvailable: () -> Unit,
    onChooseSaveDir: () -> Unit,
    appSaveName: Set<String>,
    onAppSaveName: (Set<String>) -> Unit,
    appSaveNameSpacer: String,
    onAppSaveNameSpacer: (String) -> Unit,
    isBackupModeXapk: Boolean,
    onBackupModeXapk: (Boolean) -> Unit,
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
    bundleFileInfo: String,
    onBundleFileInfo: (String) -> Unit,
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
    onPrivacyPolicy: () -> Unit,
    onTerms: () -> Unit,
    ossDependencies: () -> Unit,
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
                APKNamePreference(
                    name = R.string.app_save_name,
                    icon = Icons.Default.TextFormat,
                    summary = R.string.app_save_name_summary,
                    entries = R.array.app_save_name_names,
                    entryValues = R.array.app_save_name_values,
                    state = appSaveName,
                    onClick = onAppSaveName
                )
            }
            preferenceCategoryItemMiddle {
                ListPreference(
                    name = stringResource(id = R.string.app_save_name_part_separator),
                    summary = stringResource(id = R.string.app_save_name_part_separator_summary),
                    entries = Spacer.entries.map { "${stringResource(it.getNameResId())} (\"${it.symbol}\")" }
                        .toTypedArray(),
                    entryValues = Spacer.entries.map { it.name }.toTypedArray(),
                    state = appSaveNameSpacer,
                    onClick = onAppSaveNameSpacer
                )
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
                    icon = Icons.Default.FolderZip,
                    name = R.string.backup_mode_xapk,
                    summary = if (isBackupModeXapk) R.string.backup_mode_xapk_summary_active else R.string.backup_mode_xapk_summary_inactive,
                    state = isBackupModeXapk,
                    onClick = onBackupModeXapk
                )
            }
            preferenceCategoryItemMiddle {
                ListPreference(
                    enabled = isBackupModeXapk,
                    name = stringResource(id = R.string.backup_apk_bundle_file_ending_title),
                    summary = stringResource(id = R.string.backup_apk_bundle_file_ending_summary),
                    entries = arrayOf(FileUtil.FileInfo.APKS.name, FileUtil.FileInfo.XAPK.name),
                    entryValues = arrayOf(
                        FileUtil.FileInfo.APKS.suffix, FileUtil.FileInfo.XAPK.suffix
                    ),
                    state = bundleFileInfo,
                    onClick = onBundleFileInfo
                )
            }
            preferenceCategoryItemMiddle {
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
                )
            }
            preferenceCategoryItemMiddle {
                Preference(
                    name = R.string.googleplay,
                    icon = Icons.Default.Shop,
                    summary = R.string.googleplay_summary,
                    onClick = onGooglePlay
                )
            }
            preferenceCategoryItemMiddle {
                Preference(
                    name = R.string.privacy_policy_title,
                    icon = Icons.Default.PrivacyTip,
                    onClick = onPrivacyPolicy
                )
            }
            preferenceCategoryItemMiddle {
                Preference(
                    name = R.string.terms_title, icon = Icons.Default.Info, onClick = onTerms
                )
            }
            preferenceCategoryItemMiddle {
                Preference(
                    name = stringResource(id = R.string.oss_dependencies_title),
                    icon = Icons.Default.LocalLibrary,
                    onClick = ossDependencies
                )
            }
            preferenceCategoryItemBottom {
                Preference(
                    name = stringResource(
                        id = R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE
                    ),
                    enabled = false,
                    onClick = {},
                )
            }
        }
    }
}
