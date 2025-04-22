package domilopment.apkextractor.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import domilopment.apkextractor.R
import domilopment.apkextractor.data.InstallationResultType

@Composable
fun InstallationResultDialog(
    onDismissRequest: () -> Unit, result: InstallationResultType
) {
    val title = remember(result) {
        when (result) {
            // Installation Results
            is InstallationResultType.Failure.Install -> R.string.installation_result_dialog_failed_title
            is InstallationResultType.Success.Installed -> R.string.installation_result_dialog_success_title

            // Uninstallation Results
            is InstallationResultType.Failure.Uninstall -> R.string.uninstallation_result_dialog_failed_title
            is InstallationResultType.Success.Uninstalled -> R.string.uninstallation_result_dialog_success_title

            // Security
            is InstallationResultType.Failure.Security -> R.string.pending_user_action_security_exception_dialog_title
        }
    }
    val message = remember(result) {
        when (result) {
            // Installation Results
            is InstallationResultType.Failure.Install -> R.string.installation_result_dialog_failed_message
            is InstallationResultType.Success.Installed -> R.string.installation_result_dialog_success_message

            // Uninstallation Results
            is InstallationResultType.Failure.Uninstall -> R.string.uninstallation_result_dialog_failed_message
            is InstallationResultType.Success.Uninstalled -> R.string.uninstallation_result_dialog_success_message

            // Security
            is InstallationResultType.Failure.Security -> R.string.pending_user_action_security_exception_dialog_message
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
                result.packageName ?: "App",
                if (result is InstallationResultType.Failure) result.errorMessage ?: "" else ""
            )
        )
    })
}