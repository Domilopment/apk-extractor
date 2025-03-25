package domilopment.apkextractor.ui.settings.safeFile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import domilopment.apkextractor.ui.navigation.Route
import domilopment.apkextractor.ui.Screen
import domilopment.apkextractor.ui.viewModels.SettingsScreenViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsSaveFileScreen(
    model: SettingsScreenViewModel,
    onBackClicked: () -> Unit,
) {
    val uiState by model.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = Unit) {
        Route.SettingsSaveFile.buttons.onEach { button ->
            when (button) {
                Screen.ScreenActions.NavigationIcon -> onBackClicked()
                else -> Unit
            }
        }.launchIn(this)
    }

    SettingsSaveFileContent(
        appSaveName = uiState.saveName,
        onAppSaveName = model::setAppSaveName,
        appSaveNameSpacer = uiState.saveNameSpacer.name,
        onAppSaveNameSpacer = model::setAppSaveNameSpacer,
        isBackupModeApkBundle = uiState.backupModeApkBundle,
        onBackupModeApkBundle = model::setBackupModeApkBundle,
        bundleFileInfo = uiState.bundleFileInfo.suffix,
        onBundleFileInfo = model::setBundleFileInfo
    )
}
