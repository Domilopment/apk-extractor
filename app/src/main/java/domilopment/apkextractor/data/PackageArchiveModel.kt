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

    var appName: CharSequence? = null
        private set
    var appPackageName: String? = null
        private set
    var appIcon: Drawable? = null
        private set
    var appVersionName: String? = null
        private set
    var appVersionCode: Long? = null
        private set

    init {
        File(context.cacheDir, documentFile.name ?: "temp.apk").also { apkFile ->
            if (!apkFile.exists()) apkFile.createNewFile()
            context.contentResolver.openInputStream(documentFile.uri).use {
                if (it?.copyTo(apkFile.outputStream())!! == documentFile.length()) {
                    val archiveInfo =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.getPackageArchiveInfo(
                            apkFile.path, PackageManager.PackageInfoFlags.of(0L)
                        ) else packageManager.getPackageArchiveInfo(apkFile.path, 0)
                    appName = archiveInfo?.applicationInfo?.loadLabel(packageManager)
                    appPackageName = archiveInfo?.applicationInfo?.packageName
                    appIcon = archiveInfo?.applicationInfo?.loadIcon(packageManager)
                    appVersionName = archiveInfo?.versionName
                    appVersionCode = archiveInfo?.let { Utils.versionCode(it) }
                }
            }
            if (apkFile.exists()) apkFile.delete()
        }
    }

    var isChecked = false

    fun exist() = documentFile.exists()
}
