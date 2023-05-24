package domilopment.apkextractor.utils

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import domilopment.apkextractor.data.PackageArchiveModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ListOfAPKs(private val context: Context) {
    /**
     * Update Installed APK lists
     */
    suspend fun apkFiles(): List<PackageArchiveModel> = coroutineScope {
        val jobList = ArrayList<Deferred<PackageArchiveModel>>()

        SettingsManager(context).saveDir()?.let {
            val dir = DocumentFile.fromTreeUri(context, it)
            if (dir != null && dir.exists() && dir.isDirectory) dir else null
        }?.listFiles()?.filter {
            it.type == FileUtil.MIME_TYPE
        }?.forEach { documentFile: DocumentFile ->
            jobList.add(async(Dispatchers.IO) {
                PackageArchiveModel(
                    context.packageManager, context.contentResolver, context.cacheDir, documentFile
                )
            })

        }
        return@coroutineScope jobList.awaitAll()
    }
}