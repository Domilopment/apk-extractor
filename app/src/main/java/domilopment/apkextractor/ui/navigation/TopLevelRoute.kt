package domilopment.apkextractor.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.Graph
import domilopment.apkextractor.ui.Route

data class TopLevelRoute<T : Any>(
    @StringRes val nameResId: Int, val route: T, val icon: ImageVector
)

val TOP_LEVEL_ROUTES: List<TopLevelRoute<out Any>> = listOf(
    TopLevelRoute(R.string.menu_show_app_list, Route.AppList, Icons.Default.Apps),
    TopLevelRoute(R.string.menu_show_save_dir, Route.ApkList, Icons.Default.Folder),
    TopLevelRoute(R.string.title_activity_settings, Graph.Settings, Icons.Default.Settings)
)
