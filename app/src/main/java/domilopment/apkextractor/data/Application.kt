package domilopment.apkextractor.data

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build

class Application(
    private val applicationInfo: ApplicationInfo,
    private val packageManager: PackageManager
) {
    val appName: String get() = packageManager.getApplicationLabel(applicationInfo).toString()
    val appPackageName: String = applicationInfo.packageName
    val appSourceDirectory: String = applicationInfo.sourceDir
    val appIcon: Drawable get() = packageManager.getApplicationIcon(applicationInfo)
    val appVersionName: String get() = packageManager.getPackageInfo(applicationInfo.packageName, 0).versionName
    val appVersionCode: Long
            get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                packageManager.getPackageInfo(applicationInfo.packageName, 0).longVersionCode
            else
                packageManager.getPackageInfo(applicationInfo.packageName, 0).versionCode.toLong()
    val appFlags: Int = applicationInfo.flags
    var isChecked: Boolean = false
        private set

    fun check(bool: Boolean) {
        isChecked = bool
    }
}