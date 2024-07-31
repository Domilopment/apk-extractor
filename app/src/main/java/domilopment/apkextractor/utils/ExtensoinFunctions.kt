package domilopment.apkextractor.utils

import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.conditional(
    condition: Boolean, ifTrue: Modifier.() -> Modifier, ifFalse: (Modifier.() -> Modifier)? = null
): Modifier {
    return if (condition) {
        then(ifTrue(Modifier))
    } else if (ifFalse != null) {
        then(ifFalse(Modifier))
    } else {
        this
    }
}

fun Modifier.fadingEdge(
    start: Offset, end: Offset, visible: Boolean = true
) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .composed {
        val fade by animateColorAsState(
            targetValue = if (visible) Color.Black else Color.Transparent, label = "FadingEdge"
        )
        drawWithContent {
            drawContent()
            drawRect(
                brush = Brush.linearGradient(
                    0f to fade, 0.1f to Color.Black, start = start, end = end
                ), blendMode = BlendMode.DstIn
            )
        }
    }

fun Modifier.fadingTop(
    visible: Boolean
) = fadingEdge(start = Offset.Zero, end = Offset(0f, Float.POSITIVE_INFINITY), visible = visible)

fun Modifier.fadingBottom(visible: Boolean) = fadingEdge(
    start = Offset(0f, Float.POSITIVE_INFINITY), end = Offset.Zero, visible = visible
)
