package domilopment.apkextractor.ui.settings.safeFile

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.settings.preferences.APKNamePreference
import domilopment.apkextractor.ui.settings.preferences.ListPreference
import domilopment.apkextractor.ui.settings.preferences.SwitchPreferenceCompat
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemBottom
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemMiddle
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemTop
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.settings.Spacer
import domilopment.apkextractor.utils.settings.getNameResId

@Composable
fun SettingsSaveFileContent(
    appSaveName: Set<String>,
    onAppSaveName: (Set<String>) -> Unit,
    appSaveNameSpacer: String,
    onAppSaveNameSpacer: (String) -> Unit,
    isBackupModeApkBundle: Boolean,
    onBackupModeApkBundle: (Boolean) -> Unit,
    bundleFileInfo: String,
    onBundleFileInfo: (String) -> Unit,
) {
    LazyColumn(
        state = rememberLazyListState(),
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
            .union(WindowInsets(left = 8.dp, right = 8.dp)).asPaddingValues()
    ) {
        preferenceCategoryItemTop {
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
                icon = Icons.Default.SpaceBar,
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
                icon = Icons.Default.FolderZip,
                name = R.string.backup_mode_split_apk,
                summary = if (isBackupModeApkBundle) R.string.backup_mode_split_apk_summary_active else R.string.backup_mode_split_apk_summary_inactive,
                state = isBackupModeApkBundle,
                onClick = onBackupModeApkBundle
            )
        }
        preferenceCategoryItemBottom {
            ListPreference(
                enabled = isBackupModeApkBundle,
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
    }
}
