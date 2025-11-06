package domilopment.apkextractor.ui.bottomBar

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import domilopment.apkextractor.data.AppBarState
import domilopment.apkextractor.data.UiState
import domilopment.apkextractor.ui.DeviceTypeUtils

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ApkExtractorBottomBar(
    appBarState: AppBarState, uiState: UiState, modifier: Modifier = Modifier
) {
    val isPhoneBars = DeviceTypeUtils.isPhoneBars
    val isVisible by remember(uiState, appBarState, isPhoneBars) {
        derivedStateOf {
            uiState is UiState.ActionMode && isPhoneBars && appBarState.actionModeActions.isNotEmpty()
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = if (isVisible) 100 else 0,
            easing = FastOutSlowInEasing
        ),
        label = "BottomBarHeightAnimation",
    )

    if (animatedProgress > 0f) ActionModeBar(
        modifier = modifier.alpha(animatedProgress),
        items = appBarState.actionModeActions,
        expandedHeightDp = BottomAppBarDefaults.FlexibleBottomAppBarHeight * animatedProgress
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActionModeBar(
    modifier: Modifier = Modifier, items: List<BottomBarItem>, expandedHeightDp: Dp
) {
    FlexibleBottomAppBar(
        modifier = modifier,
        horizontalArrangement = BottomAppBarDefaults.FlexibleFixedHorizontalArrangement,
        expandedHeight = expandedHeightDp
    ) {
        val context = LocalContext.current
        AppBarRow {
            items.forEach { item ->
                clickableItem(
                    onClick = item.onClick,
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = context.getString(item.labelRes)
                )
            }
        }
    }
}