package domilopment.apkextractor.data

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import domilopment.apkextractor.utils.Utils
import java.io.File
import java.lang.Exception

private const val placeholder: String = "Failed to load"

data class PackageArchiveModel(
    private val context: Context, private val documentFile: DocumentFile
) {
    private val packageManager = context.packageManager

    val fileUri: Uri = documentFile.uri
    val fileName: String? = documentFile.name
    val fileLastModified: Long = documentFile.lastModified()
    val fileSize: Float = documentFile.length() / (1000.0F * 1000.0F)

    var appName: CharSequence = placeholder
        private set
    var appPackageName: String = placeholder
        private set
    var appIcon: Drawable? = ContextCompat.getDrawable(
        context, android.R.drawable.sym_def_app_icon
    )
        private set
    var appVersionName: String? = null
        private set
    var appVersionCode: Long = -1
        private set

    init {
        val apkFile = File.createTempFile(
            documentFile.name?.removeSuffix(".apk") ?: "temp", ".apk", context.cacheDir
        )
        try {
            context.contentResolver.openInputStream(documentFile.uri).use { input ->
                if (input?.copyTo(apkFile.outputStream()) == documentFile.length()) {
                    val archiveInfo =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.getPackageArchiveInfo(
                            apkFile.path, PackageManager.PackageInfoFlags.of(0L)
                        ) else packageManager.getPackageArchiveInfo(apkFile.path, 0)
                    archiveInfo?.also {
                        appName = it.applicationInfo?.loadLabel(packageManager) ?: placeholder
                        appPackageName = it.applicationInfo?.packageName ?: placeholder
                        appIcon = it.applicationInfo?.loadIcon(packageManager)
                        appVersionName = it.versionName
                        appVersionCode = it.let { Utils.versionCode(it) }
                    }
                }
            }
        } catch (_: Exception) {
            // No Space left on Device, ...
        }
        if (apkFile.exists()) apkFile.delete()
    }

    var isChecked = false

    fun exist() = documentFile.exists()
}
