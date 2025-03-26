package domilopment.apkextractor.ui.bottomBar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import domilopment.apkextractor.data.AppBarState
import domilopment.apkextractor.data.UiState
import domilopment.apkextractor.ui.DeviceTypeUtils

private enum class BottomBarContentKeys {
    CONTENT_KEY_ACTION, CONTENT_KEY_SEARCH, CONTENT_KEY_DEFAULT
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ApkExtractorBottomBar(
    appBarState: AppBarState, uiState: UiState, modifier: Modifier = Modifier
) {
    val isImeVisible = WindowInsets.isImeVisible
    AnimatedContent(targetState = uiState, modifier = modifier, transitionSpec = {
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
            is UiState.ActionMode -> BottomBarContentKeys.CONTENT_KEY_ACTION
            is UiState.Search -> if (isImeVisible) BottomBarContentKeys.CONTENT_KEY_SEARCH else BottomBarContentKeys.CONTENT_KEY_DEFAULT
            is UiState.Default -> BottomBarContentKeys.CONTENT_KEY_DEFAULT
        }
    }) { state ->
        when {
            state is UiState.ActionMode && DeviceTypeUtils.isPhoneBars && appBarState.actionModeActions.isNotEmpty() -> ActionModeBar(
                items = appBarState.actionModeActions
            )

            else -> Unit
        }
    }
}

@Composable
private fun ActionModeBar(modifier: Modifier = Modifier, items: List<BottomBarItem>) {
    BottomAppBar(
        actions = {
            items.forEach { item ->
                IconButton(onClick = item.onClick) {
                    Icon(item.icon, contentDescription = null)
                }
            }
        },
        modifier = modifier,
    )
}