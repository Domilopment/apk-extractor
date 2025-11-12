package domilopment.apkextractor.utils.settings

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.utils.FileUtil
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object PackageArchiveUtils {
    /**
     * Sorts Data by user selected Order
     * @param data Unsorted List of APKs
     * @return Sorted List of APKs
     */
    fun sortApkData(
        data: List<PackageArchiveEntity>, sortMode: ApkSortOptions
    ): List<PackageArchiveEntity> {
        return data.sortedWith(sortMode.comparator)
    }

    fun getPackageInfoFromApkFile(packageManager: PackageManager, apkFile: File): PackageInfo? {
        val archiveInfo =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.getPackageArchiveInfo(
                apkFile.path, PackageManager.PackageInfoFlags.of(0L)
            ) else packageManager.getPackageArchiveInfo(apkFile.path, 0)
        return archiveInfo?.apply {
            applicationInfo?.sourceDir = apkFile.path
            applicationInfo?.publicSourceDir = apkFile.path
        }
    }

    @Throws(SecurityException::class)
    fun getApkFileFromDocument(
        context: Context,
        fileUri: Uri,
        fileType: String,
    ): File? {
        try {
            val apkFile = File.createTempFile("temp", ".apk", context.cacheDir)
            val inputStream = context.contentResolver.openInputStream(fileUri)?.buffered()
            val outputStream = apkFile.outputStream().buffered()
            when (fileType) {
                FileUtil.FileInfo.APK.mimeType -> inputStream?.let {
                    getApkFromSingleFile(
                        it, outputStream
                    )
                }

                FileUtil.FileInfo.APKS.mimeType, FileUtil.FileInfo.XAPK.mimeType -> inputStream?.let {
                    getApkFromZipFile(
                        it, outputStream
                    )
                }
            }
            return if (apkFile.length() != 0L) apkFile else null
        } catch (_: IOException) {
            // No Space left on Device, ...
            return null
        } catch (_: FileNotFoundException) {
            // File seems to not exist
            return null
        } catch (_: IllegalStateException) {
            // File could not be found
            return null
        }
    }

    private fun getApkFromSingleFile(inputStream: InputStream, outputStream: OutputStream) {
        inputStream.use { input ->
            outputStream.use {
                input.copyTo(it)
                it.flush()
            }
        }
    }

    private fun getApkFromZipFile(inputStream: InputStream, outputStream: OutputStream) {
        ZipInputStream(inputStream).use { input ->
            var entry: ZipEntry?
            while (run { entry = input.nextEntry; entry } != null) {
                if (entry?.name == "base.apk") {
                    outputStream.use {
                        input.copyTo(it)
                        it.flush()
                    }
                    input.closeEntry()
                    break
                }
                input.closeEntry()
            }
        }
    }
}