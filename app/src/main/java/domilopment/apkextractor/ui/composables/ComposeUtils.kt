package domilopment.apkextractor.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
@ReadOnlyComposable
fun attrColorResource(attrId: Int): Color {
    val context = LocalContext.current
    val attrs = context.theme.obtainStyledAttributes(intArrayOf(attrId))
    val colorValue = attrs.getColor(0, 0)
    return Color(colorValue)
}
