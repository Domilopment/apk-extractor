package domilopment.apkextractor.data.apkList

import android.content.ContentResolver
import android.net.Uri
import java.io.File
import java.io.IOException

data class AppPackageArchiveFile(
    override val fileUri: Uri,
    override val fileName: String,
    override val fileType: String,
    override val fileLastModified: Long,
    override val fileSize: Long,
    private val cacheDir: File,
    private val contentResolver: ContentResolver,
) : PackageArchiveFile {
    override fun packageArchiveInfo(): File? {
        try {
            val apkFile = File.createTempFile("temp", ".apk", cacheDir)
            contentResolver.openInputStream(fileUri).use { input ->
                apkFile.outputStream().buffered().use {
                    input?.copyTo(it)
                    it.flush()
                }
            }
            return if (apkFile.length() == fileSize) apkFile else null
        } catch (_: IOException) {
            // No Space left on Device, ...
            return null
        }
    }

    override fun hashCode(): Int {
        var result = fileUri.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + fileLastModified.hashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + fileSize.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppPackageArchiveFile

        if (fileUri != other.fileUri) return false
        if (fileName != other.fileName) return false
        if (fileLastModified != other.fileLastModified) return false
        if (fileSize != other.fileSize) return false
        if (fileSize != other.fileSize) return false

        return true
    }
}
