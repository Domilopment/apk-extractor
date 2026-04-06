package domilopment.apkextractor.domain.usecase.appList

import android.content.pm.PackageManager
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.PackageName
import domilopment.apkextractor.utils.Utils
import kotlinx.coroutines.flow.first
import java.io.File

interface GetAppDetailsUseCase {
    suspend operator fun invoke(applicationModel: ApplicationModel): ApplicationModel.ApplicationDetailModel?
    suspend operator fun invoke(packageName: PackageName): ApplicationModel.ApplicationDetailModel?
}

class GetAppDetailsUseCaseImpl(
    private val packageManager: PackageManager,
    private val preferenceRepository: PreferenceRepository
) : GetAppDetailsUseCase {
    override suspend fun invoke(applicationModel: ApplicationModel): ApplicationModel.ApplicationDetailModel? {
        return invoke(applicationModel.appPackageName)
    }

    override suspend fun invoke(packageName: PackageName): ApplicationModel.ApplicationDetailModel? {
        val packageInfo = try {
            Utils.getPackageInfo(packageManager, packageName)
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
                isFavorite = preferenceRepository.appListFavorites.first()
                    .contains(applicationInfo.packageName)
            )
        }
    }
}