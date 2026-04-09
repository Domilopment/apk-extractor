package domilopment.apkextractor.domain.mapper

import domilopment.apkextractor.data.model.apkList.ApkModel
import domilopment.apkextractor.data.repository.packageArchive.PackageArchiveRepository
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

object PackageArchiveEntityToApkDetailModelMapper :
    Mapper<PackageArchiveEntity, ApkModel.ApkDetailModel> {
    override fun map(from: PackageArchiveEntity): ApkModel.ApkDetailModel {
        return ApkModel.ApkDetailModel(
            fileUri = from.fileUri,
            fileName = from.fileName,
            fileType = from.fileType,
            fileLastModified = from.fileLastModified,
            fileSize = from.fileSize,
            appName = from.appName,
            appPackageName = from.appPackageName,
            appIcon = from.appIcon,
            appVersionName = from.appVersionName,
            appVersionCode = from.appVersionCode,
            appMinSdkVersion = from.appMinSdkVersion,
            appTargetSdkVersion = from.appTargetSdkVersion,
            loaded = from.loaded,
        )
    }
}

object ApkDetailModelToPackageArchiveEntityMapper :
    Mapper<ApkModel.ApkDetailModel, PackageArchiveEntity> {
    override fun map(from: ApkModel.ApkDetailModel): PackageArchiveEntity {
        return PackageArchiveEntity(
            fileUri = from.fileUri,
            fileName = from.fileName,
            fileType = from.fileType,
            fileLastModified = from.fileLastModified,
            fileSize = from.fileSize,
            appName = from.appName,
            appPackageName = from.appPackageName,
            appIcon = from.appIcon,
            appVersionName = from.appVersionName,
            appVersionCode = from.appVersionCode,
            appMinSdkVersion = from.appMinSdkVersion,
            appTargetSdkVersion = from.appTargetSdkVersion,
            loaded = from.loaded,
        )
    }
}

object PackageArchiveEntityMapperToApkListModelMapper :
    Mapper<PackageArchiveEntity, ApkModel.ApkListModel> {
    override fun map(from: PackageArchiveEntity): ApkModel.ApkListModel {
        return ApkModel.ApkListModel(
            fileUri = from.fileUri,
            fileName = from.fileName,
            appName = from.appName,
            appPackageName = from.appPackageName,
            appIcon = from.appIcon,
            appVersionName = from.appVersionName,
            appVersionCode = from.appVersionCode,
            fileSize = from.fileSize
        )
    }
}

class ApkListModelToPackageArchiveEntityMapper(
    val packageArchiveRepository: PackageArchiveRepository
) : Mapper<ApkModel.ApkListModel, PackageArchiveEntity?> {
    override fun map(from: ApkModel.ApkListModel): PackageArchiveEntity? =
        runBlocking(Dispatchers.IO) {
            packageArchiveRepository.getApk(from.fileUri)
        }
}
