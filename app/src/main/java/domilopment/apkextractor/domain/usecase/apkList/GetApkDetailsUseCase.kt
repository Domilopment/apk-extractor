package domilopment.apkextractor.domain.usecase.apkList

import android.net.Uri
import domilopment.apkextractor.data.model.apkList.ApkModel
import domilopment.apkextractor.data.repository.packageArchive.PackageArchiveRepository
import domilopment.apkextractor.domain.mapper.PackageArchiveEntityToApkDetailModelMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface GetApkDetailsUseCase {
    operator fun invoke(fileUri: Uri): Flow<ApkModel.ApkDetailModel?>
    suspend fun single(fileUri: Uri): ApkModel.ApkDetailModel? 
}

class GetApkDetailsUseCaseImpl(
    private val apksRepository: PackageArchiveRepository,
) : GetApkDetailsUseCase {
    override fun invoke(fileUri: Uri): Flow<ApkModel.ApkDetailModel?> =
        apksRepository.getApkAsFlow(fileUri).map {
            it?.let { PackageArchiveEntityToApkDetailModelMapper.map(it) }
        }
    
    override suspend fun single(fileUri: Uri): ApkModel.ApkDetailModel? =
        apksRepository.getApk(fileUri)?.let { 
            PackageArchiveEntityToApkDetailModelMapper.map(it)
        }
}
