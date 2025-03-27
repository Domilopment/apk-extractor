package domilopment.apkextractor.ui.settings.autoBackup

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.R
import domilopment.apkextractor.data.SettingsScreenAppAutoBackUpListState
import domilopment.apkextractor.ui.settings.preferences.MultiSelectListPreference
import domilopment.apkextractor.ui.settings.preferences.SwitchPreferenceCompat
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemBottom
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemMiddle
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemTop
import domilopment.apkextractor.ui.tabletLazyListInsets

@Composable
fun SettingsAutoBackupContent(
    autoBackupService: Boolean,
    onAutoBackupService: (Boolean) -> Unit,
    isSelectAutoBackupApps: Boolean,
    autoBackupListApps: SettingsScreenAppAutoBackUpListState,
    autoBackupList: Set<String>,
    onAutoBackupList: (Set<String>) -> Unit,
    batteryOptimization: Boolean,
    onBatteryOptimization: (Boolean) -> Unit,
) {
    LazyColumn(
        state = rememberLazyListState(), contentPadding = WindowInsets.tabletLazyListInsets.union(
            WindowInsets(left = 8.dp, right = 8.dp)
        ).asPaddingValues()
    ) {
        preferenceCategoryItemTop {
            SwitchPreferenceCompat(
                name = R.string.auto_backup,
                icon = if (autoBackupService) Icons.Default.Sync else Icons.Default.SyncDisabled,
                summary = R.string.auto_backup_summary,
                state = autoBackupService,
                onClick = onAutoBackupService
            )
        }
        preferenceCategoryItemMiddle {
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
        preferenceCategoryItemBottom {
            SwitchPreferenceCompat(
                name = R.string.ignore_battery_optimization_title,
                summary = R.string.ignore_battery_optimization_summary,
                icon = Icons.Default.BatteryStd,
                state = batteryOptimization,
                onClick = onBatteryOptimization
            )
        }
    }
}
