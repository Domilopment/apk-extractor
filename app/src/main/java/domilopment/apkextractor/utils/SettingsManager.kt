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
import domilopment.apkextractor.utils.apkActions.ApkActionsOptions
import java.text.SimpleDateFormat
import java.util.*

class SettingsManager(context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val packageManager = context.packageManager

    companion object {
        // Sort types for App List
        const val SORT_BY_NAME = 0
        const val SORT_BY_PACKAGE = 1
        const val SORT_BY_INSTALL_TIME = 2
        const val SORT_BY_UPDATE_TIME = 3
        const val SORT_BY_APK_SIZE = 4
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
            "updated_system_apps", false
        ),
        selectSystemApps: Boolean = sharedPreferences.getBoolean("system_apps", false),
        selectUserApps: Boolean = sharedPreferences.getBoolean("user_apps", true),
        sortApps: Boolean = true,
        sortMode: Int = sharedPreferences.getInt("app_sort", SORT_BY_NAME),
        sortFavorites: Boolean = sharedPreferences.getBoolean("sort_favorites", true)
    ): List<ApplicationModel> {
        val (updatedSystemApps, systemApps, userApps) = applications
        val mData: MutableList<ApplicationModel> = mutableListOf()
        if (selectUpdatedSystemApps) {
            mData.addAll(updatedSystemApps)
            if (selectSystemApps) mData.addAll(systemApps)
        }
        if (selectUserApps) mData.addAll(userApps)
        mData.forEach {
            it.isFavorite =
                it.appPackageName in sharedPreferences.getStringSet("favorites", setOf())!!
        }
        return if (sortApps) sortData(mData, sortMode, sortFavorites) else mData
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
    fun sortData(
        data: List<ApplicationModel>,
        sortMode: Int = sharedPreferences.getInt("app_sort", SORT_BY_NAME),
        sortFavorites: Boolean = sharedPreferences.getBoolean("sort_favorites", true)
    ): List<ApplicationModel> {
        val comparator = if (sharedPreferences.getBoolean("app_sort_asc", true)) when (sortMode) {
            SORT_BY_NAME -> compareBy(String.CASE_INSENSITIVE_ORDER, ApplicationModel::appName)
            SORT_BY_PACKAGE -> compareBy(
                String.CASE_INSENSITIVE_ORDER, ApplicationModel::appPackageName
            )
            SORT_BY_INSTALL_TIME -> compareBy(ApplicationModel::appInstallTime)
            SORT_BY_UPDATE_TIME -> compareBy(ApplicationModel::appUpdateTime)
            SORT_BY_APK_SIZE -> compareBy(ApplicationModel::apkSize)
            else -> {
                sharedPreferences.edit().remove("app_sort").apply()
                compareBy(String.CASE_INSENSITIVE_ORDER, ApplicationModel::appName)
            }
        } else when (sortMode) {
            SORT_BY_NAME -> compareByDescending(
                String.CASE_INSENSITIVE_ORDER, ApplicationModel::appName
            )
            SORT_BY_PACKAGE -> compareByDescending(
                String.CASE_INSENSITIVE_ORDER, ApplicationModel::appPackageName
            )
            SORT_BY_INSTALL_TIME -> compareByDescending(ApplicationModel::appInstallTime)
            SORT_BY_UPDATE_TIME -> compareByDescending(ApplicationModel::appUpdateTime)
            SORT_BY_APK_SIZE -> compareByDescending(ApplicationModel::apkSize)
            else -> {
                sharedPreferences.edit().remove("app_sort").apply()
                compareByDescending(String.CASE_INSENSITIVE_ORDER, ApplicationModel::appName)
            }
        }
        val sortedList = data.sortedWith(comparator)
        return if (sortFavorites) sortFavorites(sortedList) else sortedList
    }

    /**
     * Sorts Favorites to top of the app list
     * @param data List of APKs
     */
    private fun sortFavorites(data: List<ApplicationModel>): List<ApplicationModel> {
        return data.sortedBy { app ->
            app.appPackageName !in sharedPreferences.getStringSet("favorites", setOf())!!
        }
    }

    /**
     * Filter Apps out of List
     * @param data List of Apps
     * @param filter Filter options for list, is favorite, installed from- google play, galaxy store, amazon store
     * @return filtered list of Applications
     */
    fun filterApps(
        data: List<ApplicationModel>, filter: Int = sharedPreferences.getInt("filter", 0)
    ): List<ApplicationModel> {
        var dataFiltered = data
        if (filter and AppFilterOptions.FAVORITES.getByte() == AppFilterOptions.FAVORITES.getByte()) dataFiltered =
            dataFiltered.filter { it.isFavorite }
        if (filter and AppFilterOptions.GOOGLE.getByte() == AppFilterOptions.GOOGLE.getByte()) dataFiltered =
            dataFiltered.filter { it.installationSource == "com.android.vending" }
        if (filter and AppFilterOptions.SAMSUNG.getByte() == AppFilterOptions.SAMSUNG.getByte()) dataFiltered =
            dataFiltered.filter { it.installationSource == "com.sec.android.app.samsungapps" }
        if (filter and AppFilterOptions.AMAZON.getByte() == AppFilterOptions.AMAZON.getByte()) dataFiltered =
            dataFiltered.filter { it.installationSource == "com.amazon.venezia" }
        if (filter and AppFilterOptions.OTHERS.getByte() == AppFilterOptions.OTHERS.getByte()) dataFiltered =
            dataFiltered.filter { it.installationSource !in Utils.listOfKnownStores }
        return dataFiltered
    }

    /**
     * Switch ui mode (System, Light, Dark) either with given Parameter or with saved Preference
     * @param newValue Int castable String value to switch ui mode
     */
    fun changeUIMode(
        newValue: String = sharedPreferences.getString(
            "list_preference_ui_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString()
        )!!
    ) {
        when (newValue.toInt()) {
            AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
            AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
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
            val prefs = sharedPreferences.getStringSet("app_save_name", setOf("0:name"))
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
    fun useMaterialYou(
        application: Application,
        enabled: Boolean = sharedPreferences.getBoolean("use_material_you", false)
    ) {
        val available = DynamicColors.isDynamicColorAvailable()
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

    /**
     * Get selected swipe actions from settings
     * @param preferenceValue value string from multiselect preference
     * @return ApkActionsOptions enum with information for selected option or null
     */
    private fun getSwipeActionByPreferenceValue(preferenceValue: String?): ApkActionsOptions? {
        return ApkActionsOptions.values().firstOrNull { it.preferenceValue == preferenceValue }
    }

    /**
     * Get selected right swipe actions from settings
     * @return ApkActionsOptions enum with information for selected option or null
     */
    fun getRightSwipeAction(): ApkActionsOptions? {
        val preferenceVal =
            sharedPreferences.getString("list_preference_swipe_actions_right", "save_apk")
        return getSwipeActionByPreferenceValue(preferenceVal)
    }

    /**
     * Get selected left swipe actions from settings
     * @return ApkActionsOptions enum with information for selected option or null
     */
    fun getLeftSwipeAction(): ApkActionsOptions? {
        val preferenceVal =
            sharedPreferences.getString("list_preference_swipe_actions_left", "share_apk")
        return getSwipeActionByPreferenceValue(preferenceVal)
    }

    /**
     * Add an app as package name to preference favorite set or remove it
     * @param packageName name of package for state change
     * @param isFavorite true if it should be added to favorites, false if it should be removed
     */
    fun editFavorites(packageName: String, isFavorite: Boolean) {
        val favorites: MutableSet<String> =
            sharedPreferences.getStringSet("favorites", setOf())!!.toMutableSet()
        if (isFavorite) favorites.add(packageName)
        else favorites.remove(packageName)
        sharedPreferences.edit().putStringSet("favorites", favorites).commit()
    }
}