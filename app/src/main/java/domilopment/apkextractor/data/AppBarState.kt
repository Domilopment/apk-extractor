package domilopment.apkextractor.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import domilopment.apkextractor.ui.bottomBar.BottomBarItem
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.ScreenConfig
import domilopment.apkextractor.ui.actionBar.ActionMenuItem
import domilopment.apkextractor.ui.navigation.NavigationState
import domilopment.apkextractor.ui.navigation.Route

@Stable
class AppBarState(private val navigationState: NavigationState<Route>) {
    val currentScreenConfig by derivedStateOf {
        val topKey = navigationState.backStacks[navigationState.topLevelRoute]?.lastOrNull()
        ScreenConfig.getScreenConfig(topKey)
    }

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
    navigationState: NavigationState<Route>
) = remember(navigationState) { AppBarState(navigationState) }
