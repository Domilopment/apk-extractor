package domilopment.apkextractor

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.documentfile.provider.DocumentFile
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var path: String
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var settingsManager: SettingsManager

    companion object {
        const val SHARE_APP_RESULT = 666
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        settingsManager = SettingsManager(this)
        // Set UI Mode
        settingsManager.changeUIMode()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        checkNeededPermissions()

        path = SettingsManager(this).saveDir()

        // Check if Save dir is Selected, Writing permission to dir and whether dir exists
        // if not ask for select dir
        if (!(sharedPreferences.contains("dir"))
            || checkUriPermission(
                Uri.parse(path),
                Binder.getCallingPid(),
                Binder.getCallingUid(),
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            ) == PackageManager.PERMISSION_DENIED
            || !DocumentFile.fromTreeUri(
                this,
                Uri.parse(path)
            )!!.exists()
        ) {
            AlertDialog.Builder(this).let {
                it.setMessage(R.string.alert_save_path_message)
                it.setTitle(R.string.alert_save_path_title)
                it.setCancelable(false)
                it.setPositiveButton(R.string.alert_save_path_ok) { _, _ ->
                    FileHelper(this).chooseDir()
                }
            }.create().show()
        }
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
    private fun checkNeededPermissions() : Boolean{
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ).filter {
            ActivityCompat.checkSelfPermission(applicationContext, it) != PackageManager.PERMISSION_GRANTED
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
        grantedPermissions.forEach { singleGrantedPermission ->
            if (singleGrantedPermission == PackageManager.PERMISSION_DENIED)
                return false
        }
        return true
    }

    /**
     * Checks if All Permissions Granted on Runtime
     * @param requestCode
     * @param permissions
     * All Permissions the App needs
     * @param grantResults
     * Array of grant values from permissions
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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
                sharedPreferences.edit()
                    .putString("dir", data!!.data.toString()).apply()
                (data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)).run {
                    contentResolver
                        .takePersistableUriPermission(data.data!!, this)
                }
            }
            SHARE_APP_RESULT -> {
                cacheDir.deleteRecursively()
            }
        }
    }
}