package domilopment.apkextractor.ui.settings.preferences

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import domilopment.apkextractor.R

@Composable
fun DialogPreference(
    icon: ImageVector? = null,
    enabled: Boolean = true,
    @StringRes iconDesc: Int? = null,
    @StringRes name: Int,
    @StringRes summary: Int? = null,
    dialogContent: @Composable (() -> Unit),
    onConfirm: () -> Unit,
) {
    DialogPreference(
        icon = icon,
        enabled = enabled,
        iconDesc = iconDesc?.let { stringResource(id = it) },
        name = stringResource(id = name),
        summary = summary?.let { stringResource(id = it) },
        dialogTitle = stringResource(id = name),
        dialogContent = dialogContent,
        onConfirm = onConfirm
    )
}

@Composable
fun DialogPreference(
    icon: ImageVector? = null,
    enabled: Boolean = true,
    iconDesc: String? = null,
    name: String,
    summary: String? = null,
    dialogTitle: String,
    dialogContent: @Composable (() -> Unit),
    onConfirm: () -> Unit,
) {
    var isVisible by rememberSaveable {
        mutableStateOf(false)
    }

    Preference(name = name,
        enabled = enabled,
        icon = icon,
        iconDesc = iconDesc,
        summary = summary,
        onClick = { isVisible = true })

    if (isVisible) AlertDialog(onDismissRequest = { isVisible = false }, confirmButton = {
        TextButton(onClick = {
            onConfirm()
            isVisible = false
        }) {
            Text(text = stringResource(id = R.string.app_name_dialog_ok))
        }
    }, dismissButton = {
        TextButton(onClick = { isVisible = false }) {
            Text(text = stringResource(id = R.string.app_name_dialog_cancel))
        }
    }, title = { Text(text = dialogTitle) }, text = dialogContent)
}