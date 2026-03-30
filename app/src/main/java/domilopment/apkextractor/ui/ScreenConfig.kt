package domilopment.apkextractor.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavKey
import domilopment.apkextractor.data.IconResource
import domilopment.apkextractor.ui.actionBar.ActionMenuItem
import domilopment.apkextractor.ui.bottomBar.BottomBarItem
import domilopment.apkextractor.ui.navigation.Route

interface ScreenConfig {
    val appBarNavIcon: NavigationIcon?

    @get:StringRes
    val appBarTitleRes: Int
    val isSearchable: Boolean
    val appBarActions: List<ActionMenuItem>
    val hasNavigationBar: Boolean
    val bottomBarActions: List<BottomBarItem>

    enum class ScreenActions {
        NavigationIcon, Refresh, FilterList, Sort, OpenExplorer, Share, Save
    }

    data class NavigationIcon(
        val icon: IconResource,
        val tint: @Composable (() -> Color)? = null,
        val onClick: (() -> Unit)? = null
    )

    companion object {
        /**
         * Returns the [ScreenConfig] associated with the [NavKey].
         * 1. Checks if the instance itself implements [ScreenConfig] (for data objects).
         * 2. Falls back to the companion object (for data classes).
         */
        fun getScreenConfig(navDestination: Route?): ScreenConfig? = navDestination?.config
    }
}
