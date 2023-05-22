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

data class PackageArchiveModel(
    private val packageManager: PackageManager,
    private val contentResolver: ContentResolver,
    private val cacheDir: File,
    private val documentFile: DocumentFile,
) {
    val fileUri: Uri = documentFile.uri
    val fileName: String? = documentFile.name
    val fileLastModified: Long = documentFile.lastModified()
    val fileSize: Float = documentFile.length() / (1000.0F * 1000.0F)

    private var isPackageArchiveInfoLoaded = false
    var isPackageArchiveInfoLoading = false
        private set

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

    var isChecked = false

    fun exist() = documentFile.exists()

    fun loadPackageArchiveInfo() {
        if (isPackageArchiveInfoLoaded || isPackageArchiveInfoLoading) return
        isPackageArchiveInfoLoading = true

        var apkFile: File? = null
        try {
            apkFile = File.createTempFile("temp", ".apk", cacheDir)
            contentResolver.openInputStream(documentFile.uri).use { input ->
                if (input?.copyTo(apkFile.outputStream()) == documentFile.length()) {
                    val archiveInfo =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.getPackageArchiveInfo(
                            apkFile.path, PackageManager.PackageInfoFlags.of(0L)
                        ) else packageManager.getPackageArchiveInfo(apkFile.path, 0)
                    archiveInfo?.also {
                        it.applicationInfo.sourceDir = apkFile.path
                        it.applicationInfo.publicSourceDir = apkFile.path
                        appName = it.applicationInfo.loadLabel(packageManager)
                        appPackageName = it.applicationInfo.packageName
                        appIcon = it.applicationInfo.loadIcon(packageManager)
                        appVersionName = it.versionName
                        appVersionCode = Utils.versionCode(it)
                    }
                }
            }
            isPackageArchiveInfoLoaded = true
        } catch (_: IOException) {
            // No Space left on Device, ...
        } finally {
            isPackageArchiveInfoLoading = false
            if (apkFile != null && apkFile.exists()) apkFile.delete()
        }
    }
}
