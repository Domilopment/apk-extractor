package domilopment.apkextractor.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.R
import domilopment.apkextractor.autoBackup.AutoBackupService
import domilopment.apkextractor.databinding.ActivityMainBinding
import domilopment.apkextractor.utils.Constants
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.settings.SettingsManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var settingsManager: SettingsManager

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        settingsManager = SettingsManager(this)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
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

    /**
     * Executes on Application Destroy, clear cache
     */
    override fun onDestroy() {
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
        sharedPreferences.edit().putString(Constants.PREFERENCE_KEY_SAVE_DIR, uri.toString()).apply()
        contentResolver.takePersistableUriPermission(uri, takeFlags)
    }

    companion object {
        const val PACKAGE_INSTALLATION_ACTION =
            "${BuildConfig.APPLICATION_ID}.apis.content.SESSION_API_PACKAGE_INSTALLATION"
    }
}