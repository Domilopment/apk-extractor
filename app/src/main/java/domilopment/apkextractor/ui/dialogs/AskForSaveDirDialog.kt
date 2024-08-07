package domilopment.apkextractor.ui.dialogs

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import domilopment.apkextractor.R
import domilopment.apkextractor.data.repository.analytics.AnalyticsHelper
import domilopment.apkextractor.data.repository.analytics.LocalAnalyticsHelper

@Composable
fun AskForSaveDirDialog(
    chooseSaveDir: ActivityResultLauncher<Uri?>, context: Context = LocalContext.current
) {
    val analytics = LocalAnalyticsHelper.current
    AlertDialog(onDismissRequest = {
        analytics.logEvent(AnalyticsHelper.Events.SAVE_DIR_DIALOG, Bundle().apply {
            putString(AnalyticsHelper.Param.CONTENT_TYPE, "on_dismiss")
        })
        (context as? Activity)?.finish()
    }, confirmButton = {
        TextButton(onClick = {
            analytics.logEvent(AnalyticsHelper.Events.SAVE_DIR_DIALOG, Bundle().apply {
                putString(AnalyticsHelper.Param.CONTENT_TYPE, "on_confirm")
            })
            chooseSaveDir.launch(null)
        }) {
            Text(text = stringResource(id = R.string.alert_save_path_ok))
        }
    }, title = {
        Text(text = stringResource(id = R.string.alert_save_path_title))
    }, text = {
        Text(text = stringResource(id = R.string.alert_save_path_message))
    }, properties = DialogProperties(dismissOnClickOutside = false))
}