package domilopment.apkextractor.utils.settings

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import domilopment.apkextractor.autoBackup.AutoBackupService
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.data.PackageArchiveModel
import domilopment.apkextractor.utils.Constants
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.ListOfApps
import domilopment.apkextractor.utils.apkActions.ApkActionsOptions
import domilopment.apkextractor.utils.appFilterOptions.AppFilter
import domilopment.apkextractor.utils.appFilterOptions.AppFilterCategories
import domilopment.apkextractor.utils.appFilterOptions.AppFilterInstaller
import domilopment.apkextractor.utils.appFilterOptions.AppFilterOthers
import java.text.SimpleDateFormat
import java.util.*

class SettingsManager(context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * Creates a List containing of all Types the User Selected in Settings
     * @return List of Selected App Types
     */
    fun selectedAppTypes(
        applications: Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>,
        selectUpdatedSystemApps: Boolean = sharedPreferences.getBoolean(
            Constants.PREFERENCE_KEY_UPDATED_SYSTEM_APPS, false
        ),
        selectSystemApps: Boolean = sharedPreferences.getBoolean(
            Constants.PREFERENCE_KEY_SYSTEM_APPS, false
        ),
        selectUserApps: Boolean = sharedPreferences.getBoolean(
            Constants.PREFERENCE_KEY_USER_APPS, true
        ),
    ): List<ApplicationModel> {
        val (updatedSystemApps, systemApps, userApps) = applications
        val mData: MutableList<ApplicationModel> = mutableListOf()
        if (selectUpdatedSystemApps) {
            mData.addAll(updatedSystemApps)
            if (selectSystemApps) mData.addAll(systemApps)
        }
        if (selectUserApps) mData.addAll(userApps)
        mData.forEach {
            it.isFavorite = it.appPackageName in sharedPreferences.getStringSet(
                Constants.PREFERENCE_KEY_FAVORITES, setOf()
            )!!
        }
        return mData
    }

    /**
     * Gives back in SharedPreferences Saved Directory Path
     * @return Saved Directory Path
     */
    fun saveDir(): Uri? =
        sharedPreferences.getString(Constants.PREFERENCE_KEY_SAVE_DIR, null)?.let { Uri.parse(it) }

    /**
     * Sorts Data by user selected Order
     * @param data Unsorted List of Apps
     * @return Sorted List of Apps
     */
    fun sortAppData(
        data: List<ApplicationModel>, sortMode: Int = sharedPreferences.getInt(
            Constants.PREFERENCE_KEY_APP_SORT, AppSortOptions.SORT_BY_NAME.ordinal
        ), sortFavorites: Boolean = sharedPreferences.getBoolean(
            Constants.PREFERENCE_KEY_SORT_FAVORITES, true
        )
    ): List<ApplicationModel> {
        val comparator = AppSortOptions[sortMode].comparator(
            sharedPreferences.getBoolean(
                Constants.PREFERENCE_KEY_APP_SORT_ASC, true
            )
        )
        val sortedList = data.sortedWith(comparator)
        return if (sortFavorites) sortFavorites(sortedList) else sortedList
    }

    /**
     * Sorts Data by user selected Order
     * @param data Unsorted List of APKs
     * @return Sorted List of APKs
     */
    fun sortApkData(
        data: List<PackageArchiveModel>,
        sortMode: ApkSortOptions = ApkSortOptions.SORT_BY_FILE_SIZE_DESC
    ): List<PackageArchiveModel> {
        val pref = sharedPreferences.getString("apk_sort", sortMode.name)
        return data.sortedWith(ApkSortOptions[pref].comparator)
    }

    /**
     * Sorts Favorites to top of the app list
     * @param data List of APKs
     */
    private fun sortFavorites(data: List<ApplicationModel>): List<ApplicationModel> {
        return data.sortedBy { app ->
            app.appPackageName !in sharedPreferences.getStringSet(
                Constants.PREFERENCE_KEY_FAVORITES, setOf()
            )!!
        }
    }

    /**
     * Filter Apps out of List
     * @param data List of Apps
     * @return filtered list of Applications
     */
    fun filterApps(data: List<ApplicationModel>): List<ApplicationModel> {
        val filter = mutableSetOf<AppFilter>()
        sharedPreferences.getString(Constants.PREFERENCE_KEY_FILTER_INSTALLER, null)
            ?.let { filter.add(AppFilterInstaller.valueOf(it)) }
        sharedPreferences.getString(Constants.PREFERENCE_KEY_FILTER_CATEGORY, null)
            ?.let { filter.add(AppFilterCategories.valueOf(it)) }
        sharedPreferences.getStringSet(Constants.PREFERENCE_KEY_FILTER_OTHERS, null)
            ?.map { AppFilterOthers.valueOf(it) }?.let { filter.addAll(it) }

        if (filter.isEmpty()) return data

        var dataFiltered = data
        filter.forEach {
            dataFiltered = it.getFilter(dataFiltered)
        }

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
            append(FileUtil.PREFIX)
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
            sharedPreferences.getStringSet(Constants.PREFERENCE_KEY_FAVORITES, setOf())!!
                .toMutableSet()
        if (isFavorite) favorites.add(packageName)
        else favorites.remove(packageName)
        sharedPreferences.edit().putStringSet(Constants.PREFERENCE_KEY_FAVORITES, favorites)
            .commit()
    }
}