package domilopment.apkextractor.data.repository.files

import android.net.Uri
import domilopment.apkextractor.data.sources.FilesService
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.SaveApkResult
import javax.inject.Inject

interface FilesRepository {
    suspend fun save(
        data: List<String>,
        saveDir: Uri,
        saveName: String,
        mimeType: String,
        suffix: String,
        progressCallback: (String) -> Unit
    ): SaveApkResult

    suspend fun delete(data: Uri): Boolean

    suspend fun fileInfo(file: Uri, vararg projection: String): FileUtil.DocumentFile?
}

class FilesRepositoryImpl @Inject constructor(
    private val filesService: FilesService
) : FilesRepository {
    override suspend fun save(
        data: List<String>,
        saveDir: Uri,
        saveName: String,
        mimeType: String,
        suffix: String,
        progressCallback: (String) -> Unit
    ): SaveApkResult {
        return filesService.save(data, saveDir, saveName, mimeType, suffix, progressCallback)
    }

    override suspend fun delete(data: Uri): Boolean {
        return filesService.delete(data)
    }

    override suspend fun fileInfo(file: Uri, vararg projection: String): FileUtil.DocumentFile? {
        return filesService.fileInfo(file, *projection)
    }
}