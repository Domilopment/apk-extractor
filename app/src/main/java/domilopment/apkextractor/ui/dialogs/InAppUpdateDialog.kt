package domilopment.apkextractor.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import domilopment.apkextractor.R
import domilopment.apkextractor.data.InAppUpdateResultType

@Composable
fun InAppUpdateDialog(
    onDismissRequest: () -> Unit,
    confirmButtonOnClick: () -> Unit,
    inAppUpdateResultType: InAppUpdateResultType
) {
    val title = remember(inAppUpdateResultType) {
        when (inAppUpdateResultType) {
            InAppUpdateResultType.CANCELED -> R.string.popup_dialog_for_notify_about_update_title
            InAppUpdateResultType.UPDATE_FAILED -> R.string.popup_dialog_update_failed_title
        }
    }
    val message = remember(inAppUpdateResultType) {
        when (inAppUpdateResultType) {
            InAppUpdateResultType.CANCELED -> R.string.popup_dialog_for_notify_about_update_text
            InAppUpdateResultType.UPDATE_FAILED -> R.string.popup_dialog_update_failed_text
        }
    }
    val confirmButtonText = remember(inAppUpdateResultType) {
        when (inAppUpdateResultType) {
            InAppUpdateResultType.CANCELED -> R.string.popup_dialog_for_notify_about_update_button_positive
            InAppUpdateResultType.UPDATE_FAILED -> R.string.popup_dialog_update_failed_button_positive
        }
    }
    val dismissButtonText = remember(inAppUpdateResultType) {
        when (inAppUpdateResultType) {
            InAppUpdateResultType.CANCELED -> R.string.popup_dialog_for_notify_about_update_button_negative
            InAppUpdateResultType.UPDATE_FAILED -> R.string.popup_dialog_update_failed_button_negative
        }
    }
    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        TextButton(onClick = confirmButtonOnClick) {
            Text(text = stringResource(id = confirmButtonText))
        }
    }, dismissButton = {
        TextButton(onClick = onDismissRequest) {
            Text(text = stringResource(id = dismissButtonText))
        }
    }, title = { Text(text = stringResource(id = title)) }, text = {
        Text(
            text = stringResource(id = message)
        )
    })
}