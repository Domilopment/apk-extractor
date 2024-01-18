package domilopment.apkextractor.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ApkInstallationResultType

@Composable
fun InstallationResultDialog(
    onDismissRequest: () -> Unit, result: ApkInstallationResultType
) {
    val title = remember(result) {
        when (result) {
            is ApkInstallationResultType.Failure -> R.string.installation_result_dialog_failed_title
            is ApkInstallationResultType.Success -> R.string.installation_result_dialog_success_title
        }
    }
    val message = remember(result) {
        when (result) {
            is ApkInstallationResultType.Failure -> R.string.installation_result_dialog_failed_message
            is ApkInstallationResultType.Success -> R.string.installation_result_dialog_success_message
        }
    }
    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        TextButton(onClick = onDismissRequest) {
            Text(text = stringResource(id = R.string.installation_result_dialog_ok))
        }
    }, title = {
        Text(text = stringResource(id = title))
    }, text = {
        Text(
            text = stringResource(
                id = message,
                result.packageName.toString(),
                if (result is ApkInstallationResultType.Failure) result.errorMessage ?: "" else ""
            )
        )
    })
}