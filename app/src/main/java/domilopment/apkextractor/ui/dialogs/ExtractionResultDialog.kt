package domilopment.apkextractor.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import domilopment.apkextractor.R

@Composable
fun ExtractionResultDialog(onDismissRequest: () -> Unit, appName: String, errorMessage: String?) {
    val context = LocalContext.current
    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        TextButton(onClick = onDismissRequest) {
            Text(text = stringResource(id = R.string.snackbar_extraction_failed_message_dismiss))
        }
    }, dismissButton = {
        TextButton(onClick = {
            val clipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("APK Extractor: Error Message", errorMessage)
            clipboardManager.setPrimaryClip(clip)
        }) {
            Text(text = stringResource(id = R.string.snackbar_extraction_failed_message_copy_to_clipboard))
        }
    }, title = {
        Text(
            text = stringResource(
                id = R.string.snackbar_extraction_failed, appName
            )
        )
    }, text = {
        Text(
            text = stringResource(
                id = R.string.snackbar_extraction_failed_message,
                errorMessage ?: "No error message provided"
            )
        )
    })
}