package domilopment.apkextractor.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import domilopment.apkextractor.ui.Screen
import domilopment.apkextractor.data.AppBarState

@Composable
fun APKExtractorBottomNavigation(
    items: List<Screen>,
    navController: NavHostController,
    appBarState: AppBarState,
    isActionMode: Boolean,
    modifier: Modifier = Modifier,
    onNavigate: () -> Unit
) {
    if (isActionMode && appBarState.actionModeActions.isNotEmpty()) ActionModeBar(appBarState.actionModeActions)
    else if (appBarState.hasBottomNavigation) NavigationBar(modifier = modifier) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { item ->
            NavigationBarItem(icon = {
                Icon(
                    imageVector = item.icon, contentDescription = null
                )
            },
                label = {
                    Text(
                        text = stringResource(id = item.routeNameRes)
                    )
                },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    onNavigate()
                    navController.navigate(item.route) {
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
                })
        }
    }
}

@Composable
private fun ActionModeBar(items: List<BottomBarItem>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp), color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { item ->
                OutlinedButton(
                    onClick = item.onClick,
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    border = BorderStroke(width = 2.dp, MaterialTheme.colorScheme.onPrimary)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}