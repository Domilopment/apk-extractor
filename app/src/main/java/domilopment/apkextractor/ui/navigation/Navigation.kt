package domilopment.apkextractor.ui.navigation

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.android.play.core.appupdate.AppUpdateManager
import domilopment.apkextractor.MainActivity
import domilopment.apkextractor.data.repository.analytics.AnalyticsRepository
import domilopment.apkextractor.ui.Screen
import domilopment.apkextractor.ui.apkList.ApkListScreen
import domilopment.apkextractor.ui.appList.AppListScreen
import domilopment.apkextractor.ui.settings.SettingsScreen
import domilopment.apkextractor.ui.viewModels.ApkListViewModel
import domilopment.apkextractor.ui.viewModels.AppListViewModel
import domilopment.apkextractor.ui.viewModels.SettingsScreenViewModel
import domilopment.apkextractor.utils.MySnackbarVisuals
import timber.log.Timber

@Composable
fun ApkExtractorNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    analyticsEventLogger: (name: String, bundle: Bundle) -> Unit,
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
    val context = LocalContext.current
    DisposableEffect(key1 = navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val label =
                Screen.getScreen(destination.route)?.routeNameRes?.let { context.getString(it) }

            Timber.d("Navigated to: $label")

            val bundle = Bundle().apply {
                putString(AnalyticsRepository.Param.SCREEN_NAME, label)
                putString(AnalyticsRepository.Param.SCREEN_CLASS, MainActivity::class.simpleName)
            }
            analyticsEventLogger(AnalyticsRepository.Events.SCREEN_VIEW, bundle)
        }

        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }

    }
    NavHost(
        navController = navController, startDestination = Screen.AppList.route, modifier = modifier
    ) {
        composable(Screen.AppList.route) {
            val model = hiltViewModel<AppListViewModel>()

            AppListScreen(
                model = model,
                searchString = searchQuery,
                isActionMode = isActionMode,
                onNavigate = {
                    navController.navigate(Screen.Settings.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                analyticsEventLogger = analyticsEventLogger,
                showSnackbar = showSnackbar,
                onTriggerActionMode = onTriggerActionMode,
                isActionModeAllItemsSelected = isActionModeAllItemsSelected,
                onAppSelection = onAppSelection
            )
        }
        composable(Screen.ApkList.route) {
            val model = hiltViewModel<ApkListViewModel>()

            ApkListScreen(model = model, searchString = searchQuery, onNavigate = {
                navController.navigate(Screen.Settings.route) {
                    launchSingleTop = true
                    restoreState = true
                }
            }, analyticsEventLogger = analyticsEventLogger, showSnackbar = { showSnackbar(it) })
        }
        composable(Screen.Settings.route) {
            val model = hiltViewModel<SettingsScreenViewModel>()

            SettingsScreen(
                model = model,
                analyticsEventLogger = analyticsEventLogger,
                showSnackbar = showSnackbar,
                onBackClicked = {
                    navController.popBackStack(
                        Screen.Settings.route, inclusive = true, saveState = true
                    )
                },
                chooseSaveDir = chooseSaveDir,
                appUpdateManager = appUpdateManager,
                inAppUpdateResultLauncher = inAppUpdateResultLauncher
            )
        }
    }
}
