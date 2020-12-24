package domilopment.apkextractor

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import domilopment.apkextractor.autoBackup.AutoBackupService
import domilopment.apkextractor.databinding.ActivityMainBinding
import domilopment.apkextractor.utils.FileHelper
import domilopment.apkextractor.utils.SettingsManager
import kotlin.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var settingsManager: SettingsManager

    companion object {
        const val SHARE_APP_RESULT = 666
        const val SELECTED_APK_RESULT = 7553
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        settingsManager = SettingsManager(this)
        // Set UI Mode
        settingsManager.changeUIMode()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onStart() {
        super.onStart()
        // Check if Save dir is Selected, Writing permission to dir and whether dir exists
        // if not ask for select dir
        if (mustAskForSaveDir()) {
            AlertDialog.Builder(this).apply {
                setMessage(R.string.alert_save_path_message)
                setTitle(R.string.alert_save_path_title)
                setCancelable(false)
                setPositiveButton(R.string.alert_save_path_ok) { _, _ ->
                    FileHelper(this@MainActivity).chooseDir(this@MainActivity)
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
        return !(sharedPreferences.contains("dir"))
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
        )
    }

    /**
     * Executes on Intent results
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            FileHelper.CHOOSE_SAVE_DIR_RESULT -> {
                if (resultCode == Activity.RESULT_OK) {
                    (data!!.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)).run {
                        settingsManager.saveDir()?.also { oldPath ->
                            contentResolver.releasePersistableUriPermission(oldPath, this)
                        }
                        sharedPreferences.edit().putString("dir", data.data.toString()).apply()
                        contentResolver.takePersistableUriPermission(data.data!!, this)
                    }
                } else if (mustAskForSaveDir()) {
                    FileHelper(this).chooseDir(this)
                }
            }
            SHARE_APP_RESULT -> {
                cacheDir.deleteRecursively()
            }
            SELECTED_APK_RESULT -> {
                if (resultCode == Activity.RESULT_OK)
                    AlertDialog.Builder(this).apply {
                        setTitle(getString(R.string.alert_apk_selected_title))
                        setItems(R.array.selected_apk_options) { _, i: Int ->
                            try {
                                data?.data?.let { apkFileOptions(i, it) }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Something went wrong, couldn't perform action on Selected Apk",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.e("Apk Extractor: Saved Apps Dialog", e.toString())
                            }
                        }
                    }.show()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Executes Option for "How to Use Apk" Dialog
     * @param i Selected Option
     * @param data Result Intent, holding Apk files data
     */
    private fun apkFileOptions(i: Int, data: Uri) {
        when (i) {
            // Send Selected Apk File
            0 -> startActivity(
                Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = FileHelper.MIME_TYPE
                    putExtra(Intent.EXTRA_STREAM, data)
                }, getString(R.string.share_intent_title))
            )
            // Install Selected Apk File
            1 -> startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(
                        data,
                        FileHelper.MIME_TYPE
                    )
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                })
            // Delete Selected Apk File
            2 -> DocumentsContract.deleteDocument(
                contentResolver,
                data
            )
        }
    }
}