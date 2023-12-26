package domilopment.apkextractor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import domilopment.apkextractor.ui.Screen
import domilopment.apkextractor.ui.apkList.ApkListScreen
import domilopment.apkextractor.ui.appList.AppListScreen
import domilopment.apkextractor.ui.settings.SettingsScreen
import domilopment.apkextractor.ui.viewModels.ApkListViewModel
import domilopment.apkextractor.ui.viewModels.AppListViewModel
import domilopment.apkextractor.ui.viewModels.ProgressDialogViewModel
import domilopment.apkextractor.ui.viewModels.SettingsFragmentViewModel
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
    onAppSelection: (Boolean, Int) -> Unit
) {
    NavHost(
        navController = navController, startDestination = Screen.AppList.route, modifier = modifier
    ) {
        composable(Screen.AppList.route) {
            val model = hiltViewModel<AppListViewModel>()
            val progressDialogViewModel = hiltViewModel<ProgressDialogViewModel>()

            AppListScreen(
                model = model,
                progressDialogModel = progressDialogViewModel,
                searchString = searchQuery,
                isActionMode = isActionMode,
                onNavigate = { navController.navigate(Screen.Settings.route) },
                showSnackbar = showSnackbar,
                onTriggerActionMode = onTriggerActionMode,
                isActionModeAllItemsSelected = isActionModeAllItemsSelected,
                onAppSelection = onAppSelection
            )
        }
        composable(Screen.ApkList.route) {
            val model = hiltViewModel<ApkListViewModel>()
            val progressDialogViewModel = hiltViewModel<ProgressDialogViewModel>()

            ApkListScreen(model = model,
                progressDialogViewModel = progressDialogViewModel,
                searchString = searchQuery,
                onNavigate = { navController.navigate(Screen.Settings.route) },
                showSnackbar = { showSnackbar(it) })
        }
        composable(Screen.Settings.route) {
            val model = viewModel<SettingsFragmentViewModel>()

            SettingsScreen(model = model)
        }
    }
}
