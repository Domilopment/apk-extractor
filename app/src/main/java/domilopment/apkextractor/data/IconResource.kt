package domilopment.apkextractor.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface IconResource {
    data class VectorIcon(val imageVector: ImageVector) : IconResource
    data class DrawableIcon(@param:DrawableRes val drawableResId: Int) : IconResource
}