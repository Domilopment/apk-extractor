package domilopment.apkextractor.domain.mapper

import android.content.pm.PackageManager
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import domilopment.apkextractor.data.apkList.PackageArchiveFile
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.settings.PackageArchiveUtils

class PackageArchiveModelToPackageArchiveEntityMapper(val packageManager: PackageManager) :
    Mapper<PackageArchiveFile, PackageArchiveEntity> {
    override fun map(from: PackageArchiveFile): PackageArchiveEntity {
        val apkFile = from.packageArchiveInfo()

        return apkFile?.let {
            PackageArchiveUtils.getPackageInfoFromApkFile(
                packageManager, it
            )
        }?.let { apk ->
            val load = PackageArchiveEntity(
                fileUri = from.fileUri,
                fileName = from.fileName,
                fileType = from.fileType,
                fileLastModified = from.fileLastModified,
                fileSize = from.fileSize,
                appName = apk.applicationInfo.loadLabel(packageManager).toString(),
                appPackageName = apk.applicationInfo.packageName,
                appIcon = apk.applicationInfo.loadIcon(packageManager)?.toBitmap()?.asImageBitmap(),
                appVersionName = apk.versionName,
                appVersionCode = Utils.versionCode(apk),
                appMinSdkVersion = apk.applicationInfo.minSdkVersion,
                appTargetSdkVersion = apk.applicationInfo.targetSdkVersion,
                loaded = true
            )
            apkFile.delete()
            load
        } ?: PackageArchiveEntity(
            fileUri = from.fileUri,
            fileName = from.fileName,
            fileType = from.fileType,
            fileLastModified = from.fileLastModified,
            fileSize = from.fileSize
        )
    }
}