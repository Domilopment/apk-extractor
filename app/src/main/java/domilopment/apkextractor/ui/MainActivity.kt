package domilopment.apkextractor.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import domilopment.apkextractor.R
import domilopment.apkextractor.autoBackup.AutoBackupService
import domilopment.apkextractor.databinding.ActivityMainBinding
import domilopment.apkextractor.utils.FileHelper
import domilopment.apkextractor.utils.SettingsManager
import java.io.FileNotFoundException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var settingsManager: SettingsManager

    // while waiting for chooseSaveDir to deliver and apply Result,
    // onStart will be called and Ask for Save Dir even save dir is chosen but not applied.
    private var waitForRes = false

    private val chooseSaveDir =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.also { saveDirUri -> takeUriPermission(saveDirUri) }
                waitForRes = false
            } else if (mustAskForSaveDir()) {
                chooseSaveDir()
            }
        }

    private fun chooseSaveDir() {
        waitForRes = true
        FileHelper(this@MainActivity).chooseDir(chooseSaveDir)
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
        if (!waitForRes && mustAskForSaveDir()) {
            MaterialAlertDialogBuilder(this).apply {
                setMessage(R.string.alert_save_path_message)
                setTitle(R.string.alert_save_path_title)
                setCancelable(false)
                setPositiveButton(R.string.alert_save_path_ok) { _, _ ->
                    chooseSaveDir()
                }
            }.show()
        }

        // Checks if Service isn't running but should be
        if (settingsManager.shouldStartService())
            startService(Intent(this, AutoBackupService::class.java))
    }
    
    /**
     * Executes on Application Destroy, clear cache
     */
    override fun onDestroy() {
        cacheDir.deleteRecursively()
        super.onDestroy()
    }

    /**
     * Checks for picked Save Directory and for Access to this Dir
     * @return Have to ask user for Save Dir
     */
    private fun mustAskForSaveDir(): Boolean {
        val path = SettingsManager(this).saveDir()
        return (!sharedPreferences.contains("dir"))
                || checkUriPermission(
            path,
            Binder.getCallingPid(),
            Binder.getCallingUid(),
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        ) == PackageManager.PERMISSION_DENIED
                || !DocumentsContract.isDocumentUri(
            this,
            DocumentsContract.buildDocumentUriUsingTree(
                path, DocumentsContract.getTreeDocumentId(path)
            )
        ) || try {
            DocumentsContract.findDocumentPath(
                contentResolver,
                DocumentsContract.buildDocumentUriUsingTree(
                    path, DocumentsContract.getTreeDocumentId(path)
                )
            ) == null
        } catch (e: FileNotFoundException) {
            true
        } catch (e: NullPointerException) {
            true
        }
    }

    /**
     * Take Uri Permission for Save Dir
     * @param data return Intent from choose Save Dir
     */
    private fun takeUriPermission(data: Intent) {
        val takeFlags: Int =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        settingsManager.saveDir()?.also { oldPath ->
            if (oldPath in contentResolver.persistedUriPermissions.map { it.uri })
                contentResolver.releasePersistableUriPermission(oldPath, takeFlags)
        }
        sharedPreferences.edit().putString("dir", data.data.toString()).apply()
        contentResolver.takePersistableUriPermission(data.data!!, takeFlags)
    }
}