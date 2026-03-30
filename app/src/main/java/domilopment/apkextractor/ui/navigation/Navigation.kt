package domilopment.apkextractor.ui.navigation

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.google.android.play.core.appupdate.AppUpdateManager
import domilopment.apkextractor.MainActivity
import domilopment.apkextractor.data.repository.analytics.LocalAnalyticsHelper
import domilopment.apkextractor.data.repository.analytics.logScreenView
import domilopment.apkextractor.ui.apkList.ApkListScreen
import domilopment.apkextractor.ui.appList.AppListScreen
import domilopment.apkextractor.ui.settings.about.SettingsAboutScreen
import domilopment.apkextractor.ui.settings.autoBackup.SettingsAutoBackupScreen
import domilopment.apkextractor.ui.settings.dataCollection.SettingsDataCollectionScreen
import domilopment.apkextractor.ui.settings.donation.SettingsDonationScreen
import domilopment.apkextractor.ui.settings.home.SettingsHomeScreen
import domilopment.apkextractor.ui.settings.safeFile.SettingsSaveFileScreen
import domilopment.apkextractor.ui.settings.swipeAction.SettingsSwipeActionScreen
import domilopment.apkextractor.ui.viewModels.ApkListViewModel
import domilopment.apkextractor.ui.viewModels.AppListViewModel
import domilopment.apkextractor.ui.viewModels.SettingsScreenViewModel
import domilopment.apkextractor.utils.MySnackbarVisuals

@Composable
fun ApkExtractorNavHost(
    modifier: Modifier = Modifier,
    navigationState: NavigationState<Route>,
    navigator: Navigator<Route>,
    showSnackbar: (MySnackbarVisuals) -> Unit,
    searchQuery: String,
    onTriggerActionMode: () -> Unit,
    isActionMode: Boolean,
    isActionModeAllItemsSelected: Boolean,
    onAppSelection: (Boolean, Int) -> Unit,
    chooseSaveDir: ManagedActivityResultLauncher<Uri?, Uri?>,
    showAskForSaveDirDialog: (Boolean) -> Unit,
    appUpdateManager: AppUpdateManager,
    inAppUpdateResultLauncher: ActivityResultLauncher<IntentSenderRequest>
) {
    val analytics = LocalAnalyticsHelper.current
    val currentRoute by remember {
        derivedStateOf {
            navigationState.backStacks[navigationState.topLevelRoute]?.lastOrNull()
        }
    }

    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            analytics.logScreenView(route.toString(), MainActivity::class.simpleName)
        }
    }

    val entryProvider = entryProvider<Route> {
        entry<Route.AppList> {
            val model = hiltViewModel<AppListViewModel>()
            AppListScreen(
                model = model,
                searchString = searchQuery,
                isActionMode = isActionMode,
                showSnackbar = showSnackbar,
                onTriggerActionMode = onTriggerActionMode,
                isActionModeAllItemsSelected = isActionModeAllItemsSelected,
                onAppSelection = onAppSelection,
                showAskForSaveDirDialog = showAskForSaveDirDialog
            )
        }

        entry<Route.ApkList> {
            val model = hiltViewModel<ApkListViewModel>()
            ApkListScreen(
                model = model, searchString = searchQuery, showSnackbar = { showSnackbar(it) })
        }

        entry<Route.SettingsHome> {
            val model = hiltViewModel<SettingsScreenViewModel>()
            SettingsHomeScreen(
                model = model,
                showSnackbar = showSnackbar,
                onSaveFileSettings = { navigator.navigate(Route.SettingsSaveFile) },
                onAutoBackupSettings = { navigator.navigate(Route.SettingsAutoBackup) },
                onSwipeActionSettings = { navigator.navigate(Route.SettingsSwipeAction) },
                onDataCollectionSettings = { navigator.navigate(Route.SettingsDataCollection) },
                onAboutSettings = { navigator.navigate(Route.SettingsAbout) },
                onDonationSettings = { navigator.navigate(Route.SettingsDonation) },
                chooseSaveDir = chooseSaveDir,
                appUpdateManager = appUpdateManager,
                inAppUpdateResultLauncher = inAppUpdateResultLauncher
            )
        }

        entry<Route.SettingsSaveFile> {
            val model = hiltViewModel<SettingsScreenViewModel>()
            SettingsSaveFileScreen(
                model = model, onBackClicked = { navigator.goBack() })
        }

        entry<Route.SettingsAutoBackup> {
            val model = hiltViewModel<SettingsScreenViewModel>()
            SettingsAutoBackupScreen(
                model = model, showSnackbar = showSnackbar, onBackClicked = { navigator.goBack() })
        }

        entry<Route.SettingsSwipeAction> {
            val model = hiltViewModel<SettingsScreenViewModel>()
            SettingsSwipeActionScreen(
                model = model, onBackClicked = { navigator.goBack() })
        }

        entry<Route.SettingsDataCollection> {
            val model = hiltViewModel<SettingsScreenViewModel>()
            SettingsDataCollectionScreen(
                model = model, onBackClicked = { navigator.goBack() })
        }

        entry<Route.SettingsAbout> {
            SettingsAboutScreen(onBackClicked = { navigator.goBack() })
        }

        entry<Route.SettingsDonation> {
            SettingsDonationScreen(onBackClicked = { navigator.goBack() })
        }
    }

    NavDisplay(
        modifier = modifier,
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() })
}
