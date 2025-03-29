package domilopment.apkextractor.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import domilopment.apkextractor.ui.bottomBar.BottomBarItem
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.ScreenConfig
import domilopment.apkextractor.ui.actionBar.ActionMenuItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Stable
class AppBarState(
    navController: NavController,
    scope: CoroutineScope,
) {
    init {
        navController.currentBackStackEntryFlow.distinctUntilChanged().onEach { backStackEntry ->
            currentScreenConfig = ScreenConfig.getScreenConfig(backStackEntry.destination)
        }.launchIn(scope)
    }

    var currentScreenConfig by mutableStateOf<ScreenConfig?>(null)
        private set

    val title: Int
        get() = currentScreenConfig?.appBarTitleRes ?: R.string.app_name

    val hasNavigationIcon: Boolean
        get() = currentScreenConfig?.appBarNavIcon != null

    val navigationIcon: ScreenConfig.NavigationIcon?
        get() = currentScreenConfig?.appBarNavIcon

    val isSearchable: Boolean
        get() = currentScreenConfig?.isSearchable ?: false

    val hasNavigation: Boolean
        get() = currentScreenConfig?.hasNavigationBar ?: false

    val actions: List<ActionMenuItem>
        get() = currentScreenConfig?.appBarActions.orEmpty()

    val actionModeActions: List<BottomBarItem>
        get() = currentScreenConfig?.bottomBarActions.orEmpty()
}

@Composable
fun rememberAppBarState(
    navController: NavController, scope: CoroutineScope = rememberCoroutineScope()
) = remember { AppBarState(navController, scope) }