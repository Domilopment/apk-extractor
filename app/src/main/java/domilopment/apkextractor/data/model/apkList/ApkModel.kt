package domilopment.apkextractor.data.model.apkList

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap

sealed interface ApkModel {
    val fileUri: Uri
    val fileName: String
    val appName: String?
    val appPackageName: String?
    val appIcon: ImageBitmap?
    val appVersionName: String?
    val appVersionCode: Long?
    val fileSize: Long

    data class ApkDetailModel(
        override val fileUri: Uri,
        override val fileName: String,
        val fileType: String,
        val fileLastModified: Long,
        override val fileSize: Long,
        override val appName: String?,
        override val appPackageName: String?,
        override val appIcon: ImageBitmap?,
        override val appVersionName: String?,
        override val appVersionCode: Long?,
        val appMinSdkVersion: Int?,
        val appTargetSdkVersion: Int?,
        val loaded: Boolean = false,
        ) : ApkModel

    data class ApkListModel(
        override val fileUri: Uri,
        override val fileName: String,
        override val appName: String?,
        override val appPackageName: String?,
        override val appIcon: ImageBitmap?,
        override val appVersionName: String?,
        override val appVersionCode: Long?,
        override val fileSize: Long,
    ) : ApkModel
}
