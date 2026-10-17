package domilopment.apkextractor.ui.appList

import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.ScreenConfig
import domilopment.apkextractor.data.model.appList.ExtractionResult
import domilopment.apkextractor.data.model.appList.ShareResult
import domilopment.apkextractor.ui.navigation.Route
import domilopment.apkextractor.ui.dialogs.AppFilterBottomSheet
import domilopment.apkextractor.ui.dialogs.ExtractionResultDialog
import domilopment.apkextractor.ui.viewModels.AppListViewModel
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.MySnackbarVisuals
import domilopment.apkextractor.utils.PackageName
import domilopment.apkextractor.utils.apkActions.ApkActionIntentParams
import domilopment.apkextractor.utils.apkActions.intent
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    model: AppListViewModel,
    searchString: String,
    showSnackbar: (MySnackbarVisuals) -> Unit,
    appOetailsScreen: (PackageName) -> Unit,
    isActionMode: Boolean,
    isActionModeAllItemsSelected: Boolean,
    onAppSelection: (Boolean, Int) -> Unit,
    showAskForSaveDirDialog: (Boolean) -> Unit,
    onProgress: (String) -> Unit,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
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

    // After finish share apk file, clear the temp folder so the temp.apk files is deleted as well
    val shareApp = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        context.cacheDir.deleteRecursively()
    }

    // Shows if user has selected the filter option dialog
    var showFilter by rememberSaveable {
        mutableStateOf(false)
    }

    // Error details to display in a dialog if the last extraction failed.
    var extractionError: ExtractionResult.ExtractionError.Failure? by remember {
        mutableStateOf(null)
    }

    // Automatically update the ActionMode selection count when appList changes.
    // Ignore the initial emission on composition.
    LaunchedEffect(model, onAppSelection) {
        model.mainFragmentState.map { state ->
            val selectedCount = state.appList.count { it.isChecked }
            val allSelected = state.appList.isNotEmpty() && selectedCount == state.appList.size

            allSelected to selectedCount
        }.distinctUntilChanged().drop(1).collect { (allSelected, selectedCount) ->
            onAppSelection(allSelected, selectedCount)
        }
    }

    LaunchedEffect(key1 = Unit) {
        model.onProgressRequest.collect { taskId ->
            onProgress(taskId)
        }
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
        }
    }

    LaunchedEffect(key1 = Unit) {
        /**
         * Handles Information about apk save process result
         */
        model.extractionResult.collect { extractionResult ->
            when (extractionResult) {
                ExtractionResult.ExtractionError.None -> Toast.makeText(
                    context, R.string.toast_save_apps, Toast.LENGTH_SHORT
                ).show()

                is ExtractionResult.ExtractionSuccess.SuccessSingle, is ExtractionResult.ExtractionSuccess.SuccessMultiple -> {
                    val backupsCount =
                        if (extractionResult is ExtractionResult.ExtractionSuccess.SuccessMultiple) extractionResult.backupsCount else 1
                    showSnackbar(
                        MySnackbarVisuals(
                            duration = SnackbarDuration.Long, message = resources.getQuantityString(
                                R.plurals.snackbar_successful_extracted_multiple,
                                backupsCount,
                                extractionResult.data!!.appName,
                                backupsCount - 1
                            )
                        )
                    )
                }

                is ExtractionResult.ExtractionError.Failure -> extractionError = extractionResult
                is ExtractionResult.ExtractionError.NoSaveDir -> showAskForSaveDirDialog(true)
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        /**
         * Creates Intent for Apps to Share
         */
        model.shareResult.collect { shareResult ->
            if (shareResult == ShareResult.ShareError.None) {
                Toast.makeText(
                    context, R.string.toast_share_app, Toast.LENGTH_SHORT
                ).show()
                return@collect
            } else if (shareResult is ShareResult.ShareSuccess) {
                val action =
                    if (shareResult is ShareResult.ShareSuccess.SuccessSingle) Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE
                Intent(action).apply {
                    type = FileUtil.FileInfo.APK.mimeType
                    when (shareResult) {
                        is ShareResult.ShareSuccess.SuccessMultiple -> {
                            clipData = ClipData.newRawUri(null, shareResult.uris[0]).apply {
                                shareResult.uris.drop(1).forEach { addItem(ClipData.Item(it)) }
                            }
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareResult.uris)
                        }

                        is ShareResult.ShareSuccess.SuccessSingle -> putExtra(
                            Intent.EXTRA_STREAM, shareResult.uri
                        )
                    }
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }.let {
                    Intent.createChooser(it, resources.getString(R.string.share_intent_title))
                }?.also {
                    shareApp.launch(it)
                }
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        // all actions inside Appbar or Bottombar the user can trigger in this screen
        Route.AppList.buttons.onEach { button ->
            when (button) {
                ScreenConfig.ScreenActions.FilterList -> showFilter = true
                ScreenConfig.ScreenActions.Refresh -> model.updateApps()
                ScreenConfig.ScreenActions.Save -> model.saveSelectedApps()
                ScreenConfig.ScreenActions.Share -> model.createShareUrisForSelectedApps()

                else -> Unit
            }
        }.launchIn(this)
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
        sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
        ),
        changeSelection = model::changeSelection,
        setSortOrder = model::setSortAsc,
        sortApps = model::setSortOrder,
        sortFavorites = model::setSortFavorites,
        setInstallationSource = model::setInstallationSource,
        setCategory = model::setCategory,
        setFilterOthers = model::setOtherFilter
    )

    extractionError?.let { (errorMessage, throwable, app) ->
        ExtractionResultDialog(
            onDismissRequest = { extractionError = null },
            appName = app.appName,
            errorMessage = errorMessage ?: throwable?.message
        )
    }

    val haptic = LocalHapticFeedback.current
    AppListContent(
        appList = state.appList,
        searchString = searchString,
        isSwipeToDismiss = !isActionMode,
        updateApp = { app ->
            if (isActionMode) model.updateApp(app.copy(isChecked = !app.isChecked))
            else appOetailsScreen(app.appPackageName)
        },
        triggerActionMode = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            model.updateApp(it.copy(isChecked = true))
        },
        isRefreshing = state.isRefreshing,
        isPullToRefresh = !isActionMode,
        onRefresh = model::updateApps,
        rightSwipeAction = rightSwipeAction,
        leftSwipeAction = leftSwipeAction,
        swipeActionCallback = { action, app ->
            model.appActionIntent(
                action.intent(
                    app, ApkActionIntentParams(showSnackbar = showSnackbar)
                )
            )
        },
        isSwipeActionCustomThreshold = swipeActionCustomThreshold,
        swipeActionThresholdModifier = swipeActionThresholdMod,
        uninstalledAppFound = model::removeApp
    )
}
