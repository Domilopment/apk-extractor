package domilopment.apkextractor.data

import android.content.ContentResolver
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import domilopment.apkextractor.utils.Utils
import java.io.File
import java.io.IOException

private const val placeholder: String = "Loading..."
private const val loadFailed: String = "Loading failed"

data class PackageArchiveModel(
    private val packageManager: PackageManager,
    private val contentResolver: ContentResolver,
    private val cacheDir: File,
    private val documentFile: DocumentFile,
    var appName: CharSequence = placeholder,
    var appPackageName: String = placeholder,
    var appIcon: Drawable? = null,
    var appVersionName: String? = null,
    var appVersionCode: Long = -1
) {
    val fileUri: Uri = documentFile.uri
    val fileName: String? = documentFile.name
    val fileLastModified: Long = documentFile.lastModified()
    val fileSize: Float = documentFile.length() / (1000.0F * 1000.0F)

    private var isPackageArchiveInfoLoaded = false

    var isChecked = false

    fun exist() = documentFile.exists()

    fun loadPackageArchiveInfo() {
        if (isPackageArchiveInfoLoaded) return

        var apkFile: File? = null
        try {
            apkFile = File.createTempFile(
                documentFile.name?.removeSuffix(".apk") ?: "temp", ".apk", cacheDir
            )
            contentResolver.openInputStream(documentFile.uri).use { input ->
                if (input?.copyTo(apkFile.outputStream()) == documentFile.length()) {
                    val archiveInfo =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.getPackageArchiveInfo(
                            apkFile.path, PackageManager.PackageInfoFlags.of(0L)
                        ) else packageManager.getPackageArchiveInfo(apkFile.path, 0)
                    archiveInfo?.also {
                        it.applicationInfo.sourceDir = apkFile.path
                        it.applicationInfo.publicSourceDir = apkFile.path
                        appName = it.applicationInfo?.loadLabel(packageManager) ?: loadFailed
                        appPackageName = it.applicationInfo?.packageName ?: loadFailed
                        appIcon = it.applicationInfo?.loadIcon(packageManager)
                        appVersionName = it.versionName
                        appVersionCode = Utils.versionCode(it)
                    }
                }
            }
            isPackageArchiveInfoLoaded = true
        } catch (_: IOException) {
            // No Space left on Device, ...
        } finally {
            if (apkFile != null && apkFile.exists()) apkFile.delete()
        }
    }
}
