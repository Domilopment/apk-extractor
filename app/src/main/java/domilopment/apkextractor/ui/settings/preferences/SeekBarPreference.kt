package domilopment.apkextractor.ui.settings.preferences

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.asFloatState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
    state: State<Float>,
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
    steps: Int = 1,
    showValue: Boolean = false,
    state: State<Float>,
    onValueChanged: (Float) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var value by remember {
        mutableFloatStateOf(state.value)
    }

    LaunchedEffect(state.value) {
        value = state.value
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            when (it) {
                is DragInteraction.Stop -> onValueChanged(value)
            }
        }
    }

    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column {
            ListItem(headlineContent = {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    overflow = TextOverflow.Ellipsis
                )
            }, supportingContent = {
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
                            valueRange = min..max,
                            steps = steps,
                            interactionSource = interactionSource
                        )
                        if (showValue) Text(text = value.toInt().toString())
                    }
                }
            }, leadingContent = {
                if (icon != null) Icon(
                    imageVector = icon,
                    contentDescription = iconDesc,
                    modifier = Modifier.size(24.dp)
                ) else Spacer(modifier = Modifier.size(24.dp))
            }, colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
                headlineColor = if (enabled) MaterialTheme.colorScheme.surfaceTint else MaterialTheme.colorScheme.surfaceTint.copy(
                    alpha = 0.38f
                ),
                leadingIconColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                ),
                supportingColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                ),
                trailingIconColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                )
            )
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}