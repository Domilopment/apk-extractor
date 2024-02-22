package domilopment.apkextractor.data.apkList

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.settings.PackageArchiveUtils
import java.io.File
import java.io.IOException

data class AppPackageArchiveModel(
    override val fileUri: Uri,
    override val fileName: String,
    override val fileType: String,
    override val fileLastModified: Long,
    override val fileSize: Long,
    override var appName: CharSequence? = null,
    override var appPackageName: String? = null,
    override var appIcon: Drawable? = null,
    override var appVersionName: String? = null,
    override var appVersionCode: Long? = null,
    override var appMinSdkVersion: Int? = null,
    override var appTargetSdkVersion: Int? = null,
    override var isPackageArchiveInfoLoading: Boolean = false,
    override var isPackageArchiveInfoLoaded: Boolean = false,
) : PackageArchiveModel {
    override fun packageArchiveInfo(context: Context): AppPackageArchiveModel {
        if (isPackageArchiveInfoLoading || isPackageArchiveInfoLoaded) return this
        isPackageArchiveInfoLoading = true
        var returnApp =
            copy(isPackageArchiveInfoLoading = false, isPackageArchiveInfoLoaded = false)

        val packageManager: PackageManager = context.packageManager

        var apkFile: File? = null
        try {
            apkFile = File.createTempFile("temp", ".apk", context.cacheDir)
            context.contentResolver.openInputStream(fileUri).use { input ->
                if (input?.copyTo(apkFile.outputStream()) == fileSize) {
                    returnApp =
                        PackageArchiveUtils.getPackageInfoFromApkFile(packageManager, apkFile)
                            ?.let {
                                copy(
                                    appName = it.applicationInfo.loadLabel(packageManager),
                                    appPackageName = it.applicationInfo.packageName,
                                    appIcon = it.applicationInfo.loadIcon(packageManager),
                                    appVersionName = it.versionName,
                                    appVersionCode = Utils.versionCode(it),
                                    appMinSdkVersion = it.applicationInfo.minSdkVersion,
                                    appTargetSdkVersion = it.applicationInfo.targetSdkVersion,
                                    isPackageArchiveInfoLoading = false,
                                    isPackageArchiveInfoLoaded = true
                                )
                            } ?: returnApp
                }
            }
            return returnApp
        } catch (_: IOException) {
            // No Space left on Device, ...
            return returnApp
        } finally {
            isPackageArchiveInfoLoading = false
            if (apkFile != null && apkFile.exists()) apkFile.delete()
        }
    }

    override fun forceRefresh(context: Context): AppPackageArchiveModel {
        isPackageArchiveInfoLoaded = false
        return packageArchiveInfo(context)
    }

    override fun hashCode(): Int {
        var result = fileUri.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + fileLastModified.hashCode()
        result = 31 * result + fileSize.hashCode()
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

        other as AppPackageArchiveModel

        if (fileUri != other.fileUri) return false
        if (fileName != other.fileName) return false
        if (fileLastModified != other.fileLastModified) return false
        if (fileSize != other.fileSize) return false
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
