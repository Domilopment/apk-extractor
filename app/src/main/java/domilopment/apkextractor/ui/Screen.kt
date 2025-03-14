package domilopment.apkextractor.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.vector.ImageVector
import domilopment.apkextractor.R
import domilopment.apkextractor.data.IconResource
import domilopment.apkextractor.ui.actionBar.ActionMenuItem
import domilopment.apkextractor.ui.navigation.BottomBarItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Serializable

sealed interface Screen {
    @get:StringRes
    val routeNameRes: Int
    val icon: ImageVector
    val appBarNavIcon: NavigationIcon?

    @get:StringRes
    val appBarTitleRes: Int
    val isSearchable: Boolean
    val appBarActions: List<ActionMenuItem>
    val hasBottomBar: Boolean
    val bottomBarActions: List<BottomBarItem>

    enum class ScreenActions {
        NavigationIcon, Refresh, Settings, FilterList, Sort, OpenExplorer, Share, Save
    }

    data class NavigationIcon(val icon: IconResource, val onClick: (() -> Unit)? = null)

    @Serializable
    data object AppList : Screen {
        override val routeNameRes = R.string.menu_show_app_list
        override val icon = Icons.Default.Apps
        override val appBarNavIcon =
            NavigationIcon(icon = IconResource.DrawableIcon(R.drawable.app_bar_icon))
        override val appBarTitleRes = R.string.app_name
        override val isSearchable = true
        override val hasBottomBar = true
        override val appBarActions = listOf(
            ActionMenuItem.IconMenuItem.ShownIfRoom(
                titleRes = R.string.filter_title,
                onClick = { _buttons.tryEmit(ScreenActions.FilterList) },
                icon = Icons.Default.FilterList,
                contentDescription = null,
            ),
            ActionMenuItem.IconMenuItem.ShownIfRoom(
                titleRes = R.string.menu_refresh_app_list,
                onClick = {
                    _buttons.tryEmit(ScreenActions.Refresh)
                },
                icon = Icons.Default.Refresh, contentDescription = null,
            ),
            ActionMenuItem.IconMenuItem.ShownIfRoom(
                titleRes = R.string.action_settings,
                onClick = {
                    _buttons.tryEmit(ScreenActions.Settings)
                },
                icon = Icons.Default.Settings, contentDescription = null,
            ),
        )
        override val bottomBarActions = listOf(
            BottomBarItem(
                icon = Icons.Default.Save, onClick = { _buttons.tryEmit(ScreenActions.Save) },
            ),
            BottomBarItem(
                icon = Icons.Default.Share, onClick = { _buttons.tryEmit(ScreenActions.Share) },
            ),
        )

        private val _buttons = MutableSharedFlow<ScreenActions>(extraBufferCapacity = 1)
        val buttons: Flow<ScreenActions> = _buttons.asSharedFlow()
    }

    @Serializable
    data object ApkList : Screen {
        override val routeNameRes = R.string.menu_show_save_dir
        override val icon = Icons.Default.Folder
        override val appBarNavIcon =
            NavigationIcon(icon = IconResource.DrawableIcon(R.drawable.app_bar_icon))
        override val appBarTitleRes = R.string.app_name
        override val isSearchable = true
        override val hasBottomBar = true
        override val appBarActions = listOf(
            ActionMenuItem.IconMenuItem.ShownIfRoom(
                titleRes = R.string.menu_sort_apk,
                onClick = { _buttons.tryEmit(ScreenActions.Sort) },
                icon = Icons.AutoMirrored.Filled.Sort, contentDescription = null,
            ),
            ActionMenuItem.IconMenuItem.ShownIfRoom(
                titleRes = R.string.menu_show_open_documents,
                onClick = { _buttons.tryEmit(ScreenActions.OpenExplorer) },
                icon = Icons.Default.Folder, contentDescription = null,
            ),
            ActionMenuItem.IconMenuItem.ShownIfRoom(
                titleRes = R.string.menu_refresh_apk_list,
                onClick = { _buttons.tryEmit(ScreenActions.Refresh) },
                icon = Icons.Default.Refresh, contentDescription = null,
            ),
            ActionMenuItem.IconMenuItem.ShownIfRoom(
                titleRes = R.string.action_settings,
                onClick = { _buttons.tryEmit(ScreenActions.Settings) },
                icon = Icons.Default.Settings, contentDescription = null,
            ),
        )
        override val bottomBarActions = emptyList<BottomBarItem>()

        private val _buttons = MutableSharedFlow<ScreenActions>(extraBufferCapacity = 1)
        val buttons: Flow<ScreenActions> = _buttons.asSharedFlow()
    }

    @Serializable
    data object Settings : Screen {
        override val routeNameRes = R.string.title_activity_settings
        override val icon = Icons.Default.Settings
        override val appBarNavIcon =
            NavigationIcon(icon = IconResource.VectorIcon(imageVector = Icons.AutoMirrored.Filled.ArrowBack)) {
                _buttons.tryEmit(ScreenActions.NavigationIcon)
            }
        override val appBarTitleRes = R.string.title_activity_settings
        override val isSearchable = false
        override val hasBottomBar = false
        override val appBarActions = emptyList<ActionMenuItem>()
        override val bottomBarActions = emptyList<BottomBarItem>()

        private val _buttons = MutableSharedFlow<ScreenActions>(extraBufferCapacity = 1)
        val buttons: Flow<ScreenActions> = _buttons.asSharedFlow()
    }

    companion object {
        fun getScreen(route: String?): Screen? = when (route) {
            AppList.serializer().descriptor.serialName -> AppList
            ApkList.serializer().descriptor.serialName -> ApkList
            Settings.serializer().descriptor.serialName -> Settings
            else -> null
        }
    }
}
