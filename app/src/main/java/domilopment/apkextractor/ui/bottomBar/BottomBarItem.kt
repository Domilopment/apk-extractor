package domilopment.apkextractor.ui.bottomBar

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomBarItem(
    val icon: ImageVector,
    val onClick: () -> Unit,
    @param:StringRes
    val labelRes: Int
)