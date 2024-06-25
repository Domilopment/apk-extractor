package domilopment.apkextractor.dependencyInjection.files

import android.content.Context
import android.net.Uri
import domilopment.apkextractor.data.apkList.AppPackageArchiveFile
import domilopment.apkextractor.data.apkList.PackageArchiveFile
import domilopment.apkextractor.data.apkList.ZipPackageArchiveFile
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

    suspend fun fileInfo(file: Uri, vararg projection: String): PackageArchiveFile? {
        return FileUtil.getDocumentInfo(context, file, *projection)?.let {
            when {
                it.displayName!!.endsWith(".apk") -> AppPackageArchiveFile(
                    it.uri,
                    it.displayName,
                    it.mimeType!!,
                    it.lastModified!!,
                    it.size!!,
                    context.cacheDir,
                    context.contentResolver
                )

                it.displayName.endsWith(".xapk") || it.displayName.endsWith(".apks") -> ZipPackageArchiveFile(
                    it.uri,
                    it.displayName,
                    it.mimeType!!,
                    it.lastModified!!,
                    it.size!!,
                    context.cacheDir,
                    context.contentResolver
                )

                else -> null
            }
        }
    }
}