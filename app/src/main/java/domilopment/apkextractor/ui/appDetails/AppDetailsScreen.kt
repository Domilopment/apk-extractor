package domilopment.apkextractor.ui.appDetails

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import domilopment.apkextractor.R
import domilopment.apkextractor.data.model.appList.ExtractionResult
import domilopment.apkextractor.data.model.appList.ShareResult
import domilopment.apkextractor.ui.dialogs.ExtractionResultDialog
import domilopment.apkextractor.ui.dialogs.ProgressDialog
import domilopment.apkextractor.ui.viewModels.AppDetailViewModel
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.MySnackbarVisuals
import domilopment.apkextractor.utils.apkActions.ApkActionIntent
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppDetailsScreen(
    model: AppDetailViewModel,
    onDismissRequest: () -> Unit,
    showSnackbar: (MySnackbarVisuals) -> Unit,
    showAskForSaveDirDialog: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val resources = LocalResources.current

    val state by model.uiState.collectAsStateWithLifecycle()
    val progressDialogState by model.progressDialogState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // information of error if last extraction has failed, to show inside a dialog
    var extractionError: ExtractionResult.Failure? by remember {
        mutableStateOf(null)
    }

    // After finish share apk file, clear the temp folder so the temp.apk files is deleted as well
    val shareApp = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        context.cacheDir.deleteRecursively()
    }

    // check if we have permission to save the app icon to storage
    val saveImagePermissionRequest =
        rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE) { isPermissionGranted ->
            if (isPermissionGranted) state.app?.let {
                model.appActionIntent(ApkActionIntent.Icon(it, showSnackbar))
            } else showSnackbar(
                MySnackbarVisuals(
                    duration = SnackbarDuration.Short,
                    message = resources.getString(R.string.snackbar_need_permission_save_image),
                )
            )
        }

    LaunchedEffect(key1 = Unit) {
        model.extractionResult.collect { extractionResult ->
            when (extractionResult) {
                is ExtractionResult.SuccessSingle -> snackbarHostState.showSnackbar(
                    MySnackbarVisuals(
                        duration = SnackbarDuration.Short, message = resources.getString(
                            R.string.snackbar_successful_extracted, extractionResult.app.appName
                        )
                    )
                )

                is ExtractionResult.Failure -> extractionError = extractionResult
                is ExtractionResult.NoSaveDir -> showAskForSaveDirDialog(true)

                else -> Unit
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        /**
         * Creates Intent for Apps to Share
         */
        model.shareResult.collect { shareResult ->
            if (shareResult == ShareResult.None) {
                Toast.makeText(
                    context, R.string.toast_share_app, Toast.LENGTH_SHORT
                ).show()
                return@collect
            }

            val action =
                if (shareResult is ShareResult.SuccessSingle) Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE
            Intent(action).apply {
                type = FileUtil.FileInfo.APK.mimeType
                when (shareResult) {
                    is ShareResult.SuccessMultiple -> {
                        clipData = ClipData.newRawUri(null, shareResult.uris[0]).apply {
                            shareResult.uris.drop(1).forEach { addItem(ClipData.Item(it)) }
                        }
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareResult.uris)
                    }

                    is ShareResult.SuccessSingle -> putExtra(
                        Intent.EXTRA_STREAM, shareResult.uri
                    )

                    else -> Unit
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }.let {
                Intent.createChooser(it, resources.getString(R.string.share_intent_title))
            }?.also {
                shareApp.launch(it)
            }
        }
    }

    if (!state.isLoading) {
        state.app?.let { app ->
            Column(modifier = modifier) {
                SnackbarHost(hostState = snackbarHostState) {
                    Snackbar(
                        snackbarData = it,
                        contentColor = (it.visuals as? MySnackbarVisuals)?.messageColor
                            ?: SnackbarDefaults.contentColor
                    )
                }
                AppDetailsContent(
                    app = app,
                    onDismissRequest = onDismissRequest,
                    onShowSnackbar = {
                        scope.launch {
                            snackbarHostState.showSnackbar(it)
                        }
                    },
                    onFavoriteChanged = { isChecked ->
                        model.editFavorites(isChecked)
                    },
                    onActionSaveImagePermissionRequest = saveImagePermissionRequest,
                    onActionIntent = model::appActionIntent,
                    uninstalledAppFound = model::removeApp,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
        } ?: onDismissRequest()
    } else {
        AppDetailsSkeleton(modifier = modifier)
    }

    progressDialogState?.let {
        ProgressDialog(
            state = it, onDismissRequest = model::resetProgress, onCancel = model::resetProgress
        )
    }

    extractionError?.let { (app, errorMessage) ->
        ExtractionResultDialog(
            onDismissRequest = { extractionError = null },
            appName = app.appName,
            errorMessage = errorMessage
        )
    }
}