package domilopment.apkextractor.ui.settings.swipeAction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import domilopment.apkextractor.data.repository.analytics.LocalAnalyticsHelper
import domilopment.apkextractor.data.repository.analytics.logItemClick
import domilopment.apkextractor.ui.Screen
import domilopment.apkextractor.ui.navigation.Route
import domilopment.apkextractor.ui.viewModels.SettingsScreenViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun SettingsSwipeActionScreen(
    model: SettingsScreenViewModel,
    onBackClicked: () -> Unit,
) {
    val analytics = LocalAnalyticsHelper.current

    val uiState by model.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = Unit) {
        Route.SettingsSwipeAction.buttons.onEach { button ->
            when (button) {
                Screen.ScreenActions.NavigationIcon -> onBackClicked()
                else -> Unit
            }
        }.launchIn(this)
    }

    SettingsSwipeActionContent(
        rightSwipeAction = uiState.rightSwipeAction,
        onRightSwipeAction = model::setRightSwipeAction,
        leftSwipeAction = uiState.leftSwipeAction,
        onLeftSwipeAction = model::setLeftSwipeAction,
        swipeActionCustomThreshold = uiState.swipeActionCustomThreshold,
        onSwipeActionCustomThreshold = {
            model.setSwipeActionCustomThreshold(it)
            analytics.logItemClick("SwitchPreference", "SwipeActionCustomThreshold")
        },
        swipeActionThresholdMod = uiState.swipeActionThresholdMod,
        onSwipeActionThresholdMod = {
            model.setSwipeActionThresholdMod(it)
            analytics.logItemClick("SeekBarPreference", "SwipeActionCustomMod")
        }
    )
}