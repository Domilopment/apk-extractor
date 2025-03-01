package domilopment.apkextractor.utils.settings

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import domilopment.apkextractor.data.model.appList.AppModel
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.utils.SaveApkResult
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.appFilterOptions.AppFilter
import domilopment.apkextractor.utils.appFilterOptions.AppFilterCategories
import domilopment.apkextractor.utils.appFilterOptions.AppFilterInstaller
import domilopment.apkextractor.utils.appFilterOptions.AppFilterOthers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

object ApplicationUtil {
    /**
     * Gets an app and Creates a Name from its Data
     * @param app the resource App
     * @return String of the name after the APK should be named
     */
    fun appName(
        packageManager: PackageManager, packageInfo: PackageInfo, set: Set<String>
    ): String {
        val appName =
            packageInfo.applicationInfo?.loadLabel(packageManager) ?: packageInfo.packageName
        val names = mapOf(
            "name" to appName,
            "package" to packageInfo.packageName,
            "version_name" to packageInfo.versionName,
            "version_number" to "v${Utils.versionCode(packageInfo)}",
            "datetime" to SimpleDateFormat.getDateTimeInstance().format(Date())
        )
        return StringBuilder().apply {
            val processedPrefs = try {
                set.toSortedSet(compareBy { it[0].digitToInt() }).map { it.removeRange(0, 2) }
            } catch (e: Exception) {
                set
            }
            processedPrefs.also {
                if (it.isEmpty()) append(appName)
                else it.forEach { v ->
                    append(" ${names[v]}")
                }
            }
        }.removePrefix(" ").toString()
    }

    fun appName(
        packageManager: PackageManager, applicationInfo: ApplicationInfo, set: Set<String>
    ): String {
        val packageInfo = Utils.getPackageInfo(packageManager, applicationInfo.packageName)
        return appName(packageManager, packageInfo, set)
    }

    /**
     * Creates a List containing of all Types the User Selected in Settings
     * @return List of Selected App Types
     */
    fun selectedAppTypes(
        applications: List<AppModel>,
        selectUpdatedSystemApps: Boolean,
        selectSystemApps: Boolean,
        selectUserApps: Boolean
    ): List<AppModel> {
        val mData: MutableList<AppModel> = mutableListOf()
        if (selectUpdatedSystemApps) {
            mData.addAll(applications.filter { it is AppModel.UpdatedSystemApps })
            if (selectSystemApps) mData.addAll(applications.filter { it is AppModel.SystemApp })
        }
        if (selectUserApps) mData.addAll(applications.filter { it is AppModel.UserApp })
        return mData
    }

    /**
     * Sets ApplicationModels favorite state from Set of package names
     * @param apps List of Applications
     * @param favorites Set of package names
     * @return List of Applications with correct favorite state
     */
    fun getFavorites(
        apps: List<ApplicationModel>, favorites: Set<String>
    ): List<ApplicationModel> {
        return apps.map {
            it.copy(isFavorite = it.appPackageName in favorites)
        }
    }

    /**
     * Filter Apps out of List
     * @param data List of Apps
     * @return filtered list of Applications
     */
    fun filterApps(
        data: List<ApplicationModel>,
        filterInstaller: String?,
        filterCategory: String?,
        filterOthers: Set<String>
    ): List<ApplicationModel> {
        val filter = mutableSetOf<AppFilter>()
        filterInstaller?.let { filter.add(AppFilterInstaller.valueOf(it)) }
        filterCategory?.let { filter.add(AppFilterCategories.valueOf(it)) }
        filterOthers.map { AppFilterOthers.valueOf(it) }.let { filter.addAll(it) }

        if (filter.isEmpty()) return data

        var dataFiltered = data
        filter.forEach {
            dataFiltered = it.getFilter(dataFiltered)
        }

        return dataFiltered
    }

    /**
     * Sorts Data by user selected Order
     * @param data Unsorted List of Apps
     * @return Sorted List of Apps
     */
    fun sortAppData(
        data: List<ApplicationModel>, sortMode: Int, sortFavorites: Boolean, sortAsc: Boolean
    ): List<ApplicationModel> {
        val comparator = AppSortOptions[sortMode].comparator(sortAsc)
        val sortedList = data.sortedWith(comparator)
        return if (sortFavorites) sortedList.sortedBy { app -> !app.isFavorite } else sortedList
    }

    /**
     * Add an app as package name to preference favorite set or remove it
     * @param packageName name of package for state change
     * @param isFavorite true if it should be added to favorites, false if it should be removed
     */
    fun editFavorites(
        favorites: Set<String>, packageName: String, isFavorite: Boolean
    ): Set<String> {
        val newFavorites: MutableSet<String> = favorites.toMutableSet()
        if (isFavorite) newFavorites.add(packageName)
        else newFavorites.remove(packageName)
        return newFavorites
    }

    suspend fun saveApk(
        context: Context, from: String, to: Uri, fileName: String
    ): SaveApkResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val extractedApk = FileUtil.copy(
                context,
                from,
                to,
                fileName,
                FileUtil.FileInfo.APK.mimeType,
                FileUtil.FileInfo.APK.suffix
            )
            SaveApkResult.Success(extractedApk)
        } catch (fnf_e: FileNotFoundException) {
            fnf_e.printStackTrace()
            SaveApkResult.Failure(fnf_e.message)
        } catch (e: Exception) {
            e.printStackTrace()
            SaveApkResult.Failure(e.message)
        }
    }

    suspend fun saveXapk(
        context: Context,
        files: Array<String>,
        to: Uri,
        fileName: String,
        mimeType: String,
        suffix: String,
        callback: suspend (String) -> Unit
    ): SaveApkResult = withContext(Dispatchers.IO) {
        var extractedApk: Uri? = null
        return@withContext try {
            extractedApk = FileUtil.ZipUtil.createPersistentZip(
                context, to, fileName, mimeType, suffix
            )!!
            FileUtil.ZipUtil.openZipOutputStream(context, extractedApk).use { output ->
                for (file in files) {
                    ensureActive()
                    FileUtil.ZipUtil.writeToZip(output, file)
                    callback(file)
                }
            }
            SaveApkResult.Success(extractedApk)
        } catch (fnf_e: FileNotFoundException) {
            extractedApk?.let {
                FileUtil.deleteDocument(context, extractedApk)
            }
            SaveApkResult.Failure(fnf_e.message)
        } catch (e: CancellationException) {
            extractedApk?.let {
                FileUtil.deleteDocument(context, extractedApk)
            }
            SaveApkResult.Failure(e.message)
        } catch (e: Exception) {
            e.printStackTrace()
            SaveApkResult.Failure(e.message)
        }
    }

    suspend fun shareApk(context: Context, app: ApplicationModel, appName: String): Uri =
        withContext(Dispatchers.IO) {
            val outFile = FileUtil.createTempFile(context, appName, FileUtil.FileInfo.APK.suffix)

            FileInputStream(app.appSourceDirectory).use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
            return@withContext FileUtil.createShareUriForFile(context, outFile)
        }

    suspend fun shareXapk(
        context: Context,
        app: ApplicationModel,
        appName: String,
        suffix: String,
        callback: suspend (String) -> Unit
    ): Uri = withContext(Dispatchers.IO) {
        val splits = arrayOf(
            app.appSourceDirectory, *(app.appSplitSourceDirectories ?: emptyArray())
        )

        val outFile = FileUtil.ZipUtil.createTempZip(context, appName, suffix)
        FileUtil.ZipUtil.openZipOutputStream(outFile).use { output ->
            for (file in splits) {
                FileUtil.ZipUtil.writeToZip(output, file)
                callback(file)
            }
        }
        return@withContext FileUtil.createShareUriForFile(context, outFile)
    }

    fun appModelFromPackageName(packageName: String, packageManager: PackageManager): AppModel? {
        val applicationInfo = try {
            Utils.getApplicationInfo(packageManager, packageName)
        } catch (_: PackageManager.NameNotFoundException) {
            return null
        }

        return if (applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) AppModel.UpdatedSystemApps(
            applicationInfo
        )
        else if (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM) AppModel.SystemApp(
            applicationInfo
        )
        else AppModel.UserApp(applicationInfo)
    }
}