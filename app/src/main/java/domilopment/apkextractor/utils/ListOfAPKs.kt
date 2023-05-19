package domilopment.apkextractor.utils

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import domilopment.apkextractor.R
import domilopment.apkextractor.data.PackageArchiveModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ListOfAPKs(private val context: Context) {
    private val placeholder = context.getString(R.string.apk_holder_placeholder)
    private val icon = ContextCompat.getDrawable(
        context, android.R.drawable.sym_def_app_icon
    )

    /**
     * Update Installed APK lists
     */
    suspend fun apkFiles(): List<PackageArchiveModel> = coroutineScope {
        val jobList = ArrayList<Deferred<PackageArchiveModel>>()

        SettingsManager(context).saveDir()?.let {
            val dir = DocumentFile.fromTreeUri(context, it)
            if (dir != null && dir.exists() && dir.isDirectory) dir else null
        }?.listFiles()?.filter {
            it.type == FileHelper.MIME_TYPE
        }?.forEach { documentFile: DocumentFile ->
            jobList.add(async(Dispatchers.IO) {
                PackageArchiveModel(
                    context.packageManager,
                    context.contentResolver,
                    context.cacheDir,
                    documentFile,
                    appName = placeholder,
                    appPackageName = placeholder,
                    appIcon = icon,
                )
            })

        }
        return@coroutineScope jobList.awaitAll()
    }
}