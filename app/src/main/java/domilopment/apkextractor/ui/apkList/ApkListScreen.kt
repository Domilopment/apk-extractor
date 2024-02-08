package domilopment.apkextractor.ui.apkList

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import domilopment.apkextractor.InstallXapkActivity
import domilopment.apkextractor.R
import domilopment.apkextractor.data.apkList.AppPackageArchiveModel
import domilopment.apkextractor.ui.Screen
import domilopment.apkextractor.ui.dialogs.ApkOptionBottomSheet
import domilopment.apkextractor.ui.dialogs.ApkSortMenu
import domilopment.apkextractor.ui.viewModels.ApkListViewModel
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.MySnackbarVisuals
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.eventHandler.Event
import domilopment.apkextractor.utils.eventHandler.EventDispatcher
import domilopment.apkextractor.utils.eventHandler.EventType
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkListScreen(
    model: ApkListViewModel,
    searchString: String,
    onNavigate: () -> Unit,
    showSnackbar: (MySnackbarVisuals) -> Unit
) {
    val context = LocalContext.current
    val state by model.apkListFragmentState.collectAsState()
    val saveDir by model.saveDir.collectAsState()
    val sortOrder by model.sortOrder.collectAsState()

    var sortDialog by remember {
        mutableStateOf(false)
    }

    val selectApk =
        rememberLauncherForActivityResult(object : ActivityResultContracts.OpenDocument() {
            override fun createIntent(context: Context, input: Array<String>): Intent {
                val pickerInitialUri = saveDir?.let {
                    DocumentsContract.buildDocumentUriUsingTree(
                        it, DocumentsContract.getTreeDocumentId(it)
                    )
                }
                return super.createIntent(context, input).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
                }
            }
        }) {
            it?.let { apkUri ->
                FileUtil(context).getDocumentInfo(
                    apkUri,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                    DocumentsContract.Document.COLUMN_SIZE
                )
            }?.let { documentFile ->
                AppPackageArchiveModel(
                    documentFile.uri,
                    documentFile.displayName!!,
                    documentFile.mimeType!!,
                    documentFile.lastModified!!,
                    documentFile.size!!
                )
            }?.also { apk ->
                model.selectPackageArchive(apk)
            } ?: Toast.makeText(
                context, context.getString(R.string.alert_apk_selected_failed), Toast.LENGTH_LONG
            ).show()
        }

    val uninstallApp =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val packageName = state.selectedPackageArchiveModel?.appPackageName
            if (state.selectedPackageArchiveModel?.appPackageName.isNullOrBlank()) return@rememberLauncherForActivityResult

            val isAppUninstalled = !Utils.isPackageInstalled(context.packageManager, packageName!!)
            if (isAppUninstalled) {
                EventDispatcher.emitEvent(Event(EventType.UNINSTALLED, packageName))
            }
        }

    LaunchedEffect(key1 = searchString) {
        model.searchQuery(searchString)
    }

    LaunchedEffect(key1 = Unit) {
        Screen.ApkList.buttons.onEach { button ->
            when (button) {
                Screen.ScreenActions.Sort -> sortDialog = true
                Screen.ScreenActions.OpenExplorer -> selectApk.launch(arrayOf(FileUtil.MIME_TYPE))
                Screen.ScreenActions.Refresh -> model.updatePackageArchives()
                Screen.ScreenActions.Settings -> onNavigate()

                else -> Unit
            }
        }.launchIn(this)
    }

    ApkSortMenu(
        sortOrder = sortOrder,
        expanded = sortDialog,
        onDismissRequest = { sortDialog = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        sort = model::sort
    )

    state.selectedPackageArchiveModel?.let {
        ApkOptionBottomSheet(
            apk = it,
            onDismissRequest = { model.selectPackageArchive(null) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onRefresh = { model.forceRefresh(it) },
            onActionShare = {
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = FileUtil.MIME_TYPE
                    putExtra(Intent.EXTRA_STREAM, it.fileUri)
                }, context.getString(R.string.share_intent_title)))
            },
            onActionInstall = {
                Intent(context, InstallXapkActivity::class.java).apply {
                    setDataAndType(it.fileUri, it.fileType)
                }.let { intent ->
                    context.startActivity(intent)
                }
            },
            onActionDelete = {
                DocumentsContract.deleteDocument(
                    context.contentResolver, it.fileUri
                ).let { deleted ->
                    Toast.makeText(
                        context, context.getString(
                            if (deleted) {
                                model.remove(it)
                                R.string.apk_action_delete_success
                            } else R.string.apk_action_delete_failed
                        ), Toast.LENGTH_SHORT
                    )
                }.show()
            },
            onActionUninstall = {
                uninstallApp.launch(
                    Intent(
                        Intent.ACTION_DELETE, Uri.fromParts("package", it.appPackageName, null)
                    )
                )
            },
            deletedDocumentFound = model::remove
        )
    }

    ApkListContent(
        apkList = state.appList,
        searchString = searchString,
        refreshing = state.isRefreshing,
        isPullToRefresh = true,
        onRefresh = model::updatePackageArchives,
        onClick = model::selectPackageArchive,
        deletedDocumentFound = model::remove
    )
}