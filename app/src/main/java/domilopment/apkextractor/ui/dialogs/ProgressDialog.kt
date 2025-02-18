package domilopment.apkextractor.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ProgressDialogUiState

@Composable
fun ProgressDialog(
    state: ProgressDialogUiState,
    onDismissRequest: () -> Unit,
    onCancel: () -> Unit,
    dismissOnBackPress: Boolean = false,
    dismissOnClickOutside: Boolean = false
) {
    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        TextButton(onClick = onCancel) {
            Text(text = stringResource(id = R.string.app_name_dialog_cancel))
        }
    }, text = {
        Column(
            verticalArrangement = Arrangement.Center,
        ) {
            if (state.progress == 0f) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            else LinearProgressIndicator(
                progress = { state.progress / state.tasks }, modifier = Modifier.fillMaxWidth()
            )
            Text(text = state.process ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(
                        id = R.string.progress_dialog_percentage,
                        if (state.tasks > 0) (state.progress / state.tasks) * 100 else 0f
                    )
                )
                Text(
                    text = stringResource(
                        id = R.string.progress_dialog_value, state.progress.toInt(), state.tasks
                    )
                )
            }
        }
    }, title = {
        Text(
            text = stringResource(
                id = state.title?.id ?: R.string.progress_dialog_title_placeholder,
                formatArgs = state.title?.args ?: emptyArray()
            )
        )
    }, properties = DialogProperties(
        dismissOnBackPress = dismissOnBackPress, dismissOnClickOutside = dismissOnClickOutside
    )
    )
}