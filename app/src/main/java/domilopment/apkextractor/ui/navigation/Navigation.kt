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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.google.android.play.core.appupdate.AppUpdateManager
import domilopment.apkextractor.MainActivity
import domilopment.apkextractor.data.repository.analytics.LocalAnalyticsHelper
import domilopment.apkextractor.data.repository.analytics.logScreenView
import domilopment.apkextractor.ui.apkList.ApkListScreen
import domilopment.apkextractor.ui.appList.AppListScreen
import domilopment.apkextractor.ui.settings.about.SettingsAboutScreen
import domilopment.apkextractor.ui.settings.autoBackup.SettingsAutoBackupScreen
import domilopment.apkextractor.ui.settings.dataCollection.SettingsDataCollectionScreen
import domilopment.apkextractor.ui.settings.home.SettingsHomeScreen
import domilopment.apkextractor.ui.settings.swipeAction.SettingsSwipeActionScreen
import domilopment.apkextractor.ui.settings.safeFile.SettingsSaveFileScreen
import domilopment.apkextractor.ui.viewModels.ApkListViewModel
import domilopment.apkextractor.ui.viewModels.AppListViewModel
import domilopment.apkextractor.ui.viewModels.SettingsScreenViewModel
import domilopment.apkextractor.utils.MySnackbarVisuals

@Composable
fun ApkExtractorNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    showSnackbar: (MySnackbarVisuals) -> Unit,
    searchQuery: String,
    onTriggerActionMode: () -> Unit,
    isActionMode: Boolean,
    isActionModeAllItemsSelected: Boolean,
    onAppSelection: (Boolean, Int) -> Unit,
    chooseSaveDir: ManagedActivityResultLauncher<Uri?, Uri?>,
    appUpdateManager: AppUpdateManager,
    inAppUpdateResultLauncher: ActivityResultLauncher<IntentSenderRequest>
) {
    val analytics = LocalAnalyticsHelper.current
    DisposableEffect(key1 = navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            analytics.logScreenView(destination.route, MainActivity::class.simpleName)
        }

        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    NavHost(
        navController = navController, startDestination = Route.AppList, popEnterTransition = {
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
        )
    }, popExitTransition = {
        scaleOut(targetScale = 0.9f) + fadeOut(
            animationSpec = tween(
                durationMillis = 35,
                easing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f),
            ),
        ) + slideOutHorizontally(targetOffsetX = { it + (it / 2) })
    }, modifier = modifier
    ) {
        composable<Route.AppList> {
            val model = hiltViewModel<AppListViewModel>()

            AppListScreen(
                model = model,
                searchString = searchQuery,
                isActionMode = isActionMode,
                showSnackbar = showSnackbar,
                onTriggerActionMode = onTriggerActionMode,
                isActionModeAllItemsSelected = isActionModeAllItemsSelected,
                onAppSelection = onAppSelection
            )
        }

        composable<Route.ApkList> {
            val model = hiltViewModel<ApkListViewModel>()

            ApkListScreen(
                model = model, searchString = searchQuery, showSnackbar = { showSnackbar(it) })
        }

        navigation<Graph.Settings>(startDestination = Route.SettingsHome) {
            composable<Route.SettingsHome> { backStackEntry ->
                val model = backStackEntry.sharedViewModel<SettingsScreenViewModel>(navController)

                SettingsHomeScreen(
                    model = model,
                    showSnackbar = showSnackbar,
                    onSaveFileSettings = {
                        navController.navigate(Route.SettingsSaveFile)
                    },
                    onAutoBackupSettings = {
                        navController.navigate(Route.SettingsAutoBackup)
                    },
                    onSwipeActionSettings = {
                        navController.navigate(Route.SettingsInteractions)
                    },
                    onDataCollectionSettings = {
                        navController.navigate(Route.SettingsDataCollection)
                    },
                    onAboutSettings = {
                        navController.navigate(Route.SettingsAbout)
                    },
                    chooseSaveDir = chooseSaveDir,
                    appUpdateManager = appUpdateManager,
                    inAppUpdateResultLauncher = inAppUpdateResultLauncher
                )
            }

            composable<Route.SettingsSaveFile> { backStackEntry ->
                val model = backStackEntry.sharedViewModel<SettingsScreenViewModel>(navController)

                SettingsSaveFileScreen(model = model, onBackClicked = {
                    navController.popBackStack()
                })
            }

            composable<Route.SettingsAutoBackup> { backStackEntry ->
                val model = backStackEntry.sharedViewModel<SettingsScreenViewModel>(navController)

                SettingsAutoBackupScreen(
                    model = model, showSnackbar = showSnackbar, onBackClicked = {
                        navController.popBackStack()
                    })
            }

            composable<Route.SettingsInteractions> { backStackEntry ->
                val model = backStackEntry.sharedViewModel<SettingsScreenViewModel>(navController)

                SettingsSwipeActionScreen(model = model, onBackClicked = {
                    navController.popBackStack()
                })
            }

            composable<Route.SettingsDataCollection> { backStackEntry ->
                val model = backStackEntry.sharedViewModel<SettingsScreenViewModel>(navController)

                SettingsDataCollectionScreen(model = model, onBackClicked = {
                    navController.popBackStack()
                })
            }

            composable<Route.SettingsAbout> {
                SettingsAboutScreen(onBackClicked = {
                    navController.popBackStack()
                })
            }
        }
    }
}

@Composable
private inline fun <reified VM : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): VM {
    val navGraphRoute = destination.parent?.route ?: return hiltViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return hiltViewModel(parentEntry)
}
