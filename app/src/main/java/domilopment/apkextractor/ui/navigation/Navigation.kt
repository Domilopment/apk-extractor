package domilopment.apkextractor.ui.navigation

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.google.android.play.core.appupdate.AppUpdateManager
import domilopment.apkextractor.ui.apkDetails.ApkDetailsScreen
import domilopment.apkextractor.ui.apkList.ApkListScreen
import domilopment.apkextractor.ui.appList.AppListScreen
import domilopment.apkextractor.ui.appDetails.AppDetailsScreen
import domilopment.apkextractor.ui.navigation.entryDecorators.SharedViewModelStoreNavEntryDecorator
import domilopment.apkextractor.ui.navigation.sceneStrategies.BottomSheetSceneStrategy
import domilopment.apkextractor.ui.settings.about.SettingsAboutScreen
import domilopment.apkextractor.ui.settings.autoBackup.SettingsAutoBackupScreen
import domilopment.apkextractor.ui.settings.dataCollection.SettingsDataCollectionScreen
import domilopment.apkextractor.ui.settings.donation.SettingsDonationScreen
import domilopment.apkextractor.ui.settings.home.SettingsHomeScreen
import domilopment.apkextractor.ui.settings.safeFile.SettingsSaveFileScreen
import domilopment.apkextractor.ui.settings.swipeAction.SettingsSwipeActionScreen
import domilopment.apkextractor.ui.viewModels.ApkDetailViewModel
import domilopment.apkextractor.ui.viewModels.ApkListViewModel
import domilopment.apkextractor.ui.viewModels.AppDetailViewModel
import domilopment.apkextractor.ui.viewModels.AppListViewModel
import domilopment.apkextractor.ui.viewModels.SettingsScreenViewModel
import domilopment.apkextractor.utils.MySnackbarVisuals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkExtractorNavDisplay(
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
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<Route>() }

    val entryProvider = entryProvider {
        entry<Route.AppList> {
            val model = hiltViewModel<AppListViewModel>()
            AppListScreen(
                model = model,
                searchString = searchQuery,
                isActionMode = isActionMode,
                showSnackbar = showSnackbar,
                appOetailsScreen = { navigator.navigate(Route.AppDetails(it)) },
                onTriggerActionMode = onTriggerActionMode,
                isActionModeAllItemsSelected = isActionModeAllItemsSelected,
                onAppSelection = onAppSelection,
                showAskForSaveDirDialog = showAskForSaveDirDialog
            )
        }

        entry<Route.AppDetails>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
            val model = hiltViewModel<AppDetailViewModel, AppDetailViewModel.Factory>(
                creationCallback = { factory ->
                    factory.create(key)
                })

            AppDetailsScreen(
                model = model,
                onDismissRequest = { navigator.goBack() },
                showSnackbar = showSnackbar,
                showAskForSaveDirDialog = showAskForSaveDirDialog
            )
        }

        entry<Route.ApkList> {
            val model = hiltViewModel<ApkListViewModel>()
            ApkListScreen(
                model = model,
                searchString = searchQuery,
                showSnackbar = { showSnackbar(it) },
                onApkClick = { navigator.navigate(Route.ApkDetails(it)) })
        }

        entry<Route.ApkDetails>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
            val model = hiltViewModel<ApkDetailViewModel, ApkDetailViewModel.Factory>(
                creationCallback = { factory ->
                    factory.create(key)
                })

            ApkDetailsScreen(
                model = model,
                onDismissRequest = { navigator.goBack() },
                showSnackbar = showSnackbar,
                showAskForSaveDirDialog = showAskForSaveDirDialog
            )
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

        entry<Route.SettingsSaveFile>(
            metadata = SharedViewModelStoreNavEntryDecorator.parent(
                Route.SettingsHome.toContentKey()
            )
        ) {
            val model = hiltViewModel<SettingsScreenViewModel>()
            SettingsSaveFileScreen(
                model = model, onBackClicked = { navigator.goBack() })
        }

        entry<Route.SettingsAutoBackup>(
            metadata = SharedViewModelStoreNavEntryDecorator.parent(
                Route.SettingsHome.toContentKey()
            )
        ) {
            val model = hiltViewModel<SettingsScreenViewModel>()
            SettingsAutoBackupScreen(
                model = model, showSnackbar = showSnackbar, onBackClicked = { navigator.goBack() })
        }

        entry<Route.SettingsSwipeAction>(
            metadata = SharedViewModelStoreNavEntryDecorator.parent(
                Route.SettingsHome.toContentKey()
            )
        ) {
            val model = hiltViewModel<SettingsScreenViewModel>()
            SettingsSwipeActionScreen(
                model = model, onBackClicked = { navigator.goBack() })
        }

        entry<Route.SettingsDataCollection>(
            metadata = SharedViewModelStoreNavEntryDecorator.parent(
                Route.SettingsHome.toContentKey()
            )
        ) {
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
        entries = navigationState.toEntries(entryProvider),
        modifier = modifier,
        sceneStrategies = listOf(bottomSheetStrategy),
        predictivePopTransitionSpec = {
            scaleIn(
                animationSpec = tween(
                    durationMillis = 100,
                    delayMillis = 35,
                ),
                initialScale = 1.1f,
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 100,
                    delayMillis = 35,
                ),
            ) + slideInHorizontally(
                animationSpec = tween(
                    durationMillis = 100,
                    delayMillis = 35,
                )
            ) togetherWith scaleOut(
                targetScale = 0.9f
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = 35,
                    easing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f),
                ),
            ) + slideOutHorizontally(
                targetOffsetX = { it + (it / 2) })
        },
        onBack = { navigator.goBack() })
}

fun NavKey.toContentKey() = this.toString()
