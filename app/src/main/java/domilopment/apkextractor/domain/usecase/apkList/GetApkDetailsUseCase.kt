package domilopment.apkextractor.domain.usecase.apkList

import android.net.Uri
import domilopment.apkextractor.data.repository.packageArchive.PackageArchiveRepository
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import kotlinx.coroutines.flow.Flow

interface GetApkDetailsUseCase {
    operator fun invoke(fileUri: Uri): Flow<PackageArchiveEntity?>
}

class GetApkDetailsUseCaseImpl(
    private val apksRepository: PackageArchiveRepository,
) : GetApkDetailsUseCase {
    override fun invoke(fileUri: Uri): Flow<PackageArchiveEntity?> = apksRepository.getApk(fileUri)
}