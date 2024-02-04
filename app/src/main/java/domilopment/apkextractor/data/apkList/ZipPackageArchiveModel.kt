package domilopment.apkextractor.data.apkList

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import domilopment.apkextractor.utils.Utils
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class ZipPackageArchiveModel(
    override val fileUri: Uri,
    override val fileName: String,
    override val fileLastModified: Long,
    private val fileSizeLong: Long,
    override var appName: CharSequence? = null,
    override var appPackageName: String? = null,
    override var appIcon: Drawable? = null,
    override var appVersionName: String? = null,
    override var appVersionCode: Long? = null,
    override var isPackageArchiveInfoLoading: Boolean = false,
    override var isPackageArchiveInfoLoaded: Boolean = false
) : PackageArchiveModel {
    override val fileSize: Float = fileSizeLong / (1000.0F * 1000.0F)

    override fun packageArchiveInfo(context: Context): ZipPackageArchiveModel {
        if (isPackageArchiveInfoLoading || isPackageArchiveInfoLoaded) return this
        isPackageArchiveInfoLoading = true
        var returnApp =
            copy(isPackageArchiveInfoLoading = false, isPackageArchiveInfoLoaded = false)

        val packageManager: PackageManager = context.packageManager

        var apkFile: File? = null
        try {
            apkFile = File.createTempFile("temp", ".apk", context.cacheDir)
            ZipInputStream(context.contentResolver.openInputStream(fileUri)).use { input ->
                var baseApk: ByteArray? = null
                var entry: ZipEntry?
                while (run { entry = input.nextEntry; entry } != null) {
                    if (entry?.name == "base.apk") {
                        baseApk = input.readBytes()
                        break
                    }
                }

                if (baseApk != null && baseApk.inputStream()
                        .copyTo(apkFile.outputStream()) == baseApk.size.toLong()
                ) {
                    val archiveInfo =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.getPackageArchiveInfo(
                            apkFile.path, PackageManager.PackageInfoFlags.of(0L)
                        ) else packageManager.getPackageArchiveInfo(apkFile.path, 0)
                    returnApp = archiveInfo?.let {
                        it.applicationInfo.sourceDir = apkFile.path
                        it.applicationInfo.publicSourceDir = apkFile.path
                        copy(
                            appName = it.applicationInfo.loadLabel(packageManager),
                            appPackageName = it.applicationInfo.packageName,
                            appIcon = it.applicationInfo.loadIcon(packageManager),
                            appVersionName = it.versionName,
                            appVersionCode = Utils.versionCode(it),
                            isPackageArchiveInfoLoading = false,
                            isPackageArchiveInfoLoaded = true
                        )
                    } ?: returnApp
                }
            }
            isPackageArchiveInfoLoaded = true
        } catch (e: IOException) {
            // No Space left on Device, ...
        } catch (oom_e: OutOfMemoryError) {
            // Prevent crash if memory run out
        } finally {
            isPackageArchiveInfoLoading = false
            if (apkFile != null && apkFile.exists()) apkFile.delete()
        }
        return returnApp
    }

    override fun forceRefresh(context: Context): ZipPackageArchiveModel {
        isPackageArchiveInfoLoaded = false
        return packageArchiveInfo(context)
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

        other as ZipPackageArchiveModel

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
