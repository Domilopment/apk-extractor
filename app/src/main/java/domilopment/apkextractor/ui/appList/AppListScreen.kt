package domilopment.apkextractor.ui.appList

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import domilopment.apkextractor.InstallerActivity
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.Screen
import domilopment.apkextractor.data.model.appList.ExtractionResult
import domilopment.apkextractor.data.model.appList.ShareResult
import domilopment.apkextractor.ui.dialogs.AppFilterBottomSheet
import domilopment.apkextractor.ui.dialogs.AppOptionsBottomSheet
import domilopment.apkextractor.ui.dialogs.ExtractionResultDialog
import domilopment.apkextractor.ui.dialogs.ProgressDialog
import domilopment.apkextractor.ui.viewModels.AppListViewModel
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.MySnackbarVisuals
import domilopment.apkextractor.utils.apkActions.ApkActionsManager
import domilopment.apkextractor.utils.apkActions.ApkActionsOptions
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AppListScreen(
    model: AppListViewModel,
    searchString: String,
    onNavigate: () -> Unit,
    showSnackbar: (MySnackbarVisuals) -> Unit,
    isActionMode: Boolean,
    onTriggerActionMode: () -> Unit,
    isActionModeAllItemsSelected: Boolean,
    onAppSelection: (Boolean, Int) -> Unit
) {
    val context = LocalContext.current
    val state by model.mainFragmentState.collectAsStateWithLifecycle()
    val updatedSysApps by model.updatedSystemApps.collectAsStateWithLifecycle()
    val systemApps by model.systemApps.collectAsStateWithLifecycle()
    val userApps by model.userApps.collectAsStateWithLifecycle()
    val sortAsc by model.appSortAsc.collectAsStateWithLifecycle()
    val sort by model.appSortOrder.collectAsStateWithLifecycle()
    val sortFavorites by model.appSortFavorites.collectAsStateWithLifecycle()
    val installationSource by model.filterInstaller.collectAsStateWithLifecycle()
    val category by model.filterCategory.collectAsStateWithLifecycle()
    val otherFilter by model.filterOthers.collectAsStateWithLifecycle()
    val rightSwipeAction by model.rightSwipeAction.collectAsStateWithLifecycle()
    val leftSwipeAction by model.leftSwipeAction.collectAsStateWithLifecycle()
    val swipeActionCustomThreshold by model.swipeActionCustomThreshold.collectAsStateWithLifecycle()
    val swipeActionThresholdMod by model.swipeActionThresholdMod.collectAsStateWithLifecycle()

    val progressDialogState by model.progressDialogState.collectAsStateWithLifecycle()

    // does user has selected the filter option dialog
    var showFilter by rememberSaveable {
        mutableStateOf(false)
    }

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
        /**
         * Set and update Search query in viewmodel
         */
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

    LaunchedEffect(key1 = Unit) {
        /**
         * Handles Information about apk save process result
         */
        model.extractionResult.collect { extractionResult ->
            when (extractionResult) {
                ExtractionResult.None -> Toast.makeText(
                    context, R.string.toast_save_apps, Toast.LENGTH_SHORT
                ).show()

                is ExtractionResult.SuccessSingle, is ExtractionResult.SuccessMultiple -> {
                    val backupsCount =
                        if (extractionResult is ExtractionResult.SuccessMultiple) extractionResult.backupsCount else 1
                    showSnackbar(
                        MySnackbarVisuals(
                            duration = SnackbarDuration.Long,
                            message = context.resources.getQuantityString(
                                R.plurals.snackbar_successful_extracted_multiple,
                                backupsCount,
                                extractionResult.app!!.appName,
                                backupsCount - 1
                            )
                        )
                    )
                }

                is ExtractionResult.Failure -> extractionError = extractionResult
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
                Intent.createChooser(it, context.getString(R.string.share_intent_title))
            }?.also {
                shareApp.launch(it)
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        // all actions inside Appbar or Bottombar the user can trigger in this screen
        Screen.AppList.buttons.onEach { button ->
            when (button) {
                Screen.ScreenActions.FilterList -> showFilter = true
                Screen.ScreenActions.Refresh -> model.updateApps()
                Screen.ScreenActions.Settings -> onNavigate()
                Screen.ScreenActions.Save -> model.saveSelectedApps()
                Screen.ScreenActions.Share -> model.createShareUrisForSelectedApps()

                else -> Unit
            }
        }.launchIn(this)
    }

    state.selectedApp?.let { selectedApp ->
        AppOptionsBottomSheet(
            app = selectedApp,
            onDismissRequest = { model.selectApplication(null) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onFavoriteChanged = { isChecked ->
                model.editFavorites(selectedApp.appPackageName, isChecked)
            },
            onActionSave = { model.saveApp(selectedApp) },
            saveResult = model.extractionResult,
            onActionShare = { model.createShareUrisForApp(selectedApp) },
            onActionSaveImage = saveImage,
            uninstalledAppFound = model::uninstallApps
        )
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
        onDismissRequest = model::resetProgress,
        onCancel = model::resetProgress
    )

    extractionError?.let { (app, errorMessage) ->
        ExtractionResultDialog(
            onDismissRequest = { extractionError = null },
            appName = app.appName,
            errorMessage = errorMessage
        )
    }

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
        isRefreshing = state.isRefreshing,
        isPullToRefresh = !isActionMode,
        onRefresh = model::updateApps,
        rightSwipeAction = rightSwipeAction,
        leftSwipeAction = leftSwipeAction,
        swipeActionCallback = { app, apkAction ->
            apkAction.getAction(
                context,
                app,
                ApkActionsOptions.ApkActionOptionParams.Builder().saveFunction(model::saveApp)
                    .setCallbackFun(showSnackbar).setShareFunction(model::createShareUrisForApp)
                    .setDeleteResult(InstallerActivity::class.java).build()
            )
        },
        isSwipeActionCustomThreshold = swipeActionCustomThreshold,
        swipeActionThresholdModifier = swipeActionThresholdMod,
        uninstalledAppFound = model::uninstallApps
    )
}
