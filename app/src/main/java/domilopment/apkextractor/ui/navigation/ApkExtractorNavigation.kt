package domilopment.apkextractor.ui.navigation

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldValue
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

@OptIn(ExperimentalLayoutApi::class)
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
    onNavigate: () -> Unit,
    content: @Composable (() -> Unit),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val state =
        rememberNavigationSuiteScaffoldState(initialValue = if (showNavigationSuite) NavigationSuiteScaffoldValue.Visible else NavigationSuiteScaffoldValue.Hidden)

    NavigationSuiteScaffold(
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
        state = state,
        content = content,
    )

    LaunchedEffect(key1 = showNavigationSuite) {
        val newTarget =
            if (showNavigationSuite) NavigationSuiteScaffoldValue.Visible else NavigationSuiteScaffoldValue.Hidden
        if (state.targetValue != newTarget) when (newTarget) {
            NavigationSuiteScaffoldValue.Hidden -> state.hide()
            NavigationSuiteScaffoldValue.Visible -> state.show()
        }
    }
}
