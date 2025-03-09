package domilopment.apkextractor.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import domilopment.apkextractor.ui.Screen
import domilopment.apkextractor.data.AppBarState
import domilopment.apkextractor.data.UiState

private const val CONTENT_KEY_ACTION = "Action"
private const val CONTENT_KEY_SEARCH = "Search"
private const val CONTENT_KEY_DEFAULT = "Default"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun APKExtractorBottomNavigation(
    items: List<Screen>,
    navController: NavHostController,
    appBarState: AppBarState,
    uiState: UiState,
    modifier: Modifier = Modifier,
    onNavigate: () -> Unit
) {
    val isImeVisible = WindowInsets.isImeVisible
    AnimatedContent(targetState = uiState, transitionSpec = {
        slideInVertically(
            animationSpec = tween(
                durationMillis = 100,
                delayMillis = 100,
            ), initialOffsetY = { it }) + fadeIn(
            animationSpec = tween(
                durationMillis = 100,
                delayMillis = 100,
            )
        ) togetherWith slideOutVertically(
            animationSpec = tween(
                durationMillis = 100, easing = LinearOutSlowInEasing
            ), targetOffsetY = { it }) + fadeOut(
            animationSpec = tween(durationMillis = 100, easing = LinearOutSlowInEasing)
        )
    }, label = "Bottom Navigation Content", contentKey = { state ->
        when (state) {
            is UiState.ActionMode -> CONTENT_KEY_ACTION
            is UiState.Search -> if (isImeVisible) CONTENT_KEY_SEARCH else CONTENT_KEY_DEFAULT
            is UiState.Default -> CONTENT_KEY_DEFAULT
        }
    }) { state ->
        when {
            state is UiState.ActionMode && appBarState.actionModeActions.isNotEmpty() -> ActionModeBar(
                items = appBarState.actionModeActions
            )

            state is UiState.Search && isImeVisible -> Spacer(
                modifier = Modifier.windowInsetsBottomHeight(WindowInsets.ime)
            )

            appBarState.hasBottomNavigation -> DefaultBottomNavigation(
                items = items,
                navController = navController,
                modifier = modifier,
                onNavigate = onNavigate
            )
        }
    }
}

@Composable
private fun DefaultBottomNavigation(
    items: List<Screen>,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onNavigate: () -> Unit
) {
    NavigationBar(modifier = modifier) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                Icon(
                    imageVector = item.icon, contentDescription = null
                )
            },
                label = {
                    Text(
                        text = stringResource(id = item.routeNameRes),
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                },
                selected = currentDestination?.hierarchy?.any { it.hasRoute(item::class) } == true,
                onClick = {
                    onNavigate()
                    navController.navigate(item) {
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
private fun ActionModeBar(modifier: Modifier = Modifier, items: List<BottomBarItem>) {
    Surface(
        modifier = modifier, color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(insets = NavigationBarDefaults.windowInsets)
                .defaultMinSize(minHeight = 80.0.dp)
                .selectableGroup(),
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