package domilopment.apkextractor.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class TopLevelRoute<T : Any>(
    @param:StringRes val nameResId: Int, val route: T, val icon: ImageVector
)
