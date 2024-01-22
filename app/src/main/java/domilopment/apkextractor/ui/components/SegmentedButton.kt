/**
 * Based on SegmentedButton from Google
 *
 *      https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/SegmentedButton.kt;l=129?q=SegmentedButton
 *
 * Which is published under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 */

package domilopment.apkextractor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SegmentedButtonColors
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.MultiContentMeasurePolicy
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * <a href="https://m3.material.io/components/segmented-buttons/overview" class="external" target="_blank">Material Segmented Button</a>.
 * Segmented buttons help people select options, switch views, or sort elements.
 *
 * A default Toggleable Segmented Button. Also known as Outlined Segmented Button.
 * See [Modifier.toggleable].
 *
 * Toggleable segmented buttons should be used for cases where the selection is not mutually
 * exclusive.
 *
 * This should typically be used inside of a [MultiChoiceSegmentedButtonColumn]
 *
 * @param checked whether this button is checked or not
 * @param onCheckedChange callback to be invoked when the button is clicked.
 * therefore the change of checked state in requested.
 * @param shape the shape for this button
 * @param modifier the [Modifier] to be applied to this button
 * @param enabled controls the enabled state of this button. When `false`, this component will not
 * respond to user input, and it will appear visually disabled and disabled to accessibility
 * services.
 * @param colors [SegmentedButtonColors] that will be used to resolve the colors used for this
 * @param border the border for this button, see [SegmentedButtonColors]
 * Button in different states
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for this button. You can create and pass in your own `remember`ed instance to observe
 * [Interaction]s and customize the appearance / behavior of this button in different states.
 * @param icon the icon slot for this button, you can pass null in unchecked, in which case
 * the content will displace to show the checked icon, or pass different icon lambdas for
 * unchecked and checked in which case the icons will crossfade.
 * @param label content to be rendered inside this button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiChoiceSegmentedButtonColumnScope.SegmentedButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SegmentedButtonColors = SegmentedButtonDefaults.colors(),
    border: BorderStroke = SegmentedButtonDefaults.borderStroke(
        color = borderColor(
            colors, enabled, checked
        )
    ),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    icon: @Composable () -> Unit = { SegmentedButtonDefaults.Icon(checked) },
    label: @Composable () -> Unit,
) {
    val containerColor = containerColor(colors, enabled, checked)
    val contentColor = contentColor(colors, enabled, checked)
    val interactionCount = interactionSource.interactionCountAsState()

    Surface(
        modifier = modifier
            .weight(1f)
            .interactionZIndex(checked, interactionCount)
            .defaultMinSize(
                minWidth = ButtonDefaults.MinWidth, minHeight = ButtonDefaults.MinHeight
            ),
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        interactionSource = interactionSource
    ) {
        SegmentedButtonContent(icon, label)
    }
}

/**
 * <a href="https://m3.material.io/components/segmented-buttons/overview" class="external" target="_blank">Material Segmented Button</a>.
 * Segmented buttons help people select options, switch views, or sort elements.
 *
 * A default Toggleable Segmented Button. Also known as Outlined Segmented Button.
 * See [Modifier.selectable].
 *
 * Selectable segmented buttons should be used for cases where the selection is mutually
 * exclusive, when only one button can be selected at a time.
 *
 * This should typically be used inside of a [SingleChoiceSegmentedButtonColumn]
 *
 * @param selected whether this button is selected or not
 * @param onClick callback to be invoked when the button is clicked.
 * therefore the change of checked state in requested.
 * @param shape the shape for this button
 * @param modifier the [Modifier] to be applied to this button
 * @param enabled controls the enabled state of this button. When `false`, this component will not
 * respond to user input, and it will appear visually disabled and disabled to accessibility
 * services.
 * @param colors [SegmentedButtonColors] that will be used to resolve the colors used for this
 * @param border the border for this button, see [SegmentedButtonColors]
 * Button in different states
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for this button. You can create and pass in your own `remember`ed instance to observe
 * [Interaction]s and customize the appearance / behavior of this button in different states.
 * @param icon the icon slot for this button, you can pass null in unchecked, in which case
 * the content will displace to show the checked icon, or pass different icon lambdas for
 * unchecked and checked in which case the icons will crossfade.
 * @param label content to be rendered inside this button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleChoiceSegmentedButtonColumnScope.SegmentedButton(
    selected: Boolean,
    onClick: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SegmentedButtonColors = androidx.compose.material3.SegmentedButtonDefaults.colors(),
    border: BorderStroke = androidx.compose.material3.SegmentedButtonDefaults.borderStroke(
        color = borderColor(
            colors, enabled, selected
        )
    ),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    icon: @Composable () -> Unit = { SegmentedButtonDefaults.Icon(selected) },
    label: @Composable () -> Unit,
) {
    val containerColor = containerColor(colors, enabled, selected)
    val contentColor = contentColor(colors, enabled, selected)
    val interactionCount = interactionSource.interactionCountAsState()

    Surface(
        modifier = modifier
            .weight(1f)
            .interactionZIndex(selected, interactionCount)
            .defaultMinSize(
                minWidth = ButtonDefaults.MinWidth, minHeight = ButtonDefaults.MinHeight
            )
            .semantics { role = Role.RadioButton },
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        interactionSource = interactionSource
    ) {
        SegmentedButtonContent(icon, label)
    }
}

/**
 * <a href="https://m3.material.io/components/segmented-buttons/overview" class="external" target="_blank">Material Segmented Button</a>.
 *
 * A Layout to correctly position and size [SegmentedButton]s in a Row.
 * It handles overlapping items so that strokes of the item are correctly on top of each other.
 * [SingleChoiceSegmentedButtonColumn] is used when the selection only allows one value, for correct
 * semantics.
 *
 * @param modifier the [Modifier] to be applied to this row
 * @param space the dimension of the overlap between buttons. Should be equal to the stroke width
 *  used on the items.
 * @param content the content of this Segmented Button Row, typically a sequence of
 * [SegmentedButton]s
 */
@Composable
fun SingleChoiceSegmentedButtonColumn(
    modifier: Modifier = Modifier,
    space: Dp = SegmentedButtonDefaults.BorderWidth,
    content: @Composable SingleChoiceSegmentedButtonColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .selectableGroup()
            .defaultMinSize(minHeight = 40.0.dp)
            .width(IntrinsicSize.Min),
        verticalArrangement = Arrangement.spacedBy(-space),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val scope = remember { SingleChoiceSegmentedButtonScopeWrapper(this) }
        scope.content()
    }
}

/**
 * <a href="https://m3.material.io/components/segmented-buttons/overview" class="external" target="_blank">Material Segmented Button</a>.
 *
 * A Layout to correctly position, size, and add semantics to [SegmentedButton]s in a Row.
 * It handles overlapping items so that strokes of the item are correctly on top of each other.
 *
 * [MultiChoiceSegmentedButtonColumn] is used when the selection allows multiple value, for correct
 * semantics.
 *
 * @param modifier the [Modifier] to be applied to this row
 * @param space the dimension of the overlap between buttons. Should be equal to the stroke width
 *  used on the items.
 * @param content the content of this Segmented Button Row, typically a sequence of
 * [SegmentedButton]s
 *
 */
@Composable
fun MultiChoiceSegmentedButtonColumn(
    modifier: Modifier = Modifier,
    space: Dp = SegmentedButtonDefaults.BorderWidth,
    content: @Composable MultiChoiceSegmentedButtonColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 40.0.dp)
            .width(IntrinsicSize.Min),
        verticalArrangement = Arrangement.spacedBy(-space),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val scope = remember { MultiChoiceSegmentedButtonScopeWrapper(this) }
        scope.content()
    }
}

@Composable
private fun SegmentedButtonContent(
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(ButtonDefaults.TextButtonContentPadding)
    ) {
        val typography = MaterialTheme.typography.labelLarge
        ProvideTextStyle(typography) {
            val scope = rememberCoroutineScope()
            val measurePolicy = remember { SegmentedButtonContentMeasurePolicy(scope) }

            Layout(
                modifier = Modifier.height(IntrinsicSize.Min),
                contents = listOf(icon, content),
                measurePolicy = measurePolicy
            )
        }
    }
}

internal class SegmentedButtonContentMeasurePolicy(
    val scope: CoroutineScope
) : MultiContentMeasurePolicy {
    var animatable: Animatable<Int, AnimationVector1D>? = null
    private var initialOffset: Int? = null

    override fun MeasureScope.measure(
        measurables: List<List<Measurable>>, constraints: Constraints
    ): MeasureResult {
        val (iconMeasurables, contentMeasurables) = measurables
        val iconPlaceables = iconMeasurables.fastMap { it.measure(constraints) }
        val iconWidth = iconPlaceables.fastMaxBy { it.width }?.width ?: 0
        val contentPlaceables = contentMeasurables.fastMap { it.measure(constraints) }
        val contentWidth = contentPlaceables.fastMaxBy { it.width }?.width
        val height = contentPlaceables.fastMaxBy { it.height }?.height ?: 0
        val width = maxOf(
            SegmentedButtonDefaults.IconSize.roundToPx(), iconWidth
        ) + IconSpacing.roundToPx() + (contentWidth ?: 0)
        val offsetX = if (iconWidth == 0) {
            -(SegmentedButtonDefaults.IconSize.roundToPx() + IconSpacing.roundToPx()) / 2
        } else {
            0
        }

        if (initialOffset == null) {
            initialOffset = offsetX
        } else {
            val anim = animatable ?: Animatable(initialOffset!!, Int.VectorConverter).also {
                animatable = it
            }
            if (anim.targetValue != offsetX) {
                scope.launch {
                    anim.animateTo(offsetX, tween(350.0.toInt()))
                }
            }
        }

        return layout(width, height) {
            iconPlaceables.fastForEach {
                it.place(0, (height - it.height) / 2)
            }

            val contentOffsetX =
                SegmentedButtonDefaults.IconSize.roundToPx() + IconSpacing.roundToPx() + (animatable?.value
                    ?: offsetX)

            contentPlaceables.fastForEach {
                it.place(
                    contentOffsetX, (height - it.height) / 2
                )
            }
        }
    }
}

@Composable
private fun InteractionSource.interactionCountAsState(): State<Int> {
    val interactionCount = remember { mutableIntStateOf(0) }
    LaunchedEffect(this) {
        this@interactionCountAsState.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press, is FocusInteraction.Focus -> {
                    interactionCount.intValue++
                }

                is PressInteraction.Release, is FocusInteraction.Unfocus, is PressInteraction.Cancel -> {
                    interactionCount.intValue--
                }
            }
        }
    }

    return interactionCount
}

/** Scope for the children of a [SingleChoiceSegmentedButtonColumn] */
interface SingleChoiceSegmentedButtonColumnScope : ColumnScope

/** Scope for the children of a [MultiChoiceSegmentedButtonColumn] */
interface MultiChoiceSegmentedButtonColumnScope : ColumnScope

/* Contains defaults to be used with [SegmentedButtonColumn] and [SegmentedButton] */
@Stable
object SegmentedButtonDefaults {

    /**
     * Creates a [SegmentedButtonColors] that represents the different colors
     * used in a [SegmentedButton] in different states.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun colors() = MaterialTheme.colorScheme.defaultSegmentedButtonColors

    @OptIn(ExperimentalMaterial3Api::class)
    internal val ColorScheme.defaultSegmentedButtonColors: SegmentedButtonColors
        @Composable get() = androidx.compose.material3.SegmentedButtonDefaults.colors()

    /**
     * The shape of the segmented button container, for correct behavior this should or the desired
     * [CornerBasedShape] should be used with [itemShape] and passed to each segmented button.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    val baseShape: CornerBasedShape
        @Composable @ReadOnlyComposable get() = androidx.compose.material3.SegmentedButtonDefaults.baseShape

    /** Default border width used in segmented button */
    val BorderWidth: Dp = 1.0.dp

    /**
     * A shape constructor that the button in [index] should have when there are [count] buttons in
     * the container.
     *
     * @param index the index for this button in the row
     * @param count the count of buttons in this row
     * @param baseShape the [CornerBasedShape] the base shape that should be used in buttons that
     * are not in the start or the end.
     */
    @Composable
    @ReadOnlyComposable
    fun itemShape(index: Int, count: Int, baseShape: CornerBasedShape = this.baseShape): Shape {
        if (count == 1) {
            return baseShape
        }

        return when (index) {
            0 -> RoundedCornerShape(
                baseShape.topStart, baseShape.topEnd, CornerSize(0.dp), CornerSize(0.dp)
            )

            count - 1 -> RoundedCornerShape(
                CornerSize(0.dp), CornerSize(0.dp), baseShape.bottomEnd, baseShape.bottomStart
            )

            else -> RectangleShape
        }
    }

    /**
     * Icon size to use for icons used in [SegmentedButton]
     */
    val IconSize = 18.0.dp

    /** And icon to indicate the segmented button is checked or selected */
    @Composable
    fun ActiveIcon() {
        androidx.compose.material3.Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            modifier = Modifier.size(IconSize)
        )
    }

    /**
     * The default implementation of icons for Segmented Buttons.
     *
     * @param active whether the button is activated or not.
     * @param activeContent usually a checkmark icon of [IconSize] dimensions.
     * @param inactiveContent typically an icon of [IconSize]. It shows only when the button is not
     * checked.
     */
    @Composable
    fun Icon(
        active: Boolean,
        activeContent: @Composable () -> Unit = { ActiveIcon() },
        inactiveContent: (@Composable () -> Unit)? = null
    ) {
        if (inactiveContent == null) {
            AnimatedVisibility(
                visible = active,
                exit = ExitTransition.None,
                enter = fadeIn(tween(350.0.toInt())) + scaleIn(
                    initialScale = 0f,
                    transformOrigin = TransformOrigin(0f, 1f),
                    animationSpec = tween(350.0.toInt()),
                ),
            ) {
                activeContent()
            }
        } else {
            Crossfade(targetState = active) {
                if (it) activeContent() else inactiveContent()
            }
        }
    }

    /**
     * Default factory for Segmented Button [BorderStroke] can be customized through [width],
     * and [color]. When using a width different than default make sure to also update
     * [MultiChoiceSegmentedButtonColumn] or [SingleChoiceSegmentedButtonColumn] space param.
     */
    fun borderStroke(
        color: Color,
        width: Dp = BorderWidth,
    ): BorderStroke = BorderStroke(width = width, color = color)
}

/**
 * Represents the color used for the SegmentedButton's border,
 * depending on [enabled] and [active].
 *
 * @param colors the colors applied to the [SegmentedButton] border
 * @param enabled whether the [SegmentedButton] is enabled or not
 * @param active whether the [SegmentedButton] item is checked or not
 */
@Stable
@OptIn(ExperimentalMaterial3Api::class)
private fun borderColor(
    colors: SegmentedButtonColors, enabled: Boolean, active: Boolean
): Color {
    return when {
        enabled && active -> colors.activeBorderColor
        enabled && !active -> colors.inactiveBorderColor
        !enabled && active -> colors.disabledActiveBorderColor
        else -> colors.disabledInactiveBorderColor
    }
}


/**
 * Represents the content color passed to the items
 *
 * @param colors the colors applied to the [SegmentedButton] content
 * @param enabled whether the [SegmentedButton] is enabled or not
 * @param checked whether the [SegmentedButton] item is checked or not
 */
@Stable
@OptIn(ExperimentalMaterial3Api::class)
private fun contentColor(
    colors: SegmentedButtonColors, enabled: Boolean, checked: Boolean
): Color {
    return when {
        enabled && checked -> colors.activeContentColor
        enabled && !checked -> colors.inactiveContentColor
        !enabled && checked -> colors.disabledActiveContentColor
        else -> colors.disabledInactiveContentColor
    }
}

/**
 * Represents the container color passed to the items
 *
 * @param colors the colors applied to the [SegmentedButton]
 * @param enabled whether the [SegmentedButton] is enabled or not
 * @param active whether the [SegmentedButton] item is active or not
 */
@Stable
@OptIn(ExperimentalMaterial3Api::class)
private fun containerColor(
    colors: SegmentedButtonColors, enabled: Boolean, active: Boolean
): Color {
    return when {
        enabled && active -> colors.activeContainerColor
        enabled && !active -> colors.inactiveContainerColor
        !enabled && active -> colors.disabledActiveContainerColor
        else -> colors.disabledInactiveContainerColor
    }
}

private fun Modifier.interactionZIndex(checked: Boolean, interactionCount: State<Int>) =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            val zIndex = interactionCount.value + if (checked) CheckedZIndexFactor else 0f
            placeable.place(0, 0, zIndex)
        }
    }

private const val CheckedZIndexFactor = 5f
private val IconSpacing = 8.dp

private class SingleChoiceSegmentedButtonScopeWrapper(scope: ColumnScope) :
    SingleChoiceSegmentedButtonColumnScope, ColumnScope by scope

private class MultiChoiceSegmentedButtonScopeWrapper(scope: ColumnScope) :
    MultiChoiceSegmentedButtonColumnScope, ColumnScope by scope
