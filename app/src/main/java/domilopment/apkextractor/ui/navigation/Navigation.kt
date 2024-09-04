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
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.android.play.core.appupdate.AppUpdateManager
import domilopment.apkextractor.MainActivity
import domilopment.apkextractor.data.repository.analytics.LocalAnalyticsHelper
import domilopment.apkextractor.data.repository.analytics.logScreenView
import domilopment.apkextractor.ui.Screen
import domilopment.apkextractor.ui.apkList.ApkListScreen
import domilopment.apkextractor.ui.appList.AppListScreen
import domilopment.apkextractor.ui.settings.SettingsScreen
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
        navController = navController,
        startDestination = Screen.AppList.route,
        popEnterTransition = {
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
        },
        popExitTransition = {
            scaleOut(targetScale = 0.9f) + fadeOut(
                animationSpec = tween(
                    durationMillis = 35,
                    easing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f),
                ),
            ) + slideOutHorizontally(targetOffsetX = { it + (it / 2) })
        },
        modifier = modifier
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
            }, showSnackbar = { showSnackbar(it) })
        }
        composable(Screen.Settings.route) {
            val model = hiltViewModel<SettingsScreenViewModel>()

            SettingsScreen(
                model = model,
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
