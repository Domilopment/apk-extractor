package domilopment.apkextractor.utils

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import domilopment.apkextractor.autoBackup.AutoBackupService
import domilopment.apkextractor.data.ApplicationModel
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

    fun getApps(): Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>> {
        val apks = ListOfAPKs(packageManager)
        return apks.apps
    }

    /**
     * Creates a List containing of all Types the User Selected in Settings
     * @return List of Selected App Types
     */
    fun selectedAppTypes(
        applications: Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>,
        selectUpdatedSystemApps: Boolean = sharedPreferences.getBoolean(
            "updated_system_apps",
            false
        ),
        selectSystemApps: Boolean = sharedPreferences.getBoolean("system_apps", false),
        selectUserApps: Boolean = sharedPreferences.getBoolean("user_apps", true),
        sortApps: Boolean = true,
        sortMode: Int = sharedPreferences.getInt("app_sort", SORT_BY_NAME)
    ): List<ApplicationModel> {
        val (updatedSystemApps, systemApps, userApps) = applications
        val mData: MutableList<ApplicationModel> = mutableListOf()
        if (selectUpdatedSystemApps) {
            mData.addAll(updatedSystemApps)
            if (selectSystemApps) mData.addAll(systemApps)
        }
        if (selectUserApps) mData.addAll(userApps)
        return if (sortApps) sortData(mData, sortMode) else mData
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
        val names = mapOf(
            "name" to app.appName,
            "package" to app.appPackageName,
            "version_name" to app.appVersionCode,
            "version_number" to "v${app.appVersionName}",
            "datetime" to SimpleDateFormat.getDateTimeInstance().format(Date())
        )
        return StringBuilder().apply {
            val prefs =
                sharedPreferences.getStringSet("app_save_name", setOf("0:name"))
            val processedPrefs = try {
                prefs?.toSortedSet(compareBy<String> { it[0].digitToInt() })
                    ?.map { it.removeRange(0, 2) }
            } catch (e: Exception) {
                prefs
            }
            processedPrefs.also {
                if (it.isNullOrEmpty()) append(app.appName)
                else it.forEach { v ->
                    append(" ${names[v]}")
                }
            }
            append(FileHelper.PREFIX)
        }.removePrefix(" ").toString()
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

    /**
     * Set App Locale Language for Tag and apply
     * @param locale String Tag of Locale
     */
    fun setLocale(locale: String) {
        val appLocale: LocaleListCompat =
            if (locale == "default") LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(locale)
        // Call this on the main thread as it may require Activity.restart()
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}