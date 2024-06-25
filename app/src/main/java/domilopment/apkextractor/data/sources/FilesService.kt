package domilopment.apkextractor.data.sources

import android.content.Context
import android.net.Uri
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.SaveApkResult
import domilopment.apkextractor.utils.settings.ApplicationUtil

class FilesService(private val context: Context) {
    suspend fun save(
        splits: List<String>,
        saveDir: Uri,
        saveName: String,
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
            context, splits.toTypedArray(), saveDir, saveName
        ) {
            progressCallback(it)
        }
    }

    suspend fun delete(data: Uri): Boolean {
        return FileUtil.deleteDocument(context, data)
    }

    suspend fun fileInfo(file: Uri, vararg projection: String): FileUtil.DocumentFile? {
        return FileUtil.getDocumentInfo(context, file, *projection)
    }
}