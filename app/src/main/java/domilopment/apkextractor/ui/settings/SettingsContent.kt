package domilopment.apkextractor.ui.settings

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.play.core.appupdate.AppUpdateInfo
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.R
import domilopment.apkextractor.data.SettingsScreenAppAutoBackUpListState

@Composable
fun SettingsContent(
    appUpdateInfo: AppUpdateInfo?,
    isUpdateAvailable: Boolean,
    onUpdateAvailable: () -> Unit,
    onChooseSaveDir: () -> Unit,
    appSaveName: State<Set<String>>,
    onAppSaveName: (Set<String>) -> Unit,
    autoBackupService: State<Boolean>,
    onAutoBackupService: (Boolean) -> Unit,
    isSelectAutoBackupApps: Boolean,
    autoBackupListApps: SettingsScreenAppAutoBackUpListState,
    autoBackupList: State<Set<String>>,
    onAutoBackupList: (Set<String>) -> Unit,
    nightMode: State<Int>,
    onNightMode: (Int) -> Unit,
    dynamicColors: State<Boolean>,
    isDynamicColors: Boolean,
    onDynamicColors: (Boolean) -> Unit,
    language: State<String>,
    languageLocaleMap: Map<String?, String>,
    onLanguage: (String) -> Unit,
    rightSwipeAction: State<String>,
    onRightSwipeAction: (String) -> Unit,
    leftSwipeAction: State<String>,
    onLeftSwipeAction: (String) -> Unit,
    batteryOptimization: State<Boolean>,
    onBatteryOptimization: (Boolean) -> Unit,
    checkUpdateOnStart: State<Boolean>,
    onCheckUpdateOnStart: (Boolean) -> Unit,
    onClearCache: () -> Unit,
    onGitHub: () -> Unit,
    onGooglePlay: () -> Unit,
    onPrivacyPolicy: () -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (appUpdateInfo != null) Preference(
            name = R.string.update_available_title,
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)
            ),
            summary = R.string.update_available_summary,
            isPreferenceVisible = isUpdateAvailable,
            onClick = onUpdateAvailable
        )
        PreferenceCategory(title = R.string.save_header) {
            Preference(
                name = R.string.choose_save_dir,
                summary = R.string.choose_save_dir_summary,
                icon = Icons.Default.Folder,
                onClick = onChooseSaveDir
            )
            APKNamePreference(
                name = R.string.app_save_name,
                summary = R.string.app_save_name_summary,
                entries = R.array.app_save_name_names,
                entryValues = R.array.app_save_name_values,
                state = appSaveName,
                onClick = onAppSaveName
            )
            SwitchPreferenceCompat(
                name = R.string.auto_backup,
                summary = R.string.auto_backup_summary,
                state = autoBackupService,
                onClick = onAutoBackupService
            )
            MultiSelectListPreference(
                name = stringResource(id = R.string.auto_backup_app_list),
                enabled = isSelectAutoBackupApps,
                entries = autoBackupListApps.entries,
                entryValues = autoBackupListApps.entryValues,
                summary = stringResource(id = R.string.auto_backup_app_list_summary),
                state = autoBackupList,
                onClick = onAutoBackupList
            )
        }
        PreferenceCategory(title = R.string.appearance) {
            ListPreference(
                name = R.string.ui_mode,
                summary = R.string.ui_mode_summary,
                icon = Icons.Default.ModeNight,
                entries = R.array.ui_mode_names,
                entryValues = R.array.ui_mode_values,
                state = nightMode,
                onClick = onNightMode
            )
            SwitchPreferenceCompat(
                name = R.string.use_material_you,
                summary = R.string.use_material_you_summary,
                state = dynamicColors,
                isVisible = isDynamicColors,
                onClick = onDynamicColors
            )
            ListPreference(
                name = stringResource(id = R.string.locale_list_title),
                summary = stringResource(
                    id = R.string.locale_list_summary, languageLocaleMap.getValue(language.value)
                ),
                entries = stringArrayResource(id = R.array.locale_list_names),
                entryValues = stringArrayResource(id = R.array.locale_list_values),
                state = language,
                onClick = onLanguage
            )
        }
        PreferenceCategory(title = R.string.advanced) {
            ListPreference(
                name = R.string.apk_swipe_action_right_title,
                summary = R.string.apk_swipe_action_right_summary,
                entries = R.array.apk_swipe_options_entries,
                entryValues = R.array.apk_swipe_options_values,
                state = rightSwipeAction,
                onClick = onRightSwipeAction
            )
            ListPreference(
                name = R.string.apk_swipe_action_left_title,
                summary = R.string.apk_swipe_action_left_summary,
                entries = R.array.apk_swipe_options_entries,
                entryValues = R.array.apk_swipe_options_values,
                state = leftSwipeAction,
                onClick = onLeftSwipeAction
            )
            SwitchPreferenceCompat(
                name = R.string.ignore_battery_optimization_title,
                summary = R.string.ignore_battery_optimization_summary,
                icon = Icons.Default.BatteryStd,
                state = batteryOptimization,
                onClick = onBatteryOptimization
            )
            SwitchPreferenceCompat(
                name = R.string.check_update_on_start_title,
                summary = R.string.check_update_on_start_summary,
                state = checkUpdateOnStart,
                onClick = onCheckUpdateOnStart
            )
            Preference(
                name = R.string.clear_cache,
                summary = R.string.clear_cache_summary,
                onClick = onClearCache
            )
        }
        PreferenceCategory(title = R.string.info_header) {
            Preference(
                name = R.string.github, summary = R.string.github_summary, onClick = onGitHub
            )
            Preference(
                name = R.string.googleplay,
                summary = R.string.googleplay_summary,
                onClick = onGooglePlay
            )
            Preference(name = R.string.privacy_policy_title, onClick = onPrivacyPolicy)
            Preference(name = stringResource(id = R.string.version, BuildConfig.VERSION_NAME),
                enabled = false,
                onClick = {})
        }
    }
}