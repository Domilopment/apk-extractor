package domilopment.apkextractor.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import domilopment.apkextractor.data.IconResource
import domilopment.apkextractor.ui.actionBar.ActionMenuItem
import domilopment.apkextractor.ui.navigation.BottomBarItem
import kotlin.reflect.full.companionObjectInstance

sealed interface Screen {
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
        fun getScreen(navDestination: NavDestination): Screen? =
            Route::class.sealedSubclasses.find { navDestination.hasRoute(it) }
                ?.let { it.objectInstance ?: it.companionObjectInstance } as? Screen
    }
}
