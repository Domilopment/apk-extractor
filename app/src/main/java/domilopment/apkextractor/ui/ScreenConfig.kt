package domilopment.apkextractor.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import domilopment.apkextractor.data.IconResource
import domilopment.apkextractor.ui.actionBar.ActionMenuItem
import domilopment.apkextractor.ui.bottomBar.BottomBarItem
import domilopment.apkextractor.ui.navigation.Route.Screen
import kotlin.reflect.full.companionObjectInstance

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
        fun getScreenConfig(navDestination: NavDestination): ScreenConfig? =
            Screen::class.sealedSubclasses.find { navDestination.hasRoute(it) }
                ?.let { it.objectInstance ?: it.companionObjectInstance } as? ScreenConfig
    }
}