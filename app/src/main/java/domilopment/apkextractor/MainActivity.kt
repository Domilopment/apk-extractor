package domilopment.apkextractor

import android.Manifest
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
import androidx.core.app.ActivityCompat
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

        // Check for Permissions to READ/WRITE external Storage for Lower android Versions
        checkNeededPermissions()

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
    }

    override fun onStart() {
        super.onStart()
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
     * Checks if all Permissions in Array are granted
     * @return Boolean
     * True after check
     */
    private fun checkNeededPermissions(): Boolean {
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ).filter {
            ActivityCompat.checkSelfPermission(
                applicationContext,
                it
            ) != PackageManager.PERMISSION_GRANTED
        }.also {
            if (it.isNotEmpty())
                ActivityCompat.requestPermissions(this, it.toTypedArray(), 0)
        }
        return true
    }

    /**
     * Checks if all Permissions in an IntArray are granted
     * @param grantedPermissions
     * Array of Permissions
     * @return Boolean
     * True if all Permissions Granted, else False
     */
    private fun allPermissionsGranted(grantedPermissions: IntArray): Boolean {
        return grantedPermissions.all { singleGrantedPermission ->
            singleGrantedPermission == PackageManager.PERMISSION_GRANTED
        }
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
     * Checks if All Permissions Granted on Runtime
     * @param requestCode
     * @param permissions
     * All Permissions the App needs
     * @param grantResults
     * Array of grant values from permissions
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!allPermissionsGranted(grantResults)) {
            ActivityCompat.requestPermissions(this, permissions, 0)
        }
    }

    /**
     * Executes on Intent results
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FileHelper.CHOOSE_SAVE_DIR_RESULT -> {
                if (resultCode == Activity.RESULT_OK) {
                    sharedPreferences.edit()
                        .putString("dir", data!!.data.toString()).apply()
                    (data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)).run {
                        contentResolver
                            .takePersistableUriPermission(data.data!!, this)
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