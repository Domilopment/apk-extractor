package domilopment.apkextractor.data.sources

import android.content.Context
import android.net.Uri
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.SaveApkResult
import domilopment.apkextractor.utils.settings.ApplicationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FilesService(private val context: Context) {
    suspend fun save(
        splits: List<String>,
        saveDir: Uri,
        saveName: String,
        mimeType: String,
        suffix: String,
        progressCallback: suspend (String) -> Unit
    ): SaveApkResult {
        return if (splits.size == 1) {
            val file = splits.first()
            val newApk = ApplicationUtil.saveApk(
                context, file, saveDir, saveName
            )
            progressCallback(file)
            newApk
        } else ApplicationUtil.saveXapk(
            context, splits.toTypedArray(), saveDir, saveName, mimeType, suffix
        ) {
            progressCallback(it)
        }
    }

    suspend fun delete(data: Uri): Boolean = withContext(Dispatchers.IO) {
        return@withContext FileUtil.deleteDocument(context, data)
    }

    suspend fun fileInfo(file: Uri, vararg projection: String): FileUtil.DocumentFile? =
        withContext(Dispatchers.IO) {
            return@withContext FileUtil.getDocumentInfo(context, file, *projection)
        }
}