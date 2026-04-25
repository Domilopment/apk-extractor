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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import domilopment.apkextractor.data.AppBarState
import domilopment.apkextractor.data.UiState
import domilopment.apkextractor.ui.DeviceTypeUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ApkExtractorNavigation(
    navigationItems: Map<Route, TopLevelNavItem>,
    navigationState: NavigationState<Route>,
    navigator: Navigator<Route>,
    appBarState: AppBarState,
    uiState: UiState,
    modifier: Modifier = Modifier,
    onNavigate: () -> Unit,
    content: @Composable (() -> Unit),
) {
    ApkExtractorNavigationSuiteScaffold(
        navigationItems = navigationItems,
        navigationState = navigationState,
        navigator = navigator,
        modifier = modifier,
        showNavigationSuite = ((uiState !is UiState.ActionMode && !WindowInsets.isImeVisible) || DeviceTypeUtils.isTabletBars) && appBarState.hasNavigation,
        onNavigate = onNavigate,
        content = content
    )
}

@Composable
fun ApkExtractorNavigationSuiteScaffold(
    navigationItems: Map<Route, TopLevelNavItem>,
    navigationState: NavigationState<Route>,
    navigator: Navigator<Route>,
    modifier: Modifier = Modifier,
    showNavigationSuite: Boolean = true,
    onNavigate: () -> Unit,
    content: @Composable (() -> Unit),
) {
    val state =
        rememberNavigationSuiteScaffoldState(initialValue = if (showNavigationSuite) NavigationSuiteScaffoldValue.Visible else NavigationSuiteScaffoldValue.Hidden)

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            navigationItems.forEach { (key, value) ->
                item(
                    selected = key == navigationState.topLevelRoute,
                    onClick = {
                        onNavigate()
                        navigator.navigate(key)
                    },
                    icon = {
                        Icon(
                            imageVector = value.icon, contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(id = value.nameResId),
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
