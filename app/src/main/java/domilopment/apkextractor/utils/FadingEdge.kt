package domilopment.apkextractor.utils

import androidx.annotation.FloatRange
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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

fun Modifier.fadingEdge(
    start: Offset,
    end: Offset,
    visible: Boolean = true,
    @FloatRange(from = 0.0, to = 1.0) edgeSize: Float = 0.1f
) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .composed {
        val fade by animateFloatAsState(
            targetValue = if (visible) 0f else edgeSize,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "FadingEdge"
        )
        drawWithContent {
            drawContent()
            drawRect(
                brush = Brush.linearGradient(
                    0f to Color.Transparent, fade to Color.Black, start = start, end = end
                ), blendMode = BlendMode.DstIn
            )
        }
    }

fun Modifier.fadingTop(
    visible: Boolean, @FloatRange(from = 0.0, to = 1.0) edgeSize: Float = 0.1f
) = fadingEdge(
    start = Offset.Zero,
    end = Offset(0f, Float.POSITIVE_INFINITY),
    visible = visible,
    edgeSize = edgeSize
)

fun Modifier.fadingBottom(
    visible: Boolean, @FloatRange(from = 0.0, to = 1.0) edgeSize: Float = 0.1f
) = fadingEdge(
    start = Offset(0f, Float.POSITIVE_INFINITY),
    end = Offset.Zero,
    visible = visible,
    edgeSize = edgeSize
)

fun Modifier.fadingStart(
    visible: Boolean, @FloatRange(from = 0.0, to = 1.0) edgeSize: Float = 0.1f
) = fadingEdge(
    start = Offset.Zero,
    end =  Offset(Float.POSITIVE_INFINITY, 0f),
    visible = visible,
    edgeSize = edgeSize
)

fun Modifier.fadingEnd(
    visible: Boolean, @FloatRange(from = 0.0, to = 1.0) edgeSize: Float = 0.1f
) = fadingEdge(
    start = Offset(Float.POSITIVE_INFINITY, 0f),
    end =  Offset.Zero,
    visible = visible,
    edgeSize = edgeSize
)
