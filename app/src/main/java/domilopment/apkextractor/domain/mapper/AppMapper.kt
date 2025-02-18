package domilopment.apkextractor.domain.mapper

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import domilopment.apkextractor.data.model.appList.AppModel
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.Utils
import java.io.File

class AppModelToApplicationModelMapper(val packageManager: PackageManager) :
    Mapper<AppModel, ApplicationModel> {
    override fun map(from: AppModel): ApplicationModel {
        val packageInfo = try {
            Utils.getPackageInfo(packageManager, from.applicationInfo.packageName)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }

        return ApplicationModel(
            appPackageName = from.applicationInfo.packageName,
            appName = from.applicationInfo.loadLabel(packageManager).toString(),
            appSourceDirectory = from.applicationInfo.sourceDir,
            appSplitSourceDirectories = from.applicationInfo.splitSourceDirs,
            appIcon = from.applicationInfo.loadIcon(packageManager)
                ?: packageManager.defaultActivityIcon,
            appVersionName = packageInfo?.versionName ?: "Unknown",
            appVersionCode = packageInfo?.let { Utils.versionCode(it) } ?: -1,
            minSdkVersion = from.applicationInfo.minSdkVersion,
            targetSdkVersion = from.applicationInfo.targetSdkVersion,
            appFlags = from.applicationInfo.flags,
            appCategory = from.applicationInfo.category,
            appInstallTime = packageInfo?.firstInstallTime ?: 0,
            appUpdateTime = packageInfo?.lastUpdateTime ?: 0,
            apkSize = listOfNotNull(
                from.applicationInfo.sourceDir,
                *(from.applicationInfo.splitSourceDirs ?: emptyArray())
            ).sumOf { File(it).length() }.let { FileUtil.getBytesSizeInMB(it) },
            launchIntent = packageManager.getLaunchIntentForPackage(from.applicationInfo.packageName),
            installationSource = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) packageManager.getInstallSourceInfo(
                from.applicationInfo.packageName
            ).installingPackageName
            else packageManager.getInstallerPackageName(from.applicationInfo.packageName),
        )
    }
}

class ApplicationModelToAppModelMapper(
    val packageManager: PackageManager
) : Mapper<ApplicationModel, AppModel?> {
    override fun map(from: ApplicationModel): AppModel? {
        val applicationInfo = try {
            Utils.getApplicationInfo(packageManager, from.appPackageName)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }

        return applicationInfo?.let {
            when {
                from.appFlags.and(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP -> AppModel.UpdatedSystemApps(
                    it
                )

                from.appFlags.and(ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM -> AppModel.SystemApp(
                    it
                )

                else -> AppModel.UserApp(it)
            }
        }
    }

}