package domilopment.apkextractor.data

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import domilopment.apkextractor.utils.Utils
import java.io.File
import java.io.IOException

data class PackageArchiveModel(
    val fileUri: Uri,
    val fileName: String?,
    val fileLastModified: Long,
    private val fileSizeLong: Long,
) : BaseObservable() {
    val fileSize = fileSizeLong / (1000.0F * 1000.0F)

    @get:Bindable
    var isPackageArchiveInfoLoaded = false
        private set(value) {
            field = value
            notifyPropertyChanged(BR.packageArchiveInfoLoaded)
        }

    @get:Bindable
    var isPackageArchiveInfoLoading = false
        private set(value) {
            field = value
            notifyPropertyChanged(BR.packageArchiveInfoLoading)
        }

    @get:Bindable
    var appName: CharSequence? = null
        private set(value) {
            field = value
            notifyPropertyChanged(BR.appName)
        }

    @get:Bindable
    var appPackageName: String? = null
        private set(value) {
            field = value
            notifyPropertyChanged(BR.appPackageName)
        }

    @get:Bindable
    var appIcon: Drawable? = null
        private set(value) {
            field = value
            notifyPropertyChanged(BR.appIcon)
        }

    @get:Bindable
    var appVersionName: String? = null
        private set(value) {
            field = value
            notifyPropertyChanged(BR.appVersionName)
        }

    @get:Bindable
    var appVersionCode: Long? = null
        private set(value) {
            field = value
            notifyPropertyChanged(BR.appVersionCode)
        }

    fun loadPackageArchiveInfo(context: Context) {
        if (isPackageArchiveInfoLoaded || isPackageArchiveInfoLoading) return
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
        loadPackageArchiveInfo(context)
    }
}
