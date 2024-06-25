package domilopment.apkextractor.domain.usecase.apkList

import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.data.repository.packageArchive.PackageArchiveRepository

interface LoadApkInfoUseCase {
    suspend operator fun invoke(apk: PackageArchiveEntity)
}

class LoadApkInfoUseCaseImpl(private val apkRepository: PackageArchiveRepository) : LoadApkInfoUseCase {
    override suspend operator fun invoke(apk: PackageArchiveEntity) {
        return apkRepository.updateApp(apk)
    }
}