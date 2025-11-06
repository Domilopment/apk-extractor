package domilopment.apkextractor.domain.mapper

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import domilopment.apkextractor.data.model.appList.AppModel
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.Utils
import java.io.File

class AppModelToApplicationDetailModelMapper(val packageManager: PackageManager) :
    Mapper<AppModel, ApplicationModel.ApplicationDetailModel?> {
    override fun map(from: AppModel): ApplicationModel.ApplicationDetailModel? {
        val packageInfo = try {
            Utils.getPackageInfo(packageManager, from.packageName)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }

        return packageInfo?.applicationInfo?.let { applicationInfo ->
            ApplicationModel.ApplicationDetailModel(
                appPackageName = applicationInfo.packageName,
                appName = applicationInfo.loadLabel(packageManager).toString(),
                appSourceDirectory = applicationInfo.sourceDir,
                appSplitSourceDirectories = applicationInfo.splitSourceDirs,
                appIcon = applicationInfo.loadIcon(packageManager)
                    ?: packageManager.defaultActivityIcon,
                appVersionName = packageInfo.versionName ?: "Unknown",
                appVersionCode = packageInfo.let { Utils.versionCode(it) },
                minSdkVersion = applicationInfo.minSdkVersion,
                targetSdkVersion = applicationInfo.targetSdkVersion,
                appFlags = applicationInfo.flags,
                appCategory = applicationInfo.category,
                appInstallTime = packageInfo.firstInstallTime,
                appUpdateTime = packageInfo.lastUpdateTime,
                apkSize = listOfNotNull(
                    applicationInfo.sourceDir, *(applicationInfo.splitSourceDirs ?: emptyArray())
                ).sumOf { File(it).length() }.let { FileUtil.getBytesSizeInMB(it) },
                launchIntent = packageManager.getLaunchIntentForPackage(applicationInfo.packageName),
                installationSource = Utils.getInstallationSourceOrNull(
                    packageManager, applicationInfo
                ),
            )
        }
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
                    it.packageName
                )

                from.appFlags.and(ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM -> AppModel.SystemApp(
                    it.packageName
                )

                else -> AppModel.UserApp(it.packageName)
            }
        }
    }

}

class AppModelToApplicationListModelMapper(val packageManager: PackageManager) :
    Mapper<AppModel, ApplicationModel.ApplicationListModel?> {
    override fun map(from: AppModel): ApplicationModel.ApplicationListModel? {
        val packageInfo = try {
            Utils.getPackageInfo(packageManager, from.packageName)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }

        return packageInfo?.applicationInfo?.let { applicationInfo ->
            ApplicationModel.ApplicationListModel(
                appPackageName = applicationInfo.packageName,
                appName = applicationInfo.loadLabel(packageManager).toString(),
                appIcon = applicationInfo.loadIcon(packageManager)
                    ?: packageManager.defaultActivityIcon,
                appFlags = applicationInfo.flags,
                appCategory = applicationInfo.category,
                appInstallTime = packageInfo.firstInstallTime,
                appUpdateTime = packageInfo.lastUpdateTime,
                apkSize = listOfNotNull(
                    applicationInfo.sourceDir, *(applicationInfo.splitSourceDirs ?: emptyArray())
                ).sumOf { File(it).length() }.let { FileUtil.getBytesSizeInMB(it) },
                launchIntent = packageManager.getLaunchIntentForPackage(applicationInfo.packageName),
                installationSource = Utils.getInstallationSourceOrNull(
                    packageManager, applicationInfo
                ),
            )
        }
    }
}

class ApplicationListModelToAppModelMapper(
    val packageManager: PackageManager
) : Mapper<ApplicationModel.ApplicationListModel, AppModel?> {
    override fun map(from: ApplicationModel.ApplicationListModel): AppModel? {
        val applicationInfo = try {
            Utils.getApplicationInfo(packageManager, from.appPackageName)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }

        return applicationInfo?.let {
            when {
                it.flags.and(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP -> AppModel.UpdatedSystemApps(
                    from.appPackageName
                )

                it.flags.and(ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM -> AppModel.SystemApp(
                    from.appPackageName
                )

                else -> AppModel.UserApp(from.appPackageName)
            }
        }
    }
}

class ApplicationListModelToApplicationDetailModelMapper(
    val packageManager: PackageManager
) : Mapper<ApplicationModel.ApplicationListModel, ApplicationModel.ApplicationDetailModel?> {
    override fun map(from: ApplicationModel.ApplicationListModel): ApplicationModel.ApplicationDetailModel? {
        val packageInfo = try {
            Utils.getPackageInfo(packageManager, from.appPackageName)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }

        return packageInfo?.applicationInfo?.let { applicationInfo ->
            ApplicationModel.ApplicationDetailModel(
                appPackageName = applicationInfo.packageName,
                appName = applicationInfo.loadLabel(packageManager).toString(),
                appSourceDirectory = applicationInfo.sourceDir,
                appSplitSourceDirectories = applicationInfo.splitSourceDirs,
                appIcon = applicationInfo.loadIcon(packageManager)
                    ?: packageManager.defaultActivityIcon,
                appVersionName = packageInfo.versionName ?: "Unknown",
                appVersionCode = packageInfo.let { Utils.versionCode(it) },
                minSdkVersion = applicationInfo.minSdkVersion,
                targetSdkVersion = applicationInfo.targetSdkVersion,
                appFlags = applicationInfo.flags,
                appCategory = applicationInfo.category,
                appInstallTime = packageInfo.firstInstallTime,
                appUpdateTime = packageInfo.lastUpdateTime,
                apkSize = listOfNotNull(
                    applicationInfo.sourceDir, *(applicationInfo.splitSourceDirs ?: emptyArray())
                ).sumOf { File(it).length() }.let { FileUtil.getBytesSizeInMB(it) },
                launchIntent = packageManager.getLaunchIntentForPackage(applicationInfo.packageName),
                installationSource = Utils.getInstallationSourceOrNull(
                    packageManager, applicationInfo
                ),
            )
        }
    }
}