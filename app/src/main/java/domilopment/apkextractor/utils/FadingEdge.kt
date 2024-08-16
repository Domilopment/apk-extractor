package domilopment.apkextractor.utils

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sqrt

enum class EdgeOffsetValues(val value: Float) {
    ZERO(0f), INFINITE(Float.POSITIVE_INFINITY)
}

class EdgeOffset(val x: EdgeOffsetValues, val y: EdgeOffsetValues) {
    fun toOffset(): Offset = Offset(x.value, y.value)

    companion object {
        val Zero = EdgeOffset(EdgeOffsetValues.ZERO, EdgeOffsetValues.ZERO)
        val Infinite = EdgeOffset(EdgeOffsetValues.INFINITE, EdgeOffsetValues.INFINITE)
    }
}

/**
 * A fading edge drawn by a linear gradient from start to end
 * @param start the begin of the fading edge from it is drawn drawn
 * @param end the end of the fading edge to it is drawn
 * @param visible set if the content should be fully visible or not
 * @param size the size of the fading edge
 */
fun Modifier.fadingEdge(start: EdgeOffset, end: EdgeOffset, visible: Boolean, size: Dp) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .composed {
        require(size > 0.dp) { "Invalid fade width: Width must be greater than 0" }
        
        val fade by animateDpAsState(
            targetValue = if (visible) 0.dp else size,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "FadingEdge"
        )

        val width = remember(start, end) {
            val abs = abs(end.x.value - start.x.value)
            when {
                abs.isNaN() -> 0f
                else -> abs
            }
        }

        val height = remember(start, end) {
            val abs = abs(end.y.value - start.y.value)
            when {
                abs.isNaN() -> 0f
                else -> abs
            }
        }

        drawWithContent {
            drawContent()

            val fraction = when {
                width == Float.POSITIVE_INFINITY && height == 0f -> fade.toPx() / this.size.width
                width == 0f && height == Float.POSITIVE_INFINITY -> fade.toPx() / this.size.height
                width == Float.POSITIVE_INFINITY && height == Float.POSITIVE_INFINITY -> fade.toPx() / sqrt(
                    this.size.width * this.size.width + this.size.height * this.size.height
                )

                width == 0f && height == 0f -> 0f
                else -> error("FadingEdge fraction is not in range: width=$width, height=$height")
            }

            drawRect(
                brush = Brush.linearGradient(
                    0f to Color.Transparent,
                    fraction to Color.Black,
                    start = start.toOffset(),
                    end = end.toOffset()
                ), size = this.size, blendMode = BlendMode.DstIn
            )
        }
    }

/**
 * Draws a fading edge at the top of the content
 * @param visible set if the content should be fully visible or not
 * @param size the size of the fading edge
 */
fun Modifier.fadingTop(visible: Boolean, size: Dp = 32.dp) = fadingEdge(
    start = EdgeOffset.Zero,
    end = EdgeOffset(EdgeOffsetValues.ZERO, EdgeOffsetValues.INFINITE),
    visible = visible,
    size = size
)

/**
 * Draws a fading edge at the bottom of the content
 * @param visible set if the content should be fully visible or not
 * @param size the size of the fading edge
 */
fun Modifier.fadingBottom(visible: Boolean, size: Dp = 32.dp) = fadingEdge(
    start = EdgeOffset(EdgeOffsetValues.ZERO, EdgeOffsetValues.INFINITE),
    end = EdgeOffset.Zero,
    visible = visible,
    size = size
)

/**
 * Draws a fading edge at the start of the content
 * @param visible set if the content should be fully visible or not
 * @param size the size of the fading edge
 */
fun Modifier.fadingStart(visible: Boolean, size: Dp = 32.dp) = fadingEdge(
    start = EdgeOffset.Zero,
    end = EdgeOffset(EdgeOffsetValues.INFINITE, EdgeOffsetValues.ZERO),
    visible = visible,
    size = size
)

/**
 * Draws a fading edge at the end of the content
 * @param visible set if the content should be fully visible or not
 * @param size the size of the fading edge
 */
fun Modifier.fadingEnd(visible: Boolean, size: Dp = 32.dp) = fadingEdge(
    start = EdgeOffset(EdgeOffsetValues.INFINITE, EdgeOffsetValues.ZERO),
    end = EdgeOffset.Zero,
    visible = visible,
    size = size
)