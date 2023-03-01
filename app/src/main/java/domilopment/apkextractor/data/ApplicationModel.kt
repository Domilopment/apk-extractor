package domilopment.apkextractor.data

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import domilopment.apkextractor.utils.Utils
import java.io.File

data class ApplicationModel(
    private val packageManager: PackageManager, val appPackageName: String
) {
    private val packageInfo: PackageInfo
        get() = Utils.getPackageInfo(packageManager, appPackageName)
    val appName: String
        get() = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString()
    val appSourceDirectory: String get() = packageInfo.applicationInfo.sourceDir
    val appIcon: Drawable get() = packageManager.getApplicationIcon(packageInfo.applicationInfo)
    val appVersionName: String get() = packageInfo.versionName
    val appVersionCode: Long get() = Utils.versionCode(packageInfo)
    val appFlags: Int get() = packageInfo.applicationInfo.flags
    val appInstallTime: Long get() = packageInfo.firstInstallTime
    val appUpdateTime: Long get() = packageInfo.lastUpdateTime
    val apkSize: Float get() = File(packageInfo.applicationInfo.sourceDir).length() / (1000.0F * 1000.0F) // Calculate MB Size
    val launchIntent: Intent? get() = packageManager.getLaunchIntentForPackage(appPackageName)
    val installationSource: String?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) packageManager.getInstallSourceInfo(
            appPackageName
        ).installingPackageName
        else packageManager.getInstallerPackageName(appPackageName)
    var isChecked: Boolean = false
    var isFavorite: Boolean = false
}