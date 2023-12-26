package domilopment.apkextractor.ui.dialogs

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import domilopment.apkextractor.R

@Composable
fun AskForSaveDirDialog(chooseSaveDir: ActivityResultLauncher<Uri?>) {
    AlertDialog(onDismissRequest = {}, confirmButton = {
        Button(onClick = {
            chooseSaveDir.launch(null)
        }) {
            Text(text = stringResource(id = R.string.alert_save_path_ok))
        }
    }, title = {
        Text(text = stringResource(id = R.string.alert_save_path_title))
    }, text = {
        Text(text = stringResource(id = R.string.alert_save_path_message))
    }, properties = DialogProperties(
        dismissOnBackPress = false, dismissOnClickOutside = false
    )
    )
}