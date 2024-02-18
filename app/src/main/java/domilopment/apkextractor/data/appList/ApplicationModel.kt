package domilopment.apkextractor.data.appList

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.Utils
import java.io.File

/**
 * Class holding information for a specific Application installed on device
 * @param packageManager a packageManager instance to request further information of an app if needed
 * @param appPackageName the package name of an app, we want to access information about
 * @param isChecked to save and hande a selection made by user about multiple app instances
 * @param isFavorite to save and handle if the app is of higher priority to the user to perform action on or retrieve Information
 */
data class ApplicationModel(
    private val packageManager: PackageManager,
    val appPackageName: String,
    var isChecked: Boolean = false,
    var isFavorite: Boolean = false
) {
    private val packageInfo: PackageInfo
        get() = Utils.getPackageInfo(packageManager, appPackageName)
    val appName: String
        get() = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString()
    val appSourceDirectory: String get() = packageInfo.applicationInfo.sourceDir
    val appSplitSourceDirectories: Array<String>? get() = packageInfo.applicationInfo.splitSourceDirs
    val appIcon: Drawable get() = packageManager.getApplicationIcon(packageInfo.applicationInfo)
    val appVersionName: String? get() = packageInfo.versionName
    val appVersionCode: Long get() = Utils.versionCode(packageInfo)
    val appFlags: Int get() = packageInfo.applicationInfo.flags
    val appCategory: Int get() = packageInfo.applicationInfo.category
    val appInstallTime: Long get() = packageInfo.firstInstallTime
    val appUpdateTime: Long get() = packageInfo.lastUpdateTime
    val apkSize: Float
        get() = FileUtil.getBytesSizeInMB(listOf(
            packageInfo.applicationInfo.sourceDir,
            *(packageInfo.applicationInfo.splitSourceDirs ?: emptyArray())
        ).sumOf { File(it).length() })
    val launchIntent: Intent? get() = packageManager.getLaunchIntentForPackage(appPackageName)
    val installationSource: String?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) packageManager.getInstallSourceInfo(
            appPackageName
        ).installingPackageName
        else packageManager.getInstallerPackageName(appPackageName)
}