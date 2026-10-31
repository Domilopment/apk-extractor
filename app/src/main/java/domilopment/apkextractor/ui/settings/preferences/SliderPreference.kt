package domilopment.apkextractor.ui.settings.preferences

import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SliderPreference(
    @StringRes name: Int,
    state: Float,
    onValueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    @StringRes iconDesc: Int? = null,
    @StringRes summary: Int? = null,
    min: Float = 0f,
    max: Float = 100f,
    @IntRange(from = 0L) steps: Int = 0,
    enabled: Boolean = true,
    sliderValue: @Composable ((Float) -> Unit)? = { Text(text = "%.2f".format(it)) }
) {
    SliderPreference(
        name = stringResource(id = name),
        state = state,
        onValueChanged = onValueChanged,
        modifier = modifier,
        icon = icon,
        iconDesc = iconDesc?.let { stringResource(id = it) },
        summary = summary?.let { stringResource(id = it) },
        min = min,
        max = max,
        steps = steps,
        enabled = enabled,
        sliderValue = sliderValue
    )
}

@Composable
fun SliderPreference(
    name: String,
    state: Float,
    onValueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconDesc: String? = null,
    summary: String? = null,
    min: Float = 0f,
    max: Float = 100f,
    @IntRange(from = 0L) steps: Int = 0,
    enabled: Boolean = true,
    sliderValue: @Composable ((Float) -> Unit)? = { Text(text = "%.2f".format(it)) }
) {
    val sliderState = rememberSliderState(
        value = state, steps = steps, trackRange = min..max
    )

    LaunchedEffect(state) {
        sliderState.value = state
    }

    BasePreference(
        icon = icon,
        enabled = enabled,
        iconDesc = iconDesc,
        name = {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier,
        summary = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (summary != null) Text(text = summary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        state = sliderState,
                        onValueChange = {
                            sliderState.value = it
                        },
                        modifier = Modifier.weight(1f),
                        enabled = enabled,
                        onValueChangeFinished = { onValueChanged(sliderState.value) },
                    )
                    sliderValue?.invoke(sliderState.value)
                }
            }
        },
        onClick = {}
    )
}
