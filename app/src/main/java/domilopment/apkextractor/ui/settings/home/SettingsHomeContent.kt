package domilopment.apkextractor.ui.settings.home

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.UpdateDisabled
import androidx.compose.material3.Icon
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
import domilopment.apkextractor.ui.settings.preferences.DialogPreference
import domilopment.apkextractor.ui.settings.preferences.ListPreference
import domilopment.apkextractor.ui.settings.preferences.Preference
import domilopment.apkextractor.ui.settings.preferences.preferenceCategory
import domilopment.apkextractor.ui.settings.preferences.SwitchPreferenceCompat
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemBottom
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemMiddle
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemSingle
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemTop
import domilopment.apkextractor.ui.tabletLazyListInsets

@Composable
fun SettingsHomeContent(
    appUpdateInfo: AppUpdateInfo?,
    isUpdateAvailable: Boolean,
    onUpdateAvailable: () -> Unit,
    onChooseSaveDir: () -> Unit,
    onSaveFileSettings: () -> Unit,
    onAutoBackupSettings: () -> Unit,
    nightMode: Int,
    onNightMode: (Int) -> Unit,
    dynamicColors: Boolean,
    isDynamicColors: Boolean,
    onDynamicColors: (Boolean) -> Unit,
    language: String,
    languageLocaleDisplayName: String,
    onLanguage: (String) -> Unit,
    onSwipeActionSettings: () -> Unit,
    checkUpdateOnStart: Boolean,
    onCheckUpdateOnStart: (Boolean) -> Unit,
    cacheSize: String,
    onClearCache: () -> Unit,
    onDataCollectionSettings: () -> Unit,
    dataCollectionDeleteDialogContent: @Composable (() -> Unit),
    onDeleteFirebaseInstallationsId: () -> Unit,
    onGitHub: () -> Unit,
    onGooglePlay: () -> Unit,
    onAboutSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.testTag("SettingsLazyColumn"),
        state = rememberLazyListState(),
        contentPadding = WindowInsets.tabletLazyListInsets.union(
            WindowInsets(left = 8.dp, right = 8.dp)
        ).asPaddingValues()
    ) {
        if (appUpdateInfo != null) preferenceCategoryItemSingle {
            Preference(
                name = R.string.update_available_title,
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
            preferenceCategoryItemBottom {
                Preference(
                    name = R.string.title_screen_auto_backup_settings,
                    icon = Icons.Default.Sync,
                    onClick = onAutoBackupSettings
                ) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                }
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

        preferenceCategory(title = R.string.gestures_and_interactions) {
            preferenceCategoryItemSingle {
                Preference(
                    name = stringResource(id = R.string.title_screen_swipe_action_settings),
                    icon = Icons.Default.Swipe,
                    onClick = onSwipeActionSettings
                ) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }

        preferenceCategory(title = R.string.app_options_header) {
            preferenceCategoryItemTop {
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
                Preference(
                    name = R.string.title_screen_data_collection_settings,
                    icon = Icons.Default.DataUsage,
                    onClick = onDataCollectionSettings
                ) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                }
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
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.OpenInNew,
                        contentDescription = null
                    )
                }
            }
            preferenceCategoryItemMiddle {
                Preference(
                    name = R.string.googleplay,
                    icon = Icons.Default.Shop,
                    summary = R.string.googleplay_summary,
                    onClick = onGooglePlay
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.OpenInNew,
                        contentDescription = null
                    )
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
