package domilopment.apkextractor.dependencyInjection.files

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.domain.mapper.PackageArchiveModelToPackageArchiveEntityMapper
import domilopment.apkextractor.utils.SaveApkResult
import javax.inject.Inject

interface FilesRepository {
    suspend fun save(
        data: List<String>, saveDir: Uri, saveName: String, progressCallback: (String) -> Unit
    ): SaveApkResult

    suspend fun delete(data: Uri): Boolean

    suspend fun fileInfo(file: Uri, vararg projection: String): PackageArchiveEntity?
}

class FilesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val filesService: FilesService
) : FilesRepository {
    override suspend fun save(
        data: List<String>, saveDir: Uri, saveName: String, progressCallback: (String) -> Unit
    ): SaveApkResult {
        return filesService.save(data, saveDir, saveName, progressCallback)
    }

    override suspend fun delete(data: Uri): Boolean {
        return filesService.delete(data)
    }

    override suspend fun fileInfo(file: Uri, vararg projection: String): PackageArchiveEntity? {
        return filesService.fileInfo(file, *projection)
            ?.let { PackageArchiveModelToPackageArchiveEntityMapper(context.packageManager).map(it) }
    }
}