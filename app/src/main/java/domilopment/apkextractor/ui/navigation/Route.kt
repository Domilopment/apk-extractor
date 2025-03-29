package domilopment.apkextractor.ui.navigation

import androidx.annotation.Keep
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import domilopment.apkextractor.R
import domilopment.apkextractor.data.IconResource
import domilopment.apkextractor.ui.ScreenConfig
import domilopment.apkextractor.ui.ScreenConfig.NavigationIcon
import domilopment.apkextractor.ui.ScreenConfig.ScreenActions
import domilopment.apkextractor.ui.actionBar.ActionMenuItem
import domilopment.apkextractor.ui.bottomBar.BottomBarItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Serializable


sealed interface Route {
    sealed interface Graph: Route {
        @Serializable
        data object Settings : Graph
    }

    @Keep
    sealed interface Screen : Route {
        @Keep
        @Serializable
        data object AppList : Screen, ScreenConfig {
            override val appBarNavIcon = NavigationIcon(
                icon = IconResource.DrawableIcon(R.drawable.app_bar_icon),
                tint = { MaterialTheme.colorScheme.primary })
            override val appBarTitleRes = R.string.app_name
            override val isSearchable = true
            override val hasNavigationBar = true
            override val appBarActions = listOf(
                ActionMenuItem.IconMenuItem.ShownIfRoom(
                    titleRes = R.string.filter_title,
                    onClick = { _buttons.tryEmit(ScreenActions.FilterList) },
                    icon = Icons.Default.FilterList,
                    contentDescription = null,
                ),
                ActionMenuItem.IconMenuItem.ShownIfRoom(
                    titleRes = R.string.menu_refresh_app_list,
                    onClick = { _buttons.tryEmit(ScreenActions.Refresh) },
                    icon = Icons.Default.Refresh,
                    contentDescription = null,
                ),
            )
            override val bottomBarActions = listOf(
                BottomBarItem(
                    icon = Icons.Default.Save,
                    onClick = { _buttons.tryEmit(ScreenActions.Save) },
                ),
                BottomBarItem(
                    icon = Icons.Default.Share,
                    onClick = { _buttons.tryEmit(ScreenActions.Share) },
                ),
            )

            private val _buttons = MutableSharedFlow<ScreenActions>(extraBufferCapacity = 1)
            val buttons: Flow<ScreenActions> = _buttons.asSharedFlow()
        }

        @Keep
        @Serializable
        data object ApkList : Screen, ScreenConfig {
            override val appBarNavIcon = NavigationIcon(
                icon = IconResource.DrawableIcon(R.drawable.app_bar_icon),
                tint = { MaterialTheme.colorScheme.primary })
            override val appBarTitleRes = R.string.app_name
            override val isSearchable = true
            override val hasNavigationBar = true
            override val appBarActions = listOf(
                ActionMenuItem.IconMenuItem.ShownIfRoom(
                    titleRes = R.string.menu_sort_apk,
                    onClick = { _buttons.tryEmit(ScreenActions.Sort) },
                    icon = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = null,
                ),
                ActionMenuItem.IconMenuItem.ShownIfRoom(
                    titleRes = R.string.menu_show_open_documents,
                    onClick = { _buttons.tryEmit(ScreenActions.OpenExplorer) },
                    icon = Icons.Default.Folder,
                    contentDescription = null,
                ),
                ActionMenuItem.IconMenuItem.ShownIfRoom(
                    titleRes = R.string.menu_refresh_apk_list,
                    onClick = { _buttons.tryEmit(ScreenActions.Refresh) },
                    icon = Icons.Default.Refresh,
                    contentDescription = null,
                ),
            )
            override val bottomBarActions = emptyList<BottomBarItem>()

            private val _buttons = MutableSharedFlow<ScreenActions>(extraBufferCapacity = 1)
            val buttons: Flow<ScreenActions> = _buttons.asSharedFlow()
        }

        @Keep
        @Serializable
        data object SettingsHome : Screen, ScreenConfig {
            override val appBarNavIcon = null
            override val appBarTitleRes = R.string.title_activity_settings
            override val isSearchable = false
            override val hasNavigationBar = true
            override val appBarActions = emptyList<ActionMenuItem>()
            override val bottomBarActions = emptyList<BottomBarItem>()

            private val _buttons = MutableSharedFlow<ScreenActions>(extraBufferCapacity = 1)
            val buttons: Flow<ScreenActions> = _buttons.asSharedFlow()
        }

        @Keep
        @Serializable
        data object SettingsSaveFile : Screen, ScreenConfig {
            override val appBarNavIcon =
                NavigationIcon(icon = IconResource.VectorIcon(imageVector = Icons.AutoMirrored.Filled.ArrowBack)) {
                    _buttons.tryEmit(ScreenActions.NavigationIcon)
                }
            override val appBarTitleRes = R.string.title_screen_save_file_settings
            override val isSearchable = false
            override val hasNavigationBar = true
            override val appBarActions = emptyList<ActionMenuItem>()
            override val bottomBarActions = emptyList<BottomBarItem>()

            private val _buttons = MutableSharedFlow<ScreenActions>(extraBufferCapacity = 1)
            val buttons: Flow<ScreenActions> = _buttons.asSharedFlow()
        }

        @Keep
        @Serializable
        data object SettingsAutoBackup : Screen, ScreenConfig {
            override val appBarNavIcon =
                NavigationIcon(icon = IconResource.VectorIcon(imageVector = Icons.AutoMirrored.Filled.ArrowBack)) {
                    _buttons.tryEmit(ScreenActions.NavigationIcon)
                }
            override val appBarTitleRes = R.string.title_screen_auto_backup_settings
            override val isSearchable = false
            override val hasNavigationBar = true
            override val appBarActions = emptyList<ActionMenuItem>()
            override val bottomBarActions = emptyList<BottomBarItem>()

            private val _buttons = MutableSharedFlow<ScreenActions>(extraBufferCapacity = 1)
            val buttons: Flow<ScreenActions> = _buttons.asSharedFlow()
        }

        @Keep
        @Serializable
        data object SettingsSwipeAction : Screen, ScreenConfig {
            override val appBarNavIcon =
                NavigationIcon(icon = IconResource.VectorIcon(imageVector = Icons.AutoMirrored.Filled.ArrowBack)) {
                    _buttons.tryEmit(ScreenActions.NavigationIcon)
                }
            override val appBarTitleRes = R.string.title_screen_swipe_action_settings
            override val isSearchable = false
            override val hasNavigationBar = true
            override val appBarActions = emptyList<ActionMenuItem>()
            override val bottomBarActions = emptyList<BottomBarItem>()

            private val _buttons = MutableSharedFlow<ScreenActions>(extraBufferCapacity = 1)
            val buttons: Flow<ScreenActions> = _buttons.asSharedFlow()
        }

        @Keep
        @Serializable
        data object SettingsDataCollection : Screen, ScreenConfig {
            override val appBarNavIcon =
                NavigationIcon(icon = IconResource.VectorIcon(imageVector = Icons.AutoMirrored.Filled.ArrowBack)) {
                    _buttons.tryEmit(ScreenActions.NavigationIcon)
                }
            override val appBarTitleRes = R.string.title_screen_data_collection_settings
            override val isSearchable = false
            override val hasNavigationBar = true
            override val appBarActions = emptyList<ActionMenuItem>()
            override val bottomBarActions = emptyList<BottomBarItem>()

            private val _buttons = MutableSharedFlow<ScreenActions>(extraBufferCapacity = 1)
            val buttons: Flow<ScreenActions> = _buttons.asSharedFlow()
        }

        @Keep
        @Serializable
        data object SettingsAbout : Screen, ScreenConfig {
            override val appBarNavIcon =
                NavigationIcon(icon = IconResource.VectorIcon(imageVector = Icons.AutoMirrored.Filled.ArrowBack)) {
                    _buttons.tryEmit(ScreenActions.NavigationIcon)
                }
            override val appBarTitleRes = R.string.title_screen_about_settings
            override val isSearchable = false
            override val hasNavigationBar = true
            override val appBarActions = emptyList<ActionMenuItem>()
            override val bottomBarActions = emptyList<BottomBarItem>()

            private val _buttons = MutableSharedFlow<ScreenActions>(extraBufferCapacity = 1)
            val buttons: Flow<ScreenActions> = _buttons.asSharedFlow()
        }
    }
}