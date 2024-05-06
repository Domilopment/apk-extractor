package domilopment.apkextractor.domain.usecase.apkList

import domilopment.apkextractor.dependencyInjection.packageArchive.PackageArchiveRepository

interface UpdateApksUseCase {
    suspend operator fun invoke()
}

class UpdateApksUseCaseImpl(private val apksRepository: PackageArchiveRepository): UpdateApksUseCase {
    override suspend fun invoke() {
        apksRepository.updateApps()
    }
}