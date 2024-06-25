package domilopment.apkextractor.data.repository.packageArchive

import android.content.Context
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.settings.PackageArchiveUtils

object PackageArchiveDetailLoader {
    fun load(context: Context, model: PackageArchiveEntity): PackageArchiveEntity {
        val apkFile =
            PackageArchiveUtils.getApkFileFromDocument(context, model.fileUri, model.fileType)
        return apkFile?.let {
            PackageArchiveUtils.getPackageInfoFromApkFile(
                context.packageManager, it
            )
        }?.let { apk ->
            val load = model.copy(
                appName = apk.applicationInfo.loadLabel(context.packageManager).toString(),
                appPackageName = apk.applicationInfo.packageName,
                appIcon = apk.applicationInfo.loadIcon(context.packageManager)?.toBitmap()
                    ?.asImageBitmap(),
                appVersionName = apk.versionName,
                appVersionCode = Utils.versionCode(apk),
                appMinSdkVersion = apk.applicationInfo.minSdkVersion,
                appTargetSdkVersion = apk.applicationInfo.targetSdkVersion,
                loaded = true
            )
            apkFile.delete()
            load
        } ?: model
    }

    fun load(context: Context, file: FileUtil.DocumentFile): PackageArchiveEntity {
        val apkFile = PackageArchiveUtils.getApkFileFromDocument(context, file.uri, file.mimeType!!)
        return apkFile?.let {
            PackageArchiveUtils.getPackageInfoFromApkFile(
                context.packageManager, it
            )
        }?.let { apk ->
            val load = PackageArchiveEntity(
                fileUri = file.uri,
                fileName = file.displayName!!,
                fileType = file.mimeType,
                fileLastModified = file.lastModified!!,
                fileSize = file.size!!,
                appName = apk.applicationInfo.loadLabel(context.packageManager).toString(),
                appPackageName = apk.applicationInfo.packageName,
                appIcon = apk.applicationInfo.loadIcon(context.packageManager)?.toBitmap()
                    ?.asImageBitmap(),
                appVersionName = apk.versionName,
                appVersionCode = Utils.versionCode(apk),
                appMinSdkVersion = apk.applicationInfo.minSdkVersion,
                appTargetSdkVersion = apk.applicationInfo.targetSdkVersion,
                loaded = true
            )
            apkFile.delete()
            load
        } ?: PackageArchiveEntity(
            fileUri = file.uri,
            fileName = file.displayName!!,
            fileType = file.mimeType,
            fileLastModified = file.lastModified!!,
            fileSize = file.size!!
        )
    }
}
