package domilopment.apkextractor.data

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import domilopment.apkextractor.utils.Utils
import java.io.File

data class PackageArchiveModel(
    private val context: Context, private val documentFile: DocumentFile
) {
    private val packageManager = context.packageManager

    val fileUri: Uri = documentFile.uri
    val fileName: String? = documentFile.name
    val fileLastModified: Long = documentFile.lastModified()
    val fileSize: Float = documentFile.length() / (1000.0F * 1000.0F)

    val appName: CharSequence?
    val appPackageName: String?
    val appIcon: Drawable?
    val appVersionName: String?
    val appVersionCode: Long?

    init {
        val apkFile = File(context.cacheDir, documentFile.name ?: "temp.apk").also { emptyFile ->
            if (!emptyFile.exists()) emptyFile.createNewFile()
            context.contentResolver.openInputStream(documentFile.uri).use {
                it?.copyTo(emptyFile.outputStream())
            }
        }
        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.getPackageArchiveInfo(
            apkFile.path, PackageManager.PackageInfoFlags.of(0L)
        )
        else packageManager.getPackageArchiveInfo(apkFile.path, 0)).also {
            appName = it?.applicationInfo?.loadLabel(packageManager)
            appPackageName = it?.applicationInfo?.packageName
            appIcon = it?.applicationInfo?.loadIcon(packageManager)
            appVersionName = it?.versionName
            appVersionCode = it?.let { Utils.versionCode(it) }
        }
        if (apkFile.exists()) apkFile.delete()
    }

    var isChecked = false

    fun exist() = documentFile.exists()
}
