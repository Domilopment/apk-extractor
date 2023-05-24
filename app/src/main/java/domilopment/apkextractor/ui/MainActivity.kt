package domilopment.apkextractor.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import domilopment.apkextractor.R
import domilopment.apkextractor.autoBackup.AutoBackupService
import domilopment.apkextractor.databinding.ActivityMainBinding
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.SettingsManager

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
        if (!sharedPreferences.contains("dir")) return true

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
        sharedPreferences.edit().putString("dir", uri.toString()).apply()
        contentResolver.takePersistableUriPermission(uri, takeFlags)
    }
}