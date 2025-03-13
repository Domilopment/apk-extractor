package domilopment.apkextractor.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import domilopment.apkextractor.data.IconResource

@Composable
fun Icon(
    iconResource: IconResource,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    Icon(
        painter = when (iconResource) {
            is IconResource.VectorIcon -> rememberVectorPainter(image = iconResource.imageVector)
            is IconResource.DrawableIcon -> painterResource(id = iconResource.drawableResId)
        },
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}