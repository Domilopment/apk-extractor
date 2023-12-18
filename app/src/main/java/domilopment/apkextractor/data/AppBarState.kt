package domilopment.apkextractor.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import domilopment.apkextractor.ui.navigation.BottomBarItem
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.Screen
import domilopment.apkextractor.ui.actionBar.ActionMenuItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Stable
class AppBarState(
    private val navController: NavController,
    scope: CoroutineScope,
) {
    init {
        navController.currentBackStackEntryFlow
            .distinctUntilChanged()
            .onEach { backStackEntry ->
                val route = backStackEntry.destination.route
                currentScreen = getScreen(route)
            }
            .launchIn(scope)
    }

    var currentScreen by mutableStateOf<Screen?>(null)
        private set

    val title: Int
        get() = currentScreen?.appBarTitleRes ?: R.string.app_name

    val isBackArrow: Boolean
        get() = currentScreen?.appBarNavIcon ?: true

    val onBackArrowClick: () -> Unit
        get() = { navController.navigateUp() }

    val isSearchable: Boolean
        get() = currentScreen?.isSearchable ?: false

    val hasBottomNavigation: Boolean
        get() = currentScreen?.hasBottomBar ?: false

    val actions: List<ActionMenuItem>
        get() = currentScreen?.appBarActions.orEmpty()

    val actionModeActions: List<BottomBarItem>
        get() = currentScreen?.bottomBarActions.orEmpty()

    private fun getScreen(route: String?): Screen? = when (route) {
        Screen.AppList.route -> Screen.AppList
        Screen.ApkList.route -> Screen.ApkList
        Screen.Settings.route -> Screen.Settings
        else -> null
    }
}

@Composable
fun rememberAppBarState(
    navController: NavController,
    scope: CoroutineScope = rememberCoroutineScope()
) = remember { AppBarState(navController, scope) }