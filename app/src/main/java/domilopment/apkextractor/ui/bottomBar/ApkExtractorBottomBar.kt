package domilopment.apkextractor.ui.bottomBar

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import domilopment.apkextractor.data.AppBarState
import domilopment.apkextractor.data.UiState
import domilopment.apkextractor.ui.DeviceTypeUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ApkExtractorBottomBar(
    appBarState: AppBarState, uiState: UiState, modifier: Modifier = Modifier
) {
    when {
        uiState is UiState.ActionMode && DeviceTypeUtils.isPhoneBars && appBarState.actionModeActions.isNotEmpty() -> ActionModeBar(
            modifier = modifier, items = appBarState.actionModeActions
        )

        else -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActionModeBar(modifier: Modifier = Modifier, items: List<BottomBarItem>) {
    FlexibleBottomAppBar(
        modifier = modifier,
        horizontalArrangement = BottomAppBarDefaults.FlexibleFixedHorizontalArrangement
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