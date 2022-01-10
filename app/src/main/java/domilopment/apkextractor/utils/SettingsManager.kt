package domilopment.apkextractor.utils

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import domilopment.apkextractor.autoBackup.AutoBackupService
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.data.ListOfAPKs
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.Throws

class SettingsManager(context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val packageManager = context.packageManager

    companion object {
        // Sort types for App List
        const val SORT_BY_NAME = 0
        const val SORT_BY_PACKAGE = 1
        const val SORT_BY_INSTALL_TIME = 2
        const val SORT_BY_UPDATE_TIME = 3
        const val SORT_BY_APK_SIZE = 5
    }

    /**
     * Creates a List containing of all Types the User Selected in Settings
     * @return List of Selected App Types
     */
    fun selectedAppTypes(): List<ApplicationModel> {
        val mData: MutableList<ApplicationModel> = mutableListOf()
        if (sharedPreferences.getBoolean("updated_system_apps", false)) {
            mData.addAll(ListOfAPKs(packageManager).updatedSystemApps)
            if (sharedPreferences.getBoolean("system_apps", false))
                mData.addAll(ListOfAPKs(packageManager).systemApps)
        }
        if (sharedPreferences.getBoolean("user_apps", true))
            mData.addAll(ListOfAPKs(packageManager).userApps)
        return sortData(mData)
    }


    /**
     * Gives back in SharedPreferences Saved Directory Path
     * @return Saved Directory Path
     */
    fun saveDir(): Uri? = sharedPreferences.getString("dir", null)?.let { Uri.parse(it) }


    /**
     * Sorts Data by user selected Order
     * @param data Unsorted List of APKs
     * @return Sorted List of APKs
     * @throws Exception if given sort type doesn't exist
     */
    @Throws(Exception::class)
    fun sortData(
        data: List<ApplicationModel>,
        sortMode: Int = sharedPreferences.getInt("app_sort", SORT_BY_NAME)
    ): List<ApplicationModel> {
        return when (sortMode) {
            SORT_BY_NAME ->
                data.sortedWith(
                    compareBy(String.CASE_INSENSITIVE_ORDER, ApplicationModel::appName)
                )
            SORT_BY_PACKAGE -> data.sortedWith(
                compareBy(
                    String.CASE_INSENSITIVE_ORDER,
                    ApplicationModel::appPackageName
                )
            )
            SORT_BY_INSTALL_TIME -> data.sortedWith(
                compareBy(ApplicationModel::appInstallTime).reversed()
            )
            SORT_BY_UPDATE_TIME -> data.sortedWith(
                compareBy(ApplicationModel::appUpdateTime).reversed()
            )
            SORT_BY_APK_SIZE -> data.sortedWith(compareBy(ApplicationModel::apkSize))
            else -> throw Exception("No such sort type")
        }
    }

    /**
     * Switch ui mode (System, Light, Dark) either with given Parameter or with saved Preference
     * @param newValue Int castable String value to switch ui mode
     */
    fun changeUIMode(
        newValue: String = sharedPreferences.getString(
            "list_preference_ui_mode",
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString()
        )!!
    ) {
        when (newValue.toInt()) {
            AppCompatDelegate.MODE_NIGHT_YES ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            AppCompatDelegate.MODE_NIGHT_NO ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    /**
     * Gets an app and Creates a Name from its Data
     * @param app the resource App
     * @return String of the name after the APK should be named
     */
    fun appName(app: ApplicationModel): String {
        val sb = StringBuilder()
        sharedPreferences.getStringSet("app_save_name", setOf())?.also { prefs ->
            if (prefs.contains("name")) sb.append(app.appName)
            if (prefs.contains("package")) sb.append(" ${app.appPackageName}")
            if (prefs.contains("version_name")) sb.append(" ${app.appVersionCode}")
            if (prefs.contains("version_number")) sb.append(" v${app.appVersionName}")
            if (prefs.contains("datetime")) sb.append(
                " ${
                    SimpleDateFormat.getDateTimeInstance().format(Date())
                }"
            )
        }
        if (sb.isEmpty()) sb.append(app.appName)
        return sb.append(FileHelper.PREFIX).toString()
    }

    /**
     * Get set of Packages that should be looked for to Auto Backup
     * @return A Set of Package Names
     */
    fun listOfAutoBackupApps(): Set<String>? {
        return sharedPreferences.getStringSet("app_list_auto_backup", setOf())
    }

    /**
     * Tells an Activity if AutoBackupService should be started
     * @return true if Service isn't running and should be started
     */
    fun shouldStartService(): Boolean {
        val pref = sharedPreferences.getBoolean("auto_backup", false)
        val service = AutoBackupService.isRunning
        return pref and !service
    }

    /**
     * Enable Material You if it's turned on and the User wishes to (Only after app Restart)
     * @param application Application reference to enable Dynamic Colors
     */
    fun useMaterialYou(application: Application) {
        val available = DynamicColors.isDynamicColorAvailable()
        val enabled = sharedPreferences.getBoolean("use_material_you", false)
        if (available && enabled) DynamicColors.applyToActivitiesIfAvailable(application)
    }
}