package domilopment.apkextractor.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import domilopment.apkextractor.R

val TOP_LEVEL_ROUTES: List<TopLevelRoute<out Any>> = listOf(
    TopLevelRoute(R.string.menu_show_app_list, Route.Screen.AppList, Icons.Default.Apps),
    TopLevelRoute(R.string.menu_show_save_dir, Route.Screen.ApkList, Icons.Default.Folder),
    TopLevelRoute(R.string.title_activity_settings, Route.Graph.Settings, Icons.Default.Settings)
)