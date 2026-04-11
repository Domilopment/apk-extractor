package domilopment.apkextractor.ui.apkList

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.navigation.Route
import domilopment.apkextractor.ui.ScreenConfig
import domilopment.apkextractor.ui.dialogs.ApkSortMenu
import domilopment.apkextractor.ui.viewModels.ApkListViewModel
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.MySnackbarVisuals
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkListScreen(
    model: ApkListViewModel,
    searchString: String,
    showSnackbar: (MySnackbarVisuals) -> Unit,
    onApkClick: (Uri) -> Unit
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val state by model.apkListFragmentState.collectAsStateWithLifecycle()
    val saveDir by model.saveDir.collectAsStateWithLifecycle()
    val sortOrder by model.sortOrder.collectAsStateWithLifecycle()

    var sortDialog by remember {
        mutableStateOf(false)
    }

    var isNavigating by remember { mutableStateOf(false) }

    LaunchedEffect(isNavigating) {
        if (isNavigating) {
            delay(500)
            isNavigating = false
        }
    }

    val takenSpace by remember {
        derivedStateOf {
            state.appList.sumOf { it.fileSize }
        }
    }

    val (totalSpace, freeSpace) = remember(state.appList) {
        Environment.getExternalStorageDirectory().let {
            Pair(it.totalSpace, it.freeSpace)
        }
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
            it?.also { uri ->
                onApkClick(uri)
            } ?: Toast.makeText(
                context, resources.getString(R.string.alert_apk_selected_failed), Toast.LENGTH_LONG
            ).show()
        }

    LaunchedEffect(key1 = searchString) {
        model.searchQuery(searchString)
    }

    LaunchedEffect(key1 = Unit) {
        Route.ApkList.buttons.onEach { button ->
            when (button) {
                ScreenConfig.ScreenActions.Sort -> sortDialog = true
                ScreenConfig.ScreenActions.OpenExplorer -> selectApk.launch(arrayOf(FileUtil.FileInfo.APK.mimeType))
                ScreenConfig.ScreenActions.Refresh -> model.updatePackageArchives()

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

    ApkListContent(
        apkList = state.appList,
        totalSpace = totalSpace,
        takenSpace = takenSpace,
        freeSpace = freeSpace,
        searchString = searchString,
        isRefreshing = state.isRefreshing,
        isPullToRefresh = true,
        onRefresh = model::updatePackageArchives,
        onClick = {
            if (!isNavigating) {
                isNavigating = true
                onApkClick(it.fileUri)
            }
        },
        isApkFileDeleted = { apk ->
            !FileUtil.doesDocumentExist(context, apk.fileUri)
        },
        deletedDocumentFound = model::remove,
        onStorageInfoClick = {
            Intent(android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS).let {
                context.startActivity(it)
            }
        })
}