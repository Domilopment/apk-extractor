package domilopment.apkextractor.ui.appList

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.Screen
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.ui.dialogs.AppFilterBottomSheet
import domilopment.apkextractor.ui.dialogs.AppOptionsBottomSheet
import domilopment.apkextractor.ui.dialogs.ProgressDialog
import domilopment.apkextractor.ui.viewModels.AppListViewModel
import domilopment.apkextractor.ui.viewModels.ProgressDialogViewModel
import domilopment.apkextractor.utils.settings.ApplicationUtil
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.MySnackbarVisuals
import domilopment.apkextractor.utils.apkActions.ApkActionsManager
import domilopment.apkextractor.utils.apkActions.ApkActionsOptions
import domilopment.apkextractor.utils.settings.AppSortOptions
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AppListScreen(
    model: AppListViewModel,
    progressDialogModel: ProgressDialogViewModel,
    searchString: String,
    onNavigate: () -> Unit,
    showSnackbar: (MySnackbarVisuals) -> Unit,
    isActionMode: Boolean,
    onTriggerActionMode: () -> Unit,
    isActionModeAllItemsSelected: Boolean,
    onAppSelection: (Boolean, Int) -> Unit
) {
    val context = LocalContext.current
    val state by model.mainFragmentState.collectAsState()
    val saveDir by model.saveDir.collectAsState(initial = null)
    val appName by model.appName.collectAsState(initial = setOf("0:name"))
    val updatedSysApps by model.updatedSystemApps.collectAsState(initial = false)
    val systemApps by model.systemApps.collectAsState(initial = false)
    val userApps by model.userApps.collectAsState(initial = true)
    val sortAsc by model.appSortAsc.collectAsState(initial = true)
    val sort by model.appSortOrder.collectAsState(initial = AppSortOptions.SORT_BY_NAME)
    val sortFavorites by model.appSortFavorites.collectAsState(initial = true)
    val installationSource by model.filterInstaller.collectAsState(initial = null)
    val category by model.filterCategory.collectAsState(initial = null)
    val otherFilter by model.filterOthers.collectAsState(initial = setOf())
    val rightSwipeAction by model.rightSwipeAction.collectAsState(initial = ApkActionsOptions.SAVE)
    val leftSwipeAction by model.leftSwipeAction.collectAsState(initial = ApkActionsOptions.SHARE)

    val progressDialogState by progressDialogModel.progressDialogState.collectAsState()
    val extractionResult by progressDialogModel.extractionResult.collectAsState()
    val shareResult by progressDialogModel.shareResult.collectAsState()

    var showFilter by rememberSaveable {
        mutableStateOf(false)
    }

    var appToUninstall by remember {
        mutableStateOf<ApplicationModel?>(null)
    }

    val shareApp = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        context.cacheDir.deleteRecursively()
    }
    val uninstallApp =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            appToUninstall?.let {
                model.uninstallApps(it)
                appToUninstall = null
            }
        }
    val saveImage =
        rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE) { isPermissionGranted ->
            if (isPermissionGranted) state.selectedApp?.let {
                ApkActionsManager(
                    context, it
                ).actionSaveImage(showSnackbar)
            } else showSnackbar(
                MySnackbarVisuals(
                    duration = SnackbarDuration.Short,
                    message = context.getString(R.string.snackbar_need_permission_save_image),
                )
            )
        }

    LaunchedEffect(key1 = searchString) {
        model.searchQuery(searchString)
    }

    LaunchedEffect(key1 = isActionMode) {
        if (!isActionMode) model.selectAllApps(false)
    }

    LaunchedEffect(key1 = isActionModeAllItemsSelected) {
        if (isActionModeAllItemsSelected) {
            model.selectAllApps(true)
            onAppSelection(true, state.appList.size)
        }
    }

    LaunchedEffect(key1 = extractionResult) {
        extractionResult?.getContentIfNotHandled()?.let { (errorMessage, app, size) ->
            if (errorMessage != null) MaterialAlertDialogBuilder(context).apply {
                setMessage(
                    context.getString(
                        R.string.snackbar_extraction_failed_message, errorMessage
                    )
                )
                setTitle(
                    context.getString(
                        R.string.snackbar_extraction_failed, app?.appPackageName
                    )
                )
                setPositiveButton(R.string.snackbar_extraction_failed_message_dismiss) { dialog, _ ->
                    dialog.dismiss()
                }
                setNeutralButton(R.string.snackbar_extraction_failed_message_copy_to_clipboard) { _, _ ->
                    val clipboardManager =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("APK Extractor: Error Message", errorMessage)
                    clipboardManager.setPrimaryClip(clip)
                }
            }.show()
            else showSnackbar(
                MySnackbarVisuals(
                    duration = SnackbarDuration.Long, message = context.resources.getQuantityString(
                        R.plurals.snackbar_successful_extracted_multiple,
                        size,
                        app?.appName,
                        size - 1
                    )
                )
            )

            progressDialogModel.resetProgress()
        }
    }

    LaunchedEffect(key1 = shareResult) {
        /**
         * Creates Intent for Apps to Share
         */
        shareResult?.getContentIfNotHandled()?.let { files ->
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = FileUtil.MIME_TYPE
                clipData = ClipData.newRawUri(null, files[0]).apply {
                    files.drop(1).forEach { addItem(ClipData.Item(it)) }
                }
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }.let {
                Intent.createChooser(it, context.getString(R.string.share_intent_title))
            }?.also {
                shareApp.launch(it)
            }

            progressDialogModel.resetProgress()
        }
    }

    LaunchedEffect(key1 = Unit) {
        Screen.AppList.buttons.onEach { button ->
            when (button) {
                Screen.ScreenActions.FilterList -> showFilter = true
                Screen.ScreenActions.Refresh -> model.updateApps()
                Screen.ScreenActions.Settings -> onNavigate()
                Screen.ScreenActions.Save -> {
                    state.appList.filter {
                        it.isChecked
                    }.also { list ->
                        if (list.isEmpty()) Toast.makeText(
                            context, R.string.toast_save_apps, Toast.LENGTH_SHORT
                        ).show()
                        else progressDialogModel.saveApps(list)
                    }
                }

                Screen.ScreenActions.Share -> {
                    state.appList.filter {
                        it.isChecked
                    }.also {
                        if (it.isEmpty()) Toast.makeText(
                            context, R.string.toast_share_app, Toast.LENGTH_SHORT
                        ).show()
                        else progressDialogModel.createShareUrisForApps(it)
                    }
                }

                else -> {
                    // Nothing to do
                }
            }
        }.launchIn(this)
    }

    state.selectedApp?.let { selectedApp ->
        AppOptionsBottomSheet(app = selectedApp,
            saveDir = saveDir!!,
            appName = appName,
            onDismissRequest = { model.selectApplication(null) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onFavoriteChanged = { isChecked ->
                model.editFavorites(selectedApp.appPackageName, isChecked)
            },
            onActionShare = shareApp,
            onActionSaveImage = saveImage,
            intentUninstallApp = uninstallApp,
            onActionUninstall = { appToUninstall = selectedApp },
            uninstalledAppFound = { model.uninstallApps(it) })
    }

    if (showFilter) AppFilterBottomSheet(
        updatedSystemApps = updatedSysApps,
        systemApps = systemApps,
        userApps = userApps,
        sortOrder = sortAsc,
        sort = sort,
        prefSortFavorites = sortFavorites,
        installationSource = installationSource,
        appCategory = category,
        otherFilters = otherFilter,
        onDismissRequest = { showFilter = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        changeSelection = model::changeSelection,
        setSortOrder = model::setSortAsc,
        sortApps = model::setSortOrder,
        sortFavorites = model::setSortFavorites,
        setInstallationSource = model::setInstallationSource,
        setCategory = model::setCategory,
        setFilterOthers = model::setOtherFilter
    )

    if (progressDialogState.shouldBeShown) ProgressDialog(
        state = progressDialogState,
        title = stringResource(id = R.string.progress_dialog_title_placeholder),
        onDismissRequest = progressDialogModel::resetProgress,
        onCancel = progressDialogModel::resetProgress
    )

    AppListContent(
        appList = state.appList,
        searchString = searchString,
        isSwipeToDismiss = !isActionMode,
        updateApp = { app ->
            if (isActionMode) {
                val newApp = app.copy(isChecked = !app.isChecked)
                val selectedAppCount =
                    state.appList.map { if (it == app) newApp else it }.count { it.isChecked }
                model.updateApp(newApp)
                onAppSelection(false, selectedAppCount)
            } else model.selectApplication(app)
        },
        triggerActionMode = {
            if (!isActionMode) {
                onTriggerActionMode()
                model.updateApp(it.copy(isChecked = true))
            }
        },
        refreshing = state.isRefreshing,
        isPullToRefresh = !isActionMode,
        onRefresh = model::updateApps,
        rightSwipeAction = rightSwipeAction,
        leftSwipeAction = leftSwipeAction,
        swipeActionCallback = { app, apkAction ->
            appToUninstall = app
            apkAction.getAction(context,
                app,
                ApkActionsOptions.ApkActionOptionParams.Builder().setSaveDir(saveDir!!)
                    .setAppNameBuilder { ApplicationUtil.appName(it, setOf()) }
                    .setCallbackFun(showSnackbar).setShareResult(shareApp)
                    .setDeleteResult(uninstallApp).build())
        },
        uninstalledAppFound = model::uninstallApps
    )
}
