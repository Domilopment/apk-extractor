package domilopment.apkextractor.ui.settings.preferences

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import domilopment.apkextractor.R

@Composable
inline fun <reified T> ListPreference(
    icon: ImageVector? = null,
    enabled: Boolean = true,
    @StringRes iconDesc: Int? = null,
    @StringRes name: Int,
    @StringRes summary: Int? = null,
    @ArrayRes entries: Int,
    @ArrayRes entryValues: Int,
    state: T,
    crossinline onClick: (T) -> Unit
) {
    ListPreference(
        icon = icon,
        enabled = enabled,
        iconDesc = iconDesc?.let { stringResource(id = it) },
        name = stringResource(id = name),
        summary = summary?.let { stringResource(id = it) },
        entries = stringArrayResource(id = entries),
        entryValues = stringArrayResource(id = entryValues),
        state = state,
        onClick = onClick
    )
}

@Composable
inline fun <reified T> ListPreference(
    icon: ImageVector? = null,
    enabled: Boolean = true,
    iconDesc: String? = null,
    name: String,
    summary: String? = null,
    entries: Array<String>,
    entryValues: Array<String>,
    state: T,
    crossinline onClick: (T) -> Unit
) {
    val entriesMap = remember(entries, entryValues) { entries.zip(entryValues) }

    var value by rememberSaveable {
        mutableStateOf(state)
    }

    var dialog by rememberSaveable {
        mutableStateOf(false)
    }

    Preference(icon = icon,
        iconDesc = iconDesc,
        name = name,
        summary = summary,
        enabled = enabled,
        onClick = {
            value = state
            dialog = true
        })

    if (dialog) AlertDialog(onDismissRequest = { dialog = false },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { dialog = false }) {
                Text(text = stringResource(id = R.string.app_name_dialog_cancel))
            }
        },
        title = { Text(text = name) },
        text = {
            LazyColumn {
                items(items = entriesMap, key = { it.second }) {
                    ListItem(headlineContent = { Text(text = it.first) },
                        modifier = Modifier.clickable {
                            val newValue = when (T::class) {
                                Int::class -> it.second.toInt() as T
                                String::class -> it.second as T
                                else -> error("Unknown Generic Type")
                            }
                            onClick(newValue)
                            dialog = false
                        },
                        leadingContent = {
                            RadioButton(
                                selected = value.toString() == it.second, onClick = null
                            )
                        })
                }
            }
        })
}

