package domilopment.apkextractor.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import domilopment.apkextractor.R

data class TopLevelNavItem(
    @param:StringRes val nameResId: Int, val selectedIcon: ImageVector, val unselectedIcon: ImageVector
) {
    companion object {
        val TOP_LEVEL_ROUTES = mapOf<Route, TopLevelNavItem>(
            Route.AppList to TopLevelNavItem(
                R.string.menu_show_app_list, Icons.Filled.Apps, Icons.Outlined.Apps
            ), Route.ApkList to TopLevelNavItem(
                R.string.menu_show_save_dir, Icons.Filled.Folder, Icons.Outlined.Folder
            ), Route.SettingsHome to TopLevelNavItem(
                R.string.title_activity_settings, Icons.Filled.Settings, Icons.Outlined.Settings
            )
        )
    }
}
