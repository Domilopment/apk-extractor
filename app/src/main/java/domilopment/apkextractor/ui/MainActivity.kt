package domilopment.apkextractor.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.R
import domilopment.apkextractor.Screen
import domilopment.apkextractor.autoBackup.AutoBackupService
import domilopment.apkextractor.data.rememberAppBarState
import domilopment.apkextractor.ui.actionBar.APKExtractorAppBar
import domilopment.apkextractor.ui.navigation.APKExtractorBottomNavigation
import domilopment.apkextractor.ui.navigation.ApkExtractorNavHost
import domilopment.apkextractor.ui.theme.APKExtractorTheme
import domilopment.apkextractor.ui.viewModels.MainViewModel
import domilopment.apkextractor.utils.Constants
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.MySnackbarVisuals
import domilopment.apkextractor.utils.settings.SettingsManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var settingsManager: SettingsManager
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var snackbarHostState: SnackbarHostState

    // while waiting for chooseSaveDir to deliver and apply Result,
    // onStart will be called and Ask for Save Dir even save dir is chosen but not applied.
    private var waitForRes = false

    private val chooseSaveDir =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
            it?.also { saveDirUri ->
                takeUriPermission(saveDirUri)
                waitForRes = false
            } ?: run {
                waitForRes = false
                showDialog()
            }
        }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            when (result.resultCode) {
                RESULT_CANCELED -> popupDialogForNotifyAboutUpdate()
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> popupDialogUpdateFailed()
            }
        }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // After the update is downloaded, show a notification
            // and request user confirmation to restart the app.
            popupSnackbarForCompleteUpdate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            val model = viewModel<MainViewModel>()
            val mainScreenState = model.mainScreenState
            val actionModeState = model.actionModeState
            val navController = rememberNavController()
            val appBarState = rememberAppBarState(navController = navController)
            val scope = rememberCoroutineScope()

            APKExtractorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(topBar = {
                        APKExtractorAppBar(
                            appBarState = appBarState,
                            modifier = Modifier.fillMaxWidth(),
                            isSearchActive = mainScreenState.isAppBarSearchActive,
                            searchText = mainScreenState.appBarSearchText,
                            isActionModeActive = mainScreenState.isActionModeActive,
                            isAllItemsChecked = actionModeState.selectAllItemsCheck,
                            onSearchQueryChanged = { model.updateSearchQuery(it) },
                            onTriggerSearch = { model.setSearchBarState(it) },
                            onEndActionMode = {
                                model.setActionModeState(false)
                                model.resetActionMode()
                            },
                            onCheckAllItems = { model.updateActionMode(selectAllItems = it) },
                            selectedApplicationsCount = actionModeState.selectedItemCount
                        )
                    }, bottomBar = {
                        APKExtractorBottomNavigation(items = listOf(
                            Screen.AppList, Screen.ApkList
                        ),
                            navController = navController,
                            appBarState = appBarState,
                            isActionMode = mainScreenState.isActionModeActive,
                            onNavigate = {
                                model.resetAppBarState()
                            })
                    }, snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState, snackbar = { snackbarData ->
                            val visuals = snackbarData.visuals as MySnackbarVisuals
                            Snackbar(
                                snackbarData = snackbarData,
                                contentColor = visuals.messageColor ?: SnackbarDefaults.contentColor
                            )
                        })
                    }) { contentPadding ->
                        ApkExtractorNavHost(modifier = Modifier.padding(contentPadding),
                            navController = navController,
                            showSnackbar = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(it)
                                }
                            },
                            searchQuery = mainScreenState.appBarSearchText,
                            isActionMode = mainScreenState.isActionModeActive,
                            isActionModeAllItemsSelected = actionModeState.selectAllItemsCheck,
                            onTriggerActionMode = {
                                model.setActionModeState(true)
                                model.updateActionMode(selectedItems = 1)
                            },
                            onAppSelection = { isAllSelected, appCount ->
                                model.updateActionMode(isAllSelected, appCount)
                            })
                    }
                }
            }
        }

        settingsManager = SettingsManager(this)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)

        snackbarHostState = SnackbarHostState()

        appUpdateManager.registerListener(installStateUpdatedListener)

        if (sharedPreferences.getBoolean(
                Constants.PREFERENCE_CHECK_UPDATE_ON_START, true
            )
        ) checkForAppUpdates()
    }

    override fun onStart() {
        super.onStart()
        // Check if Save dir is Selected, Writing permission to dir and whether dir exists
        // if not ask for select dir
        showDialog()

        // Checks if Service isn't running but should be
        if (settingsManager.shouldStartService()) startForegroundService(
            Intent(
                this, AutoBackupService::class.java
            )
        )
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

    override fun onNewIntent(intent: Intent?) {
        if (intent?.action == PACKAGE_INSTALLATION_ACTION) {
            when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    val activityIntent =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                        } else {
                            intent.getParcelableExtra(Intent.EXTRA_INTENT)
                        }
                    startActivity(activityIntent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }

                PackageInstaller.STATUS_SUCCESS -> MaterialAlertDialogBuilder(this).apply {
                    val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
                    setMessage(
                        getString(
                            R.string.installation_result_dialog_success_message, packageName
                        )
                    )
                    setTitle(R.string.installation_result_dialog_success_title)
                    setPositiveButton(R.string.installation_result_dialog_ok) { alert, _ ->
                        alert.dismiss()
                    }
                }.show()

                PackageInstaller.STATUS_FAILURE, PackageInstaller.STATUS_FAILURE_ABORTED, PackageInstaller.STATUS_FAILURE_BLOCKED, PackageInstaller.STATUS_FAILURE_CONFLICT, PackageInstaller.STATUS_FAILURE_INCOMPATIBLE, PackageInstaller.STATUS_FAILURE_INVALID, PackageInstaller.STATUS_FAILURE_STORAGE -> MaterialAlertDialogBuilder(
                    this
                ).apply {
                    val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
                    setMessage(
                        getString(
                            R.string.installation_result_dialog_failed_message,
                            packageName,
                            intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

                        )
                    )
                    setTitle(R.string.installation_result_dialog_failed_title)
                    setPositiveButton(R.string.installation_result_dialog_ok) { alert, _ ->
                        alert.dismiss()
                    }
                }.show()
            }
        } else super.onNewIntent(intent)
    }

    /**
     * Show dialog that prompts the user to select a save dir, for the app to be able save APKs
     */
    private fun showDialog() {
        if (!waitForRes && mustAskForSaveDir()) {
            MaterialAlertDialogBuilder(this).apply {
                setMessage(R.string.alert_save_path_message)
                setTitle(R.string.alert_save_path_title)
                setCancelable(false)
                setPositiveButton(R.string.alert_save_path_ok) { _, _ ->
                    waitForRes = true
                    chooseSaveDir.launch(null)
                }
            }.show()
        }
    }

    /**
     * Checks for picked Save Directory and for Access to this Dir
     * @return Have to ask user for Save Dir
     */
    private fun mustAskForSaveDir(): Boolean {
        if (!sharedPreferences.contains(Constants.PREFERENCE_KEY_SAVE_DIR)) return true

        val path = SettingsManager(this).saveDir()
        return path == null || checkUriPermission(
            path,
            Binder.getCallingPid(),
            Binder.getCallingUid(),
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        ) == PackageManager.PERMISSION_DENIED || !FileUtil(this).doesDocumentExist(path)
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
                else -> {
                    // Nothing to do
                }
            }
        }
    }

    private fun popupDialogForNotifyAboutUpdate() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(R.string.popup_dialog_for_notify_about_update_text)
            setTitle(R.string.popup_dialog_for_notify_about_update_title)
            setPositiveButton(R.string.popup_dialog_for_notify_about_update_button_positive) { _, _ ->
                checkForAppUpdates()
            }
            setNegativeButton(R.string.popup_dialog_for_notify_about_update_button_negative) { dialog, _ ->
                dialog.dismiss()
            }
            setNeutralButton(R.string.popup_dialog_for_notify_about_update_button_neutral) { dialog, _ ->
                sharedPreferences.edit()
                    .putBoolean(Constants.PREFERENCE_CHECK_UPDATE_ON_START, false).apply()
                dialog.dismiss()
            }
        }.show()
    }

    private fun popupDialogUpdateFailed(message: String? = "No Error message provided") {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(getString(R.string.popup_dialog_update_failed_text, message))
            setTitle(R.string.popup_dialog_update_failed_title)
            setPositiveButton(R.string.popup_dialog_update_failed_button_positive) { _, _ ->
                checkForAppUpdates()
            }
            setNegativeButton(R.string.popup_dialog_update_failed_button_negative) { dialog, _ ->
                dialog.dismiss()
            }
        }.show()
    }

    /**
     * Take Uri Permission for Save Dir
     * @param uri content uri for selected save path
     */
    private fun takeUriPermission(uri: Uri) {
        val takeFlags: Int =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        settingsManager.saveDir()?.also { oldPath ->
            if (oldPath in contentResolver.persistedUriPermissions.map { it.uri }) contentResolver.releasePersistableUriPermission(
                oldPath, takeFlags
            )
        }
        sharedPreferences.edit().putString(Constants.PREFERENCE_KEY_SAVE_DIR, uri.toString())
            .apply()
        contentResolver.takePersistableUriPermission(uri, takeFlags)
    }

    companion object {
        const val PACKAGE_INSTALLATION_ACTION =
            "${BuildConfig.APPLICATION_ID}.apis.content.SESSION_API_PACKAGE_INSTALLATION"
    }
}