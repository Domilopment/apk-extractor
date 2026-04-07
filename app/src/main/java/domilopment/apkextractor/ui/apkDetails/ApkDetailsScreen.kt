package domilopment.apkextractor.ui.apkDetails

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import domilopment.apkextractor.InstallerActivity
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.appDetails.AppDetailsSkeleton
import domilopment.apkextractor.ui.viewModels.ApkDetailViewModel
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.MySnackbarVisuals
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ApkDetailsScreen(
    model: ApkDetailViewModel,
    onDismissRequest: () -> Unit,
    showSnackbar: (MySnackbarVisuals) -> Unit,
    showAskForSaveDirDialog: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val resources = LocalResources.current

    val state by model.uiState.collectAsStateWithLifecycle()

    if (!state.isLoading) {
        state.app?.let { apk ->
            Column(modifier = modifier) {
                ApkDetailsContent(
                    apk = apk,
                    onDismissRequest = { onDismissRequest() },
                    onRefresh = { model.loadPackageArchiveInfo(apk) },
                    onActionShare = {
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = FileUtil.FileInfo.APK.mimeType
                            putExtra(Intent.EXTRA_STREAM, apk.fileUri)
                        }, resources.getString(R.string.share_intent_title)))
                    },
                    onActionInstall = {
                        Intent(context, InstallerActivity::class.java).apply {
                            setDataAndType(apk.fileUri, apk.fileType)
                            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        }.let { intent ->
                            context.startActivity(intent)
                        }
                    },
                    onActionDelete = {
                        runBlocking {
                            FileUtil.deleteDocument(context, apk.fileUri).let { deleted ->
                                Toast.makeText(
                                    context, resources.getString(
                                        if (deleted) {
                                            model.remove(apk)
                                            onDismissRequest()
                                            R.string.apk_action_delete_success
                                        } else R.string.apk_action_delete_failed
                                    ), Toast.LENGTH_SHORT
                                )
                            }.show()
                        }
                    },
                    onActionUninstall = {
                        Intent(context, InstallerActivity::class.java).apply {
                            data = Uri.fromParts("package", apk.appPackageName, null)
                            flags =
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        }.let { intent ->
                            context.startActivity(intent)
                        }
                    },
                    deletedDocumentFound = model::remove
                )
            }
        } ?: onDismissRequest()
    } else {
        AppDetailsSkeleton(modifier = modifier)
    }
}