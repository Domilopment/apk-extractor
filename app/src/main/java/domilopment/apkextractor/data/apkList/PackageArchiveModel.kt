package domilopment.apkextractor.data.apkList

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri

interface PackageArchiveModel {
    val fileUri: Uri
    val fileName: String
    val fileType: String
    val fileLastModified: Long
    val fileSize: Float
    var appName: CharSequence?
    var appPackageName: String?
    var appIcon: Drawable?
    var appVersionName: String?
    var appVersionCode: Long?
    var isPackageArchiveInfoLoading: Boolean
    var isPackageArchiveInfoLoaded: Boolean
    fun packageArchiveInfo(context: Context): PackageArchiveModel
    fun forceRefresh(context: Context): PackageArchiveModel
}