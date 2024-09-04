package domilopment.apkextractor.ui.settings.preferences

import android.annotation.SuppressLint
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SeekBarPreference(
    icon: ImageVector? = null,
    enabled: Boolean = true,
    @StringRes iconDesc: Int? = null,
    @StringRes name: Int,
    @StringRes summary: Int? = null,
    min: Float = 0f,
    max: Float = 100f,
    steps: Int = 1,
    showValue: Boolean = false,
    state: Float,
    onValueChanged: (Float) -> Unit
) {
    SeekBarPreference(
        icon = icon,
        enabled = enabled,
        iconDesc = iconDesc?.let { stringResource(id = it) },
        name = stringResource(id = name),
        summary = summary?.let { stringResource(id = it) },
        min = min,
        max = max,
        steps = steps,
        showValue = showValue,
        state = state,
        onValueChanged = onValueChanged
    )
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun SeekBarPreference(
    icon: ImageVector? = null,
    enabled: Boolean = true,
    iconDesc: String? = null,
    name: String,
    summary: String? = null,
    min: Float = 0f,
    max: Float = 100f,
    @IntRange(from = 0L) steps: Int = 1,
    showValue: Boolean = false,
    state: Float,
    onValueChanged: (Float) -> Unit
) {
    var value by remember {
        mutableFloatStateOf(state)
    }

    LaunchedEffect(state) {
        value = state
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
                    value = value,
                    onValueChange = {
                        value = it
                    },
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    valueRange = min..max,
                    steps = steps,
                    onValueChangeFinished = { onValueChanged(value) },
                )
                if (showValue) Text(text = value.toInt().toString())
            }
        }
    }, onClick = {})
}