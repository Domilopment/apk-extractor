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
    icon: ImageVector? = null,
    enabled: Boolean = true,
    @StringRes iconDesc: Int? = null,
    @StringRes name: Int,
    @StringRes summary: Int? = null,
    min: Float = 0f,
    max: Float = 100f,
    @IntRange(from = 0L) steps: Int = 0,
    sliderValue: @Composable ((Float) -> Unit)? = { Text(text = "%.2f".format(it)) },
    state: Float,
    onValueChanged: (Float) -> Unit
) {
    SliderPreference(
        icon = icon,
        enabled = enabled,
        iconDesc = iconDesc?.let { stringResource(id = it) },
        name = stringResource(id = name),
        summary = summary?.let { stringResource(id = it) },
        min = min,
        max = max,
        steps = steps,
        sliderValue = sliderValue,
        state = state,
        onValueChanged = onValueChanged
    )
}

@Composable
fun SliderPreference(
    icon: ImageVector? = null,
    enabled: Boolean = true,
    iconDesc: String? = null,
    name: String,
    summary: String? = null,
    min: Float = 0f,
    max: Float = 100f,
    @IntRange(from = 0L) steps: Int = 0,
    sliderValue: @Composable ((Float) -> Unit)? = { Text(text = "%.2f".format(it)) },
    state: Float,
    onValueChanged: (Float) -> Unit
) {
    val sliderState = rememberSliderState(
        value = state, steps = steps, trackRange = min..max
    )

    LaunchedEffect(state) {
        sliderState.value = state
    }

    BasePreference(icon = icon, enabled = enabled, iconDesc = iconDesc, name = {
        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis
        )
    }, summary = {
        Column {
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
    }, onClick = {})
}