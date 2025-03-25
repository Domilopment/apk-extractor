package domilopment.apkextractor.ui.bottomBar

import android.app.Activity
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import domilopment.apkextractor.data.AppBarState
import domilopment.apkextractor.data.UiState
import domilopment.apkextractor.ui.DeviceTypeUtils
import domilopment.apkextractor.ui.bottomBar.BottomBarItem

private const val CONTENT_KEY_ACTION = "Action"
private const val CONTENT_KEY_SEARCH = "Search"
private const val CONTENT_KEY_DEFAULT = "Default"

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
            is UiState.ActionMode -> CONTENT_KEY_ACTION
            is UiState.Search -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && isImeVisible) CONTENT_KEY_SEARCH else CONTENT_KEY_DEFAULT
            is UiState.Default -> CONTENT_KEY_DEFAULT
        }
    }) { state ->
        when {
            state is UiState.ActionMode && DeviceTypeUtils.isPhoneBars && appBarState.actionModeActions.isNotEmpty() -> ActionModeBar(
                items = appBarState.actionModeActions
            )

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && state is UiState.Search && isImeVisible -> Spacer(
                modifier = Modifier.windowInsetsBottomHeight(WindowInsets.ime)
            )
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val view = LocalView.current
            if (!view.isInEditMode) {
                val color = BottomAppBarDefaults.containerColor
                SideEffect {
                    val window = (view.context as Activity).window
                    window.navigationBarColor = color.toArgb()
                }
            }
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