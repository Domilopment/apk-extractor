package domilopment.apkextractor.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import domilopment.apkextractor.ui.Screen

data class TopLevelRoute<T : Any>(
    @StringRes val nameResId: Int, val route: T, val icon: ImageVector
)

fun Screen.toTopLevelRoute() = TopLevelRoute(this.routeNameRes, this, this.icon)
