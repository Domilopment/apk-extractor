package domilopment.apkextractor.ui.settings.preferences

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import domilopment.apkextractor.R
import domilopment.apkextractor.utils.conditional

@Composable
fun DialogPreference(
    icon: ImageVector? = null,
    enabled: Boolean = true,
    @StringRes iconDesc: Int? = null,
    @StringRes name: Int,
    @StringRes summary: Int? = null,
    onClick: (() -> Unit)? = null,
    dialogState: DialogPreferenceState = rememberDialogPreferenceState(),
    scrollable: Boolean = true,
    dialogContent: @Composable (() -> Unit),
    onConfirm: (() -> Unit)? = null,
    onConfirmEnabled: Boolean = true,
    onDismiss: (() -> Unit)? = null,
) {
    DialogPreference(
        icon = icon,
        enabled = enabled,
        iconDesc = iconDesc?.let { stringResource(id = it) },
        name = stringResource(id = name),
        summary = summary?.let { stringResource(id = it) },
        onClick = onClick,
        dialogState = dialogState,
        scrollable = scrollable,
        dialogContent = dialogContent,
        onConfirm = onConfirm,
        onConfirmEnabled = onConfirmEnabled,
        onDismiss = onDismiss
    )
}

@Composable
fun DialogPreference(
    icon: ImageVector? = null,
    enabled: Boolean = true,
    iconDesc: String? = null,
    name: String,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
    dialogState: DialogPreferenceState = rememberDialogPreferenceState(),
    scrollable: Boolean = true,
    dialogContent: @Composable (() -> Unit),
    onConfirm: (() -> Unit)? = null,
    onConfirmEnabled: Boolean = true,
    onDismiss: (() -> Unit)? = null,
) {
    Preference(name = name,
        enabled = enabled,
        icon = icon,
        iconDesc = iconDesc,
        summary = summary,
        onClick = {
            onClick?.invoke()
            dialogState.show()
        })

    if (dialogState.value) AlertDialog(onDismissRequest = { dialogState.hide() }, confirmButton = {
        if (onConfirm != null) TextButton(onClick = {
            onConfirm()
            dialogState.hide()
        }, enabled = onConfirmEnabled) {
            Text(text = stringResource(id = R.string.app_name_dialog_ok))
        }
    }, dismissButton = {
        TextButton(onClick = {
            onDismiss?.invoke()
            dialogState.hide()
        }) {
            Text(text = stringResource(id = R.string.app_name_dialog_cancel))
        }
    }, title = { Text(text = name) }, text = {
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier.conditional(condition = scrollable,
                ifTrue = { verticalScroll(state = scrollState) })
        ) {
            dialogContent()
        }
    })
}

@Composable
fun rememberDialogPreferenceState(initial: Boolean = false): DialogPreferenceState {
    return rememberSaveable(saver = object : Saver<DialogPreferenceState, Boolean> {
        override fun restore(value: Boolean): DialogPreferenceState =
            DialogPreferenceState(initial = value)

        override fun SaverScope.save(value: DialogPreferenceState): Boolean = value.value
    }) {
        DialogPreferenceState(initial)
    }
}

@Stable
class DialogPreferenceState(initial: Boolean) {
    var value: Boolean by mutableStateOf(initial)
        private set

    fun show() {
        value = true
    }

    fun hide() {
        value = false
    }
}