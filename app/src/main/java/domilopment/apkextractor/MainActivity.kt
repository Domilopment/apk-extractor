package domilopment.apkextractor

import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.content.IntentSanitizer
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import domilopment.apkextractor.autoBackup.AutoBackupService
import domilopment.apkextractor.data.ApkInstallationResult
import domilopment.apkextractor.data.ApkInstallationResultType
import domilopment.apkextractor.data.InAppUpdateResult
import domilopment.apkextractor.data.InAppUpdateResultType
import domilopment.apkextractor.data.UiMode
import domilopment.apkextractor.data.rememberAppBarState
import domilopment.apkextractor.ui.Screen
import domilopment.apkextractor.ui.actionBar.APKExtractorAppBar
import domilopment.apkextractor.ui.dialogs.AskForSaveDirDialog
import domilopment.apkextractor.ui.dialogs.InAppUpdateDialog
import domilopment.apkextractor.ui.dialogs.InstallationResultDialog
import domilopment.apkextractor.ui.navigation.APKExtractorBottomNavigation
import domilopment.apkextractor.ui.navigation.ApkExtractorNavHost
import domilopment.apkextractor.ui.theme.APKExtractorTheme
import domilopment.apkextractor.ui.viewModels.MainViewModel
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.MySnackbarVisuals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var snackbarHostState: SnackbarHostState

    private val model by viewModels<MainViewModel>()

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // After the update is downloaded, show a notification
            // and request user confirmation to restart the app.
            popupSnackbarForCompleteUpdate()
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.apply {
            var keepOnScreen = true
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    model.keepSplashScreen.collect {
                        keepOnScreen = it
                    }
                }
            }
            setKeepOnScreenCondition { keepOnScreen }
        }
        setContent {
            val mainScreenState = model.mainScreenState
            val actionModeState = model.actionModeState
            val saveDir by model.saveDir.collectAsState()
            val dynamicColors by model.materialYou.collectAsState()
            val navController = rememberNavController()
            val appBarState = rememberAppBarState(navController = navController)
            val scope = rememberCoroutineScope()

            var showAskForSaveDir by remember {
                mutableStateOf(false)
            }

            val isActionModeActive = remember(key1 = mainScreenState.uiMode) {
                mainScreenState.uiMode is UiMode.Action
            }

            var installationResult: ApkInstallationResult? by remember {
                mutableStateOf(null)
            }

            var inAppUpdateResult: InAppUpdateResult? by remember {
                mutableStateOf(null)
            }

            val chooseSaveDir =
                rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
                    it?.also { saveDirUri ->
                        takeUriPermission(saveDir, saveDirUri, model::setSaveDir)
                    }
                }

            activityResultLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                    when (result.resultCode) {
                        RESULT_CANCELED -> inAppUpdateResult =
                            InAppUpdateResult(InAppUpdateResultType.CANCELED)

                        ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> inAppUpdateResult =
                            InAppUpdateResult(InAppUpdateResultType.UPDATE_FAILED)
                    }
                }

            DisposableEffect(key1 = lifecycle, key2 = saveDir) {
                // Create an observer that triggers our remembered callbacks
                // for sending analytics events
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_START -> showAskForSaveDir = mustAskForSaveDir(saveDir)

                        else -> Unit
                    }
                }

                // Add the observer to the lifecycle
                lifecycle.addObserver(observer)

                // When the effect leaves the Composition, remove the observer
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }

            DisposableEffect(Unit) {
                val listener = Consumer<Intent> { intent ->
                    onNewIntent(intent) { resultType ->
                        installationResult = ApkInstallationResult(resultType)
                    }
                }
                addOnNewIntentListener(listener)
                onDispose {
                    removeOnNewIntentListener(listener)
                }
            }

            LaunchedEffect(key1 = Unit) {
                model.hideSplashScreen()
            }

            APKExtractorTheme(dynamicColor = dynamicColors) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { testTagsAsResourceId = true },
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(topBar = {
                        APKExtractorAppBar(
                            appBarState = appBarState,
                            modifier = Modifier.fillMaxWidth(),
                            uiMode = mainScreenState.uiMode,
                            searchText = mainScreenState.appBarSearchText,
                            isAllItemsChecked = actionModeState.selectAllItemsCheck,
                            onSearchQueryChanged = model::updateSearchQuery,
                            onTriggerSearch = model::setSearchBarState,
                            onReturnUiMode = model::onReturnUiMode,
                            onCheckAllItems = { model.updateActionMode(selectAllItems = it) },
                            selectedApplicationsCount = actionModeState.selectedItemCount
                        )
                    }, bottomBar = {
                        APKExtractorBottomNavigation(
                            items = listOf(
                                Screen.AppList, Screen.ApkList
                            ),
                            navController = navController,
                            appBarState = appBarState,
                            isActionMode = isActionModeActive,
                            onNavigate = model::resetAppBarState
                        )
                    }, snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState, snackbar = { snackbarData ->
                            val visuals = snackbarData.visuals as MySnackbarVisuals
                            Snackbar(
                                snackbarData = snackbarData,
                                contentColor = visuals.messageColor ?: SnackbarDefaults.contentColor
                            )
                        })
                    }) { contentPadding ->
                        val haptic = LocalHapticFeedback.current
                        ApkExtractorNavHost(
                            modifier = Modifier.padding(contentPadding),
                            navController = navController,
                            showSnackbar = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(it)
                                }
                            },
                            searchQuery = mainScreenState.appBarSearchText.trim(),
                            isActionMode = isActionModeActive,
                            isActionModeAllItemsSelected = actionModeState.selectAllItemsCheck,
                            onTriggerActionMode = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                model.updateActionMode(selectedItems = 1)
                                model.setActionModeState()
                            },
                            onAppSelection = { isAllSelected, appCount ->
                                model.updateActionMode(isAllSelected, appCount)
                            },
                            chooseSaveDir = chooseSaveDir,
                            appUpdateManager = appUpdateManager,
                            inAppUpdateResultLauncher = activityResultLauncher
                        )

                        installationResult?.let {
                            InstallationResultDialog(
                                onDismissRequest = { installationResult = null },
                                result = it.result,
                            )
                        }

                        inAppUpdateResult?.let {
                            InAppUpdateDialog(
                                onDismissRequest = { inAppUpdateResult = null },
                                confirmButtonOnClick = ::checkForAppUpdates,
                                inAppUpdateResultType = it.resultType
                            )
                        }

                        if (showAskForSaveDir) AskForSaveDirDialog(chooseSaveDir = chooseSaveDir)
                    }
                }
            }
        }

        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)

        snackbarHostState = SnackbarHostState()

        appUpdateManager.registerListener(installStateUpdatedListener)

        lifecycleScope.launch {
            if (model.updateOnStart.first()) checkForAppUpdates()
        }
    }

    override fun onStart() {
        super.onStart()
        // Checks if Service isn't running but should be
        lifecycleScope.launch {
            if (model.autoBackup.first()) startForegroundService(
                Intent(
                    this@MainActivity, AutoBackupService::class.java
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            // If the update is downloaded but not installed,
            // notify the user to complete the update.
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackbarForCompleteUpdate()
            }
        }
    }

    /**
     * Executes on Application Destroy, clear cache
     */
    override fun onDestroy() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
        cacheDir.deleteRecursively()
        super.onDestroy()
    }

    private fun onNewIntent(intent: Intent, result: (ApkInstallationResultType) -> Unit) {
        if (intent.action == PACKAGE_INSTALLATION_ACTION) {
            when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    var proceed = true
                    val activityIntent =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                        } else {
                            intent.getParcelableExtra(Intent.EXTRA_INTENT)
                        }?.let {
                            IntentSanitizer.Builder().allowAnyComponent()
                                .allowAction("android.content.pm.action.CONFIRM_INSTALL")
                                .allowPackage("com.google.android.packageinstaller").allowExtra(
                                    "android.content.pm.extra.SESSION_ID", Integer::class.java
                                ).build().sanitize(it) {
                                    proceed = false
                                }
                        }
                    if (proceed) startActivity(activityIntent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }

                PackageInstaller.STATUS_SUCCESS -> {
                    val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
                    result(ApkInstallationResultType.Success(packageName))
                }

                PackageInstaller.STATUS_FAILURE, PackageInstaller.STATUS_FAILURE_ABORTED, PackageInstaller.STATUS_FAILURE_BLOCKED, PackageInstaller.STATUS_FAILURE_CONFLICT, PackageInstaller.STATUS_FAILURE_INCOMPATIBLE, PackageInstaller.STATUS_FAILURE_INVALID, PackageInstaller.STATUS_FAILURE_STORAGE -> {
                    val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
                    val errorMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                        ?: "No Error message provided"
                    result(ApkInstallationResultType.Failure(packageName, errorMessage))
                }
            }
        }
    }


    /**
     * Checks for picked Save Directory and for Access to this Dir
     * @return Have to ask user for Save Dir
     */
    private fun mustAskForSaveDir(saveDir: Uri?): Boolean {
        return saveDir == null || checkUriPermission(
            saveDir,
            Binder.getCallingPid(),
            Binder.getCallingUid(),
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        ) == PackageManager.PERMISSION_DENIED || !FileUtil(this).doesDocumentExist(saveDir)
    }

    private fun checkForAppUpdates() {
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                // This example applies an immediate update. To apply a flexible update
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // an activity result launcher registered via registerForActivityResult
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            }
        }
    }

    // Displays the snackbar notification and call to action.
    private fun popupSnackbarForCompleteUpdate() {
        lifecycleScope.launch {
            val result = snackbarHostState.showSnackbar(
                MySnackbarVisuals(
                    actionLabel = getString(R.string.popup_snackbar_for_complete_update_action),
                    duration = SnackbarDuration.Indefinite,
                    message = getString(R.string.popup_snackbar_for_complete_update_text),
                )
            )
            when (result) {
                SnackbarResult.ActionPerformed -> appUpdateManager.completeUpdate()
                else -> Unit
            }
        }
    }

    /**
     * Take Uri Permission for Save Dir
     * @param newUri content uri for selected save path
     */
    private fun takeUriPermission(oldUri: Uri?, newUri: Uri, saveUri: (Uri) -> Unit) {
        val takeFlags: Int =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        oldUri?.let { oldPath ->
            if (oldPath in contentResolver.persistedUriPermissions.map { it.uri } && oldPath != newUri) contentResolver.releasePersistableUriPermission(
                oldPath, takeFlags
            )
        }
        saveUri(newUri)
        contentResolver.takePersistableUriPermission(newUri, takeFlags)
    }

    companion object {
        const val PACKAGE_INSTALLATION_ACTION =
            "${BuildConfig.APPLICATION_ID}.apis.content.SESSION_API_PACKAGE_INSTALLATION"
    }
}