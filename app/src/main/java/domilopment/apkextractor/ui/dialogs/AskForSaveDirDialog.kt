package domilopment.apkextractor.ui.dialogs

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import domilopment.apkextractor.R
import domilopment.apkextractor.data.repository.analytics.AnalyticsHelper
import domilopment.apkextractor.data.repository.analytics.LocalAnalyticsHelper
import domilopment.apkextractor.ui.components.DoubleBackPressDialog
import domilopment.apkextractor.utils.fadingBottom
import domilopment.apkextractor.utils.fadingTop
import timber.log.Timber

@Composable
fun AskForSaveDirDialog(
    chooseSaveDir: ActivityResultLauncher<Uri?>, context: Context = LocalContext.current
) {
    val analytics = LocalAnalyticsHelper.current
    var error: Boolean by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val dismissLambda: () -> Unit = {
        analytics.logEvent(AnalyticsHelper.Events.SAVE_DIR_DIALOG) {
            putString(AnalyticsHelper.Param.CONTENT_TYPE, "on_dismiss")
        }
        (context as? Activity)?.finish()
    }

    DoubleBackPressDialog(onDismissRequest = dismissLambda,
        confirmButton = {
            TextButton(onClick = {
                analytics.logEvent(AnalyticsHelper.Events.SAVE_DIR_DIALOG) {
                    putString(AnalyticsHelper.Param.CONTENT_TYPE, "on_confirm")
                }
                try {
                    chooseSaveDir.launch(null)
                } catch (e: ActivityNotFoundException) {
                    Timber.tag("Initial Ask for Save Dir Dialog").e(e)
                    error = true
                }

            }) {
                Text(text = stringResource(id = if (error) R.string.alert_save_path_error_ok else R.string.alert_save_path_ok))
            }
        },
        backPressNotice = stringResource(id = R.string.double_back_press_first_press_toast_text),
        dismissButton = {
            if (error) TextButton(onClick = dismissLambda) {
                Text(text = stringResource(id = R.string.alert_save_path_error_cancel))
            }
        },
        icon = {
            if (error) Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            else Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            if (error) Text(text = stringResource(id = R.string.alert_save_path_error_title))
            else Text(text = stringResource(id = R.string.alert_save_path_title))
        },
        text = {
            Column(
                Modifier
                    .fadingTop(visible = scrollState.canScrollBackward)
                    .fadingBottom(visible = scrollState.canScrollForward)
                    .verticalScroll(state = scrollState)
            ) {
                if (error) Text(text = stringResource(id = R.string.alert_save_path_error_message))
                else Text(text = stringResource(id = R.string.alert_save_path_message))
            }
        },
        properties = DialogProperties(dismissOnClickOutside = false),
        context = context
    )
}