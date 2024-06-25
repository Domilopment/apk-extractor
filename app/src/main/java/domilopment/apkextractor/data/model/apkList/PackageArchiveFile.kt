package domilopment.apkextractor.data.model.apkList

import android.net.Uri
import java.io.File

interface PackageArchiveFile {
    val fileUri: Uri
    val fileName: String
    val fileType: String
    val fileLastModified: Long
    val fileSize: Long
    fun packageArchiveInfo(): File?
}