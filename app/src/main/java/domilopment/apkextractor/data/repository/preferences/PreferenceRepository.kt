package domilopment.apkextractor.data.repository.preferences

import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.APK_SORT_ORDER
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.APP_AUTO_BACKUP_LIST
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.APP_FILTER_CATEGORY
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.APP_FILTER_INSTALLER
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.APP_FILTER_OTHERS
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.APP_LEFT_SWIPE_ACTION
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.APP_LIST_FAVORITES
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.APP_RIGHT_SWIPE_ACTION
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.APP_SAVE_NAME
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.APP_SAVE_NAME_SPACER
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.APP_SORT_ASC
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.APP_SORT_FAVORITES
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.APP_SORT_ORDER
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.APP_SWIPE_ACTION_CUSTOM_THRESHOLD
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.APP_SWIPE_ACTION_THRESHOLD_MODIFIER
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.AUTO_BACKUP_SERVICE
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.BACKUP_MODE_XAPK
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.BUNDLE_FILE_INFO
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.CHECK_UPDATE_ON_START
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.DATA_COLLECTION_ANALYTICS
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.DATA_COLLECTION_CRASHLYTICS
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.DATA_COLLECTION_PERF
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.FIRST_LAUNCH
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.MATERIAL_YOU
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.NIGHT_MODE
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.SAVE_DIR
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.SYSTEM_APPS
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.UPDATED_SYSTEM_APPS
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository.PreferencesKeys.USER_APPS
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.apkActions.ApkActionsOptions
import domilopment.apkextractor.utils.settings.ApkSortOptions
import domilopment.apkextractor.utils.settings.AppSortOptions
import domilopment.apkextractor.utils.settings.SettingsManager
import domilopment.apkextractor.utils.settings.Spacer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

interface PreferenceRepository {
    object PreferencesKeys {
        val SAVE_DIR = stringPreferencesKey("dir")
        val CHECK_UPDATE_ON_START = booleanPreferencesKey("check_update_on_start")
        val UPDATED_SYSTEM_APPS = booleanPreferencesKey("updated_system_apps")
        val SYSTEM_APPS = booleanPreferencesKey("system_apps")
        val USER_APPS = booleanPreferencesKey("user_apps")
        val APP_SORT_ORDER = intPreferencesKey("app_sort")
        val APP_SORT_FAVORITES = booleanPreferencesKey("sort_favorites")
        val APP_SORT_ASC = booleanPreferencesKey("app_sort_asc")
        val APP_LIST_FAVORITES = stringSetPreferencesKey("favorites")
        val APP_FILTER_INSTALLER = stringPreferencesKey("filter_installer_v2")
        val APP_FILTER_CATEGORY = stringPreferencesKey("filter_category_v2")
        val APP_FILTER_OTHERS = stringSetPreferencesKey("filter_others")
        val APP_RIGHT_SWIPE_ACTION = stringPreferencesKey("list_preference_swipe_actions_right")
        val APP_LEFT_SWIPE_ACTION = stringPreferencesKey("list_preference_swipe_actions_left")
        val APP_SWIPE_ACTION_CUSTOM_THRESHOLD =
            booleanPreferencesKey("swipe_action_custom_threshold")
        val APP_SWIPE_ACTION_THRESHOLD_MODIFIER =
            floatPreferencesKey("swipe_action_threshold_modifier")
        val APP_AUTO_BACKUP_LIST = stringSetPreferencesKey("app_list_auto_backup")
        val APP_SAVE_NAME = stringSetPreferencesKey("app_save_name")
        val APK_SORT_ORDER = stringPreferencesKey("apk_sort")
        val AUTO_BACKUP_SERVICE = booleanPreferencesKey("auto_backup")
        val MATERIAL_YOU = booleanPreferencesKey("use_material_you")
        val NIGHT_MODE = stringPreferencesKey("list_preference_ui_mode")
        val BACKUP_MODE_XAPK = booleanPreferencesKey("backup_mode_xapk")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch_0")
        val DATA_COLLECTION_ANALYTICS = booleanPreferencesKey("data_collection_analytics")
        val DATA_COLLECTION_CRASHLYTICS = booleanPreferencesKey("data_collection_crashlytics")
        val DATA_COLLECTION_PERF = booleanPreferencesKey("data_collection_perf")
        val BUNDLE_FILE_INFO = stringPreferencesKey("bundle_file_info")
        val APP_SAVE_NAME_SPACER = stringPreferencesKey("app_save_name_spacer")
    }

    val saveDir: Flow<Uri?>
    suspend fun setSaveDir(uri: Uri)

    val updatedSysApps: Flow<Boolean>
    suspend fun setUpdatedSysApps(value: Boolean)

    val sysApps: Flow<Boolean>
    suspend fun setSysApps(value: Boolean)

    val userApps: Flow<Boolean>
    suspend fun setUserApps(value: Boolean)

    val appSortOrder: Flow<AppSortOptions>
    suspend fun setAppSortOrder(value: Int)

    val appSortFavorites: Flow<Boolean>
    suspend fun setAppSortFavorites(value: Boolean)

    val appSortAsc: Flow<Boolean>
    suspend fun setAppSortAsc(value: Boolean)

    val appListFavorites: Flow<Set<String>>
    suspend fun setAppListFavorites(favorites: Set<String>)

    val appFilterInstaller: Flow<String?>
    suspend fun setAppFilterInstaller(value: String?)

    val appFilterCategory: Flow<String?>
    suspend fun setAppFilterCategory(value: String?)

    val appFilterOthers: Flow<Set<String>>
    suspend fun setAppFilterOthers(value: Set<String>)

    val autoBackupAppList: Flow<Set<String>>
    suspend fun setListOfAutoBackupApps(list: Set<String>)

    val appSaveName: Flow<Set<String>>
    suspend fun setAppSaveName(set: Set<String>)

    val apkSortOrder: Flow<ApkSortOptions>
    suspend fun setApkSortOrder(value: String)

    val checkUpdateOnStart: Flow<Boolean>
    suspend fun setCheckUpdateOnStart(value: Boolean)

    val appRightSwipeAction: Flow<ApkActionsOptions>
    suspend fun setRightSwipeAction(value: String)

    val appLeftSwipeAction: Flow<ApkActionsOptions>
    suspend fun setLeftSwipeAction(value: String)

    val appSwipeActionCustomThreshold: Flow<Boolean>
    suspend fun setSwipeActionCustomThreshold(value: Boolean)

    val appSwipeActionThresholdMod: Flow<Float>
    suspend fun setSwipeActionThresholdMod(value: Float)

    val autoBackupService: Flow<Boolean>
    suspend fun setAutoBackupService(value: Boolean)

    val useMaterialYou: Flow<Boolean>
    suspend fun setUseMaterialYou(value: Boolean)

    val nightMode: Flow<Int>
    suspend fun setNightMode(value: Int)

    val backupModeXapk: Flow<Boolean>
    suspend fun setBackupModeXapk(value: Boolean)

    val firstLaunch: Flow<Boolean>
    suspend fun setFirstLaunch(value: Boolean)

    val analytics: Flow<Boolean>
    suspend fun setAnalytics(value: Boolean)

    val crashlytics: Flow<Boolean>
    suspend fun setCrashlytics(value: Boolean)

    val performance: Flow<Boolean>
    suspend fun setPerformance(value: Boolean)

    val bundleFileInfo: Flow<FileUtil.FileInfo>
    suspend fun setBundleFileInfo(value: FileUtil.FileInfo)

    val appSaveNameSpacer: Flow<Spacer>
    suspend fun setAppSaveNameSpacer(value: Spacer)
}

class MyPreferenceRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PreferenceRepository {
    private fun <T> getPreference(key: Preferences.Key<T>): Flow<T?> =
        dataStore.data.catch { exception ->
            /*
             * dataStore.data throws an IOException when an error
             * is encountered when reading data
             */
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[key]
        }

    private suspend fun <T> setPreference(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    override val saveDir: Flow<Uri?> = getPreference(SAVE_DIR).map { it?.let { Uri.parse(it) } }
    override suspend fun setSaveDir(uri: Uri) = setPreference(SAVE_DIR, uri.toString())

    override val updatedSysApps: Flow<Boolean> =
        getPreference(UPDATED_SYSTEM_APPS).map { it ?: false }

    override suspend fun setUpdatedSysApps(value: Boolean) =
        setPreference(UPDATED_SYSTEM_APPS, value)

    override val sysApps: Flow<Boolean> = getPreference(SYSTEM_APPS).map { it ?: false }

    override suspend fun setSysApps(value: Boolean) = setPreference(SYSTEM_APPS, value)

    override val userApps: Flow<Boolean> = getPreference(USER_APPS).map { it ?: true }

    override suspend fun setUserApps(value: Boolean) = setPreference(USER_APPS, value)

    override val appSortOrder: Flow<AppSortOptions> = getPreference(APP_SORT_ORDER).map {
        it?.let { AppSortOptions[it] } ?: AppSortOptions.SORT_BY_NAME
    }

    override suspend fun setAppSortOrder(value: Int) = setPreference(APP_SORT_ORDER, value)

    override val appSortFavorites: Flow<Boolean> =
        getPreference(APP_SORT_FAVORITES).map { it ?: true }

    override suspend fun setAppSortFavorites(value: Boolean) =
        setPreference(APP_SORT_FAVORITES, value)

    override val appSortAsc: Flow<Boolean> = getPreference(APP_SORT_ASC).map { it ?: true }
    override suspend fun setAppSortAsc(value: Boolean) = setPreference(APP_SORT_ASC, value)

    override val appListFavorites: Flow<Set<String>> =
        getPreference(APP_LIST_FAVORITES).map { it ?: emptySet() }

    override suspend fun setAppListFavorites(favorites: Set<String>) =
        setPreference(APP_LIST_FAVORITES, favorites)

    override val appFilterInstaller: Flow<String?> = getPreference(APP_FILTER_INSTALLER)

    override suspend fun setAppFilterInstaller(value: String?) {
        if (value.isNullOrEmpty()) dataStore.edit { preferences ->
            preferences.remove(
                APP_FILTER_INSTALLER
            )
        }
        else setPreference(APP_FILTER_INSTALLER, value)
    }

    override val appFilterCategory: Flow<String?> = getPreference(APP_FILTER_CATEGORY)

    override suspend fun setAppFilterCategory(value: String?) {
        if (value.isNullOrEmpty()) dataStore.edit { preferences ->
            preferences.remove(
                APP_FILTER_CATEGORY
            )
        }
        else setPreference(APP_FILTER_CATEGORY, value)
    }

    override val appFilterOthers: Flow<Set<String>> =
        getPreference(APP_FILTER_OTHERS).map { it ?: emptySet() }

    override suspend fun setAppFilterOthers(value: Set<String>) =
        setPreference(APP_FILTER_OTHERS, value)

    override val autoBackupAppList: Flow<Set<String>> =
        getPreference(APP_AUTO_BACKUP_LIST).map { it ?: emptySet() }

    override suspend fun setListOfAutoBackupApps(list: Set<String>) =
        setPreference(APP_AUTO_BACKUP_LIST, list)

    override val appSaveName: Flow<Set<String>> =
        getPreference(APP_SAVE_NAME).map { it ?: setOf("0:name") }

    override suspend fun setAppSaveName(set: Set<String>) = setPreference(APP_SAVE_NAME, set)

    override val apkSortOrder: Flow<ApkSortOptions> = getPreference(APK_SORT_ORDER).map {
        it?.let { ApkSortOptions[it] } ?: ApkSortOptions.SORT_BY_FILE_SIZE_DESC
    }

    override suspend fun setApkSortOrder(value: String) = setPreference(APK_SORT_ORDER, value)

    override val checkUpdateOnStart: Flow<Boolean> =
        getPreference(CHECK_UPDATE_ON_START).map { it ?: true }

    override suspend fun setCheckUpdateOnStart(value: Boolean) =
        setPreference(CHECK_UPDATE_ON_START, value)

    override val appRightSwipeAction: Flow<ApkActionsOptions> =
        getPreference(APP_RIGHT_SWIPE_ACTION).map {
            SettingsManager.getSwipeActionByPreferenceValue(it) ?: ApkActionsOptions.SAVE
        }

    override suspend fun setRightSwipeAction(value: String) =
        setPreference(APP_RIGHT_SWIPE_ACTION, value)

    override val appLeftSwipeAction: Flow<ApkActionsOptions> =
        getPreference(APP_LEFT_SWIPE_ACTION).map {
            SettingsManager.getSwipeActionByPreferenceValue(it) ?: ApkActionsOptions.SHARE
        }

    override suspend fun setLeftSwipeAction(value: String) =
        setPreference(APP_LEFT_SWIPE_ACTION, value)

    override val appSwipeActionCustomThreshold: Flow<Boolean> =
        getPreference(APP_SWIPE_ACTION_CUSTOM_THRESHOLD).map {
            it ?: false
        }

    override suspend fun setSwipeActionCustomThreshold(value: Boolean) =
        setPreference(APP_SWIPE_ACTION_CUSTOM_THRESHOLD, value)

    override val appSwipeActionThresholdMod: Flow<Float> =
        getPreference(APP_SWIPE_ACTION_THRESHOLD_MODIFIER).map { it ?: 32f }

    override suspend fun setSwipeActionThresholdMod(value: Float) =
        setPreference(APP_SWIPE_ACTION_THRESHOLD_MODIFIER, value)

    override val autoBackupService: Flow<Boolean> =
        getPreference(AUTO_BACKUP_SERVICE).map { it ?: false }

    override suspend fun setAutoBackupService(value: Boolean) =
        setPreference(AUTO_BACKUP_SERVICE, value)

    override val useMaterialYou: Flow<Boolean> = getPreference(MATERIAL_YOU).map { it ?: true }
    override suspend fun setUseMaterialYou(value: Boolean) = setPreference(MATERIAL_YOU, value)

    override val nightMode: Flow<Int> =
        getPreference(NIGHT_MODE).map { it?.toInt() ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM }

    override suspend fun setNightMode(value: Int) = setPreference(NIGHT_MODE, value.toString())

    override val backupModeXapk: Flow<Boolean> = getPreference(BACKUP_MODE_XAPK).map { it ?: true }

    override suspend fun setBackupModeXapk(value: Boolean) = setPreference(BACKUP_MODE_XAPK, value)

    override val firstLaunch: Flow<Boolean> = getPreference(FIRST_LAUNCH).map { it ?: true }

    override suspend fun setFirstLaunch(value: Boolean) = setPreference(FIRST_LAUNCH, value)

    override val analytics: Flow<Boolean> =
        getPreference(DATA_COLLECTION_ANALYTICS).map { it ?: false }

    override suspend fun setAnalytics(value: Boolean) =
        setPreference(DATA_COLLECTION_ANALYTICS, value)

    override val crashlytics: Flow<Boolean> =
        getPreference(DATA_COLLECTION_CRASHLYTICS).map { it ?: false }

    override suspend fun setCrashlytics(value: Boolean) =
        setPreference(DATA_COLLECTION_CRASHLYTICS, value)

    override val performance: Flow<Boolean> =
        getPreference(DATA_COLLECTION_PERF).map { it ?: false }

    override suspend fun setPerformance(value: Boolean) = setPreference(DATA_COLLECTION_PERF, value)

    override val bundleFileInfo: Flow<FileUtil.FileInfo> = getPreference(BUNDLE_FILE_INFO).map {
        it?.let { suffix -> FileUtil.FileInfo.fromSuffix(suffix) } ?: FileUtil.FileInfo.APKS
    }

    override suspend fun setBundleFileInfo(value: FileUtil.FileInfo) {
        setPreference(BUNDLE_FILE_INFO, value.suffix)
    }

    override val appSaveNameSpacer: Flow<Spacer> =
        getPreference(APP_SAVE_NAME_SPACER).map { it?.let { Spacer.fromName(it) } ?: Spacer.SPACE }

    override suspend fun setAppSaveNameSpacer(value: Spacer) =
        setPreference(APP_SAVE_NAME_SPACER, value.name)
}