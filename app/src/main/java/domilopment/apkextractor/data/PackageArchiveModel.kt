package domilopment.apkextractor.data

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import domilopment.apkextractor.utils.Utils
import java.io.File
import java.io.IOException

data class PackageArchiveModel(
    val fileUri: Uri,
    val fileName: String,
    val fileLastModified: Long,
    private val fileSizeLong: Long,
    var appName: CharSequence? = null,
    var appPackageName: String? = null,
    var appIcon: Drawable? = null,
    var appVersionName: String? = null,
    var appVersionCode: Long? = null,
) {
    val fileSize: Float = fileSizeLong / (1000.0F * 1000.0F)
    var isPackageArchiveInfoLoading: Boolean = false
        private set
    var isPackageArchiveInfoLoaded: Boolean = false
        private set

    fun packageArchiveInfo(context: Context) {
        if (isPackageArchiveInfoLoading || isPackageArchiveInfoLoaded) return
        isPackageArchiveInfoLoading = true

        val packageManager: PackageManager = context.packageManager

        var apkFile: File? = null
        try {
            apkFile = File.createTempFile("temp", ".apk", context.cacheDir)
            context.contentResolver.openInputStream(fileUri).use { input ->
                if (input?.copyTo(apkFile.outputStream()) == fileSizeLong) {
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

    fun forceRefresh(context: Context) {
        isPackageArchiveInfoLoaded = false
        packageArchiveInfo(context)
    }

    override fun hashCode(): Int {
        var result = fileUri.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + fileLastModified.hashCode()
        result = 31 * result + fileSizeLong.hashCode()
        result = 31 * result + (appName?.hashCode() ?: 0)
        result = 31 * result + (appPackageName?.hashCode() ?: 0)
        result = 31 * result + (appIcon?.hashCode() ?: 0)
        result = 31 * result + (appVersionName?.hashCode() ?: 0)
        result = 31 * result + (appVersionCode?.hashCode() ?: 0)
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + isPackageArchiveInfoLoading.hashCode()
        result = 31 * result + isPackageArchiveInfoLoaded.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PackageArchiveModel

        if (fileUri != other.fileUri) return false
        if (fileName != other.fileName) return false
        if (fileLastModified != other.fileLastModified) return false
        if (fileSizeLong != other.fileSizeLong) return false
        if (appName != other.appName) return false
        if (appPackageName != other.appPackageName) return false
        if (appIcon != other.appIcon) return false
        if (appVersionName != other.appVersionName) return false
        if (appVersionCode != other.appVersionCode) return false
        if (fileSize != other.fileSize) return false
        if (isPackageArchiveInfoLoading != other.isPackageArchiveInfoLoading) return false
        if (isPackageArchiveInfoLoaded != other.isPackageArchiveInfoLoaded) return false

        return true
    }
}
