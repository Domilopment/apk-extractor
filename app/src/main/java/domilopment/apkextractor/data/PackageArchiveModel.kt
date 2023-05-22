package domilopment.apkextractor.data

import android.content.ContentResolver
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import androidx.documentfile.provider.DocumentFile
import domilopment.apkextractor.utils.Utils
import java.io.File
import java.io.IOException

data class PackageArchiveModel(
    private val packageManager: PackageManager,
    private val contentResolver: ContentResolver,
    private val cacheDir: File,
    private val documentFile: DocumentFile,
) : BaseObservable() {
    val fileUri: Uri = documentFile.uri
    val fileName: String? = documentFile.name
    val fileLastModified: Long = documentFile.lastModified()
    val fileSize: Float = documentFile.length() / (1000.0F * 1000.0F)

    @get:Bindable
    var isPackageArchiveInfoLoaded = false
        private set

    @get:Bindable
    var isPackageArchiveInfoLoading = false
        private set

    @get:Bindable
    var appName: CharSequence? = null
        private set

    @get:Bindable
    var appPackageName: String? = null
        private set

    @get:Bindable
    var appIcon: Drawable? = null
        private set

    @get:Bindable
    var appVersionName: String? = null
        private set

    @get:Bindable
    var appVersionCode: Long? = null
        private set

    fun exist() = documentFile.exists()

    fun loadPackageArchiveInfo() {
        if (isPackageArchiveInfoLoaded || isPackageArchiveInfoLoading) return
        isPackageArchiveInfoLoading = true
        notifyPropertyChanged(BR.packageArchiveInfoLoading)

        var apkFile: File? = null
        try {
            apkFile = File.createTempFile("temp", ".apk", cacheDir)
            contentResolver.openInputStream(documentFile.uri).use { input ->
                if (input?.copyTo(apkFile.outputStream()) == documentFile.length()) {
                    val archiveInfo =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) packageManager.getPackageArchiveInfo(
                            apkFile.path, PackageManager.PackageInfoFlags.of(0L)
                        ) else packageManager.getPackageArchiveInfo(apkFile.path, 0)
                    archiveInfo?.also {
                        it.applicationInfo.sourceDir = apkFile.path
                        it.applicationInfo.publicSourceDir = apkFile.path
                        appName = it.applicationInfo.loadLabel(packageManager)
                        notifyPropertyChanged(BR.appName)
                        appPackageName = it.applicationInfo.packageName
                        notifyPropertyChanged(BR.appPackageName)
                        appIcon = it.applicationInfo.loadIcon(packageManager)
                        notifyPropertyChanged(BR.appIcon)
                        appVersionName = it.versionName
                        notifyPropertyChanged(BR.appVersionName)
                        appVersionCode = Utils.versionCode(it)
                        notifyPropertyChanged(BR.appVersionCode)
                    }
                }
            }
            isPackageArchiveInfoLoaded = true
            notifyPropertyChanged(BR.packageArchiveInfoLoaded)
        } catch (_: IOException) {
            // No Space left on Device, ...
        } finally {
            isPackageArchiveInfoLoading = false
            notifyPropertyChanged(BR.packageArchiveInfoLoading)
            if (apkFile != null && apkFile.exists()) apkFile.delete()
        }
    }

    fun forceRefresh() {
        isPackageArchiveInfoLoaded = false
        notifyPropertyChanged(BR.packageArchiveInfoLoaded)
        loadPackageArchiveInfo()
    }
}
