package domilopment.apkextractor.ui.navigation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import domilopment.apkextractor.data.AppBarState
import domilopment.apkextractor.data.UiState
import domilopment.apkextractor.ui.DeviceTypeUtils
import domilopment.apkextractor.ui.components.AnimatedNavigationSuiteScaffold

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ApkExtractorNavigation(
    navigationItems: List<TopLevelRoute<out Any>>,
    navController: NavHostController,
    appBarState: AppBarState,
    uiState: UiState,
    modifier: Modifier = Modifier,
    onNavigate: () -> Unit,
    content: @Composable (() -> Unit),
) {
    ApkExtractorNavigationSuiteScaffold(
        navigationItems = navigationItems,
        navController = navController,
        modifier = modifier,
        showNavigationSuite = ((uiState !is UiState.ActionMode && !WindowInsets.isImeVisible) || DeviceTypeUtils.isTabletBars) && appBarState.hasNavigation,
        onNavigate = onNavigate,
        content = content
    )
}

@Composable
fun ApkExtractorNavigationSuiteScaffold(
    navigationItems: List<TopLevelRoute<out Any>>,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    showNavigationSuite: Boolean = true,
    navigationRailHeader: @Composable (ColumnScope.() -> Unit)? = null,
    onNavigate: () -> Unit,
    content: @Composable (() -> Unit),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    AnimatedNavigationSuiteScaffold(
        navigationSuiteItems = {
            navigationItems.forEach { navigationItem ->
                item(
                    selected = currentDestination?.hierarchy?.any { it.hasRoute(navigationItem.route::class) } == true,
                    onClick = {
                        onNavigate()
                        navController.navigate(navigationItem.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = navigationItem.icon, contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(id = navigationItem.nameResId),
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2
                        )
                    })
            }

        },
        modifier = modifier,
        layoutType = if (showNavigationSuite) {
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
        } else {
            NavigationSuiteType.None
        },
        navigationRailHeader = navigationRailHeader,
        content = content,
    )

}
