package domilopment.apkextractor.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Measured
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.unit.dp

interface SegmentedButtonColumnScope : ColumnScope {
    @Composable
    fun SegmentedButton(
        selected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        shape: Shape,
        content: @Composable () -> Unit
    ) {
        Button(
            onClick = onClick,
            modifier = modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
            shape = shape,
            colors = ButtonColors(
                if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                MaterialTheme.colorScheme.onSecondaryContainer,
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            content()
        }
    }

    object SegmentedButtonDefaults {
        @Composable
        fun itemShape(index: Int, count: Int): Shape {
            return RoundedCornerShape(
                if (index == 0) MaterialTheme.shapes.extraLarge.topStart else CornerSize(0.dp),
                if (index == 0) MaterialTheme.shapes.extraLarge.topStart else CornerSize(0.dp),
                if (index == count - 1) MaterialTheme.shapes.extraLarge.bottomEnd else CornerSize(0.dp),
                if (index == count - 1) MaterialTheme.shapes.extraLarge.bottomStart else CornerSize(
                    0.dp
                )
            )
        }
    }
}

@Composable
fun SegmentedButtonColumn(
    modifier: Modifier = Modifier, content: @Composable() (SegmentedButtonColumnScope.() -> Unit)
) {
    Column(modifier) {
        content(object : SegmentedButtonColumnScope {
            override fun Modifier.align(alignment: Alignment.Horizontal): Modifier {
                return this.align(alignment)
            }

            override fun Modifier.alignBy(alignmentLineBlock: (Measured) -> Int): Modifier {
                return this.alignBy(alignmentLineBlock)
            }

            override fun Modifier.alignBy(alignmentLine: VerticalAlignmentLine): Modifier {
                return this.alignBy(alignmentLine)
            }

            override fun Modifier.weight(weight: Float, fill: Boolean): Modifier {
                return this.weight(weight, fill)
            }

        })
    }
}