package domilopment.apkextractor.domain.usecase.apkList

import android.content.Context
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.dependencyInjection.packageArchive.PackageArchiveRepository
import domilopment.apkextractor.utils.FileUtil

interface DeleteApkUseCase {
    suspend operator fun invoke(apk: PackageArchiveEntity)
}

class DeleteApkUseCaseImpl(
    private val context: Context, private val apksRepository: PackageArchiveRepository
) : DeleteApkUseCase {
    override suspend fun invoke(apk: PackageArchiveEntity) {
        if (FileUtil.doesDocumentExist(context, apk.fileUri)) return

        apksRepository.removeApk(apk)
    }
}