package domilopment.apkextractor.domain.usecase.apkList

import domilopment.apkextractor.data.model.apkList.ApkModel
import domilopment.apkextractor.data.repository.packageArchive.PackageArchiveRepository
import domilopment.apkextractor.domain.mapper.ApkDetailModelToPackageArchiveEntityMapper

interface LoadApkInfoUseCase {
    suspend operator fun invoke(apk: ApkModel.ApkDetailModel)
}

class LoadApkInfoUseCaseImpl(private val apkRepository: PackageArchiveRepository) : LoadApkInfoUseCase {
    override suspend operator fun invoke(apk: ApkModel.ApkDetailModel) {
        val packageArchive = ApkDetailModelToPackageArchiveEntityMapper.map(apk)
        return apkRepository.updateApp(packageArchive)
    }
}