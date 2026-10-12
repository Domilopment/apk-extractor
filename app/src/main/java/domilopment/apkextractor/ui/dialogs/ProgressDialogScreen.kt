package domilopment.apkextractor.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import domilopment.apkextractor.R
import domilopment.apkextractor.data.TaskUiState
import domilopment.apkextractor.ui.viewModels.ProgressDialogViewModel

@Composable
fun ProgressDialogScreen(
    model: ProgressDialogViewModel,
    onDismissRequest: () -> Unit,
) {
    val state by model.progressDialogState.collectAsStateWithLifecycle()

    when (state) {
        null, TaskUiState.Suspended -> LaunchedEffect(Unit) {
            onDismissRequest()
        }

        TaskUiState.Pending -> Unit

        is TaskUiState.Progress -> Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            modifier = Modifier.widthIn(min = 280.dp, max = 560.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Title
                ProvideTextStyle(value = MaterialTheme.typography.headlineSmall) {
                    ProgressDialogTitle(state = state as TaskUiState.Progress)
                }

                // Content
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 24.dp)
                        .fillMaxWidth()
                ) {
                    ProgressDialogContent(state = state as TaskUiState.Progress)
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { model.resetProgress() }) {
                        Text(text = stringResource(id = R.string.app_name_dialog_cancel))
                    }
                }
            }
        }

        is TaskUiState.Result -> LaunchedEffect(Unit) {
            onDismissRequest()
        }
    }
}
