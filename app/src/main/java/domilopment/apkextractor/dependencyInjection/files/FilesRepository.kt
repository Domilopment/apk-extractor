package domilopment.apkextractor.dependencyInjection.files

import android.net.Uri
import domilopment.apkextractor.data.apkList.PackageArchiveModel
import domilopment.apkextractor.utils.SaveApkResult
import javax.inject.Inject

interface FilesRepository {
    suspend fun save(
        data: List<String>, saveDir: Uri, saveName: String, progressCallback: (String) -> Unit
    ): SaveApkResult

    suspend fun delete(data: Uri): Boolean

    suspend fun fileInfo(file: Uri, vararg projection: String): PackageArchiveModel?
}

class FilesRepositoryImpl @Inject constructor(
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

    override suspend fun fileInfo(file: Uri, vararg projection: String): PackageArchiveModel? {
        return filesService.fileInfo(file, *projection)
    }
}