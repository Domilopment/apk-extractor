package domilopment.apkextractor.utils

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.utils.FadingEdge.BOTTOM
import domilopment.apkextractor.utils.FadingEdge.BOTTOM_LEFT_CORNER
import domilopment.apkextractor.utils.FadingEdge.BOTTOM_RIGHT_CORNER
import domilopment.apkextractor.utils.FadingEdge.END
import domilopment.apkextractor.utils.FadingEdge.START
import domilopment.apkextractor.utils.FadingEdge.TOP
import domilopment.apkextractor.utils.FadingEdge.TOP_LEFT_CORNER
import domilopment.apkextractor.utils.FadingEdge.TOP_RIGHT_CORNER
import kotlin.math.sqrt

enum class FadingEdge {
    TOP, BOTTOM, START, END, TOP_RIGHT_CORNER, TOP_LEFT_CORNER, BOTTOM_RIGHT_CORNER, BOTTOM_LEFT_CORNER
}

/**
 * A fading edge drawn by a linear gradient from start to end
 * @param edge the anchor point for the edge on canvas
 * @param visible if edge should be visible
 * @param size the size of the fading edge
 */
fun Modifier.fadingEdge(edge: FadingEdge, visible: Boolean, size: Dp) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .composed {
        require(size > 0.dp) { "Invalid fade width: Width must be greater than 0" }

        val fade by animateDpAsState(
            targetValue = if (visible) size else 0.dp,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "FadingEdge"
        )

        drawWithContent {
            drawContent()

            val (start, end) = when (edge) {
                TOP -> Offset.Zero to Offset(0f, this.size.height)
                BOTTOM -> Offset(0f, this.size.height) to Offset.Zero
                START -> Offset.Zero to Offset(this.size.width, 0f)
                END -> Offset(this.size.width, 0f) to Offset.Zero
                TOP_RIGHT_CORNER -> Offset(this.size.width, 0f) to Offset(0f, this.size.height)
                TOP_LEFT_CORNER -> Offset.Zero to Offset(this.size.width, this.size.height)
                BOTTOM_RIGHT_CORNER -> Offset(this.size.width, this.size.height) to Offset.Zero
                BOTTOM_LEFT_CORNER -> Offset(0f, this.size.height) to Offset(
                    this.size.width, 0f
                )
            }

            val fraction = when (edge) {
                START, END -> fade.toPx() / this.size.width
                TOP, BOTTOM -> fade.toPx() / this.size.height
                TOP_RIGHT_CORNER, TOP_LEFT_CORNER, BOTTOM_RIGHT_CORNER, BOTTOM_LEFT_CORNER -> fade.toPx() / sqrt(
                    this.size.width * this.size.width + this.size.height * this.size.height
                )
            }

            drawRect(
                brush = Brush.linearGradient(
                    0f to Color.Transparent, fraction to Color.Black, start = start, end = end
                ), size = this.size, blendMode = BlendMode.DstIn
            )
        }
    }

/**
 * Draws a fading edge at the top of the content
 * @param visible if edge should be visible
 * @param size the size of the fading edge
 */
fun Modifier.fadingTop(visible: Boolean, size: Dp = 32.dp) =
    fadingEdge(edge = TOP, visible = visible, size = size)

/**
 * Draws a fading edge at the bottom of the content
 * @param visible if edge should be visible
 * @param size the size of the fading edge
 */
fun Modifier.fadingBottom(visible: Boolean, size: Dp = 32.dp) =
    fadingEdge(edge = BOTTOM, visible = visible, size = size)

/**
 * Draws a fading edge at the start of the content
 * @param visible if edge should be visible
 * @param size the size of the fading edge
 */
fun Modifier.fadingStart(visible: Boolean, size: Dp = 32.dp) =
    fadingEdge(edge = START, visible = visible, size = size)

/**
 * Draws a fading edge at the end of the content
 * @param visible if edge should be visible
 * @param size the size of the fading edge
 */
fun Modifier.fadingEnd(visible: Boolean, size: Dp = 32.dp) =
    fadingEdge(edge = END, visible = visible, size = size)