package domilopment.apkextractor.ui.apkList

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import domilopment.apkextractor.data.apkList.PackageArchiveModel
import domilopment.apkextractor.ui.components.PullToRefreshBox
import domilopment.apkextractor.utils.FileUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ApkListContent(
    apkList: List<PackageArchiveModel>,
    totalSpace: Long,
    takenSpace: Long,
    freeSpace: Long,
    searchString: String?,
    isRefreshing: Boolean,
    isPullToRefresh: Boolean,
    onRefresh: () -> Unit,
    onClick: (PackageArchiveModel) -> Unit,
    onLoadingPackageArchiveInfo: (PackageArchiveModel) -> Unit,
    isApkFileDeleted: (PackageArchiveModel) -> Boolean,
    deletedDocumentFound: (PackageArchiveModel) -> Unit,
    onStorageInfoClick: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing, onRefresh = onRefresh, isPullToRefreshEnabled = isPullToRefresh
    ) {
        ApkList(
            apkList = apkList,
            totalSpace = totalSpace,
            takenSpace = takenSpace,
            freeSpace = freeSpace,
            searchString = searchString,
            onClick = onClick,
            onLoadPackageArchiveInfo = onLoadingPackageArchiveInfo,
            isApkFileDeleted = isApkFileDeleted,
            deletedDocumentFound = deletedDocumentFound,
            onStorageInfoClick = onStorageInfoClick
        )
    }
}

@Preview
@Composable
private fun ApkListScreenPreview() {
    val apks = remember {
        mutableStateListOf(object : PackageArchiveModel {
            override val fileUri: Uri = Uri.parse("test")
            override val fileName: String = "test.apk"
            override val fileType: String = FileUtil.FileInfo.APK.mimeType
            override val fileLastModified: Long = 0L
            override val fileSize: Long = 1024 * 1024
            override var appName: CharSequence? = "Test"
            override var appPackageName: String? = "com.example.test"
            override var appIcon: Drawable? = null
            override var appVersionName: String? = "v0"
            override var appVersionCode: Long? = 0L
            override var appMinSdkVersion: Int? = 28
            override var appTargetSdkVersion: Int? = 33
            override var isPackageArchiveInfoLoading: Boolean = false
            override var isPackageArchiveInfoLoaded: Boolean = false
            override fun packageArchiveInfo(context: Context): PackageArchiveModel = this
            override fun forceRefresh(context: Context): PackageArchiveModel = this
        }, object : PackageArchiveModel {
            override val fileUri: Uri = Uri.parse("test2")
            override val fileName: String = "test2.apk"
            override val fileType: String = FileUtil.FileInfo.APK.mimeType
            override val fileLastModified: Long = 0L
            override val fileSize: Long = 1024 * 1024
            override var appName: CharSequence? = "Test"
            override var appPackageName: String? = "com.example.test2"
            override var appIcon: Drawable? = null
            override var appVersionName: String? = "v0"
            override var appVersionCode: Long? = 0L
            override var appMinSdkVersion: Int? = 28
            override var appTargetSdkVersion: Int? = 33
            override var isPackageArchiveInfoLoading: Boolean = false
            override var isPackageArchiveInfoLoaded: Boolean = false
            override fun packageArchiveInfo(context: Context): PackageArchiveModel = this
            override fun forceRefresh(context: Context): PackageArchiveModel = this
        }, object : PackageArchiveModel {
            override val fileUri: Uri = Uri.parse("test (2)")
            override val fileName: String = "test (2).apk"
            override val fileType: String = FileUtil.FileInfo.APK.mimeType
            override val fileLastModified: Long = 0L
            override val fileSize: Long = 1024 * 1024
            override var appName: CharSequence? = "Test"
            override var appPackageName: String? = "com.example.test"
            override var appIcon: Drawable? = null
            override var appVersionName: String? = "v1.0.1"
            override var appVersionCode: Long? = 2L
            override var appMinSdkVersion: Int? = 28
            override var appTargetSdkVersion: Int? = 33
            override var isPackageArchiveInfoLoading: Boolean = false
            override var isPackageArchiveInfoLoaded: Boolean = false
            override fun packageArchiveInfo(context: Context): PackageArchiveModel = this
            override fun forceRefresh(context: Context): PackageArchiveModel = this
        })
    }
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    val apkToAdd = object : PackageArchiveModel {
        override val fileUri: Uri = Uri.parse("test (3)")
        override val fileName: String = "test (3).apk"
        override val fileType: String = FileUtil.FileInfo.APK.mimeType
        override val fileLastModified: Long = 0L
        override val fileSize: Long = 1024 * 1024
        override var appName: CharSequence? = "Test"
        override var appPackageName: String? = "com.example.test"
        override var appIcon: Drawable? = null
        override var appVersionName: String? = "v1.1.0"
        override var appVersionCode: Long? = 3L
        override var appMinSdkVersion: Int? = 28
        override var appTargetSdkVersion: Int? = 33
        override var isPackageArchiveInfoLoading: Boolean = false
        override var isPackageArchiveInfoLoaded: Boolean = false
        override fun packageArchiveInfo(context: Context): PackageArchiveModel = this
        override fun forceRefresh(context: Context): PackageArchiveModel = this
    }

    val space by remember {
        derivedStateOf {
            apks.sumOf { it.fileSize }
        }
    }

    MaterialTheme {
        Column {
            ApkListContent(apkList = apks,
                totalSpace = 6L * 1000 * 1000 * 1000,
                takenSpace = space,
                freeSpace = 4L * 1000 * 1000 * 1000,
                searchString = "",
                isRefreshing = refreshing,
                isPullToRefresh = true,
                onRefresh = {
                    refreshScope.launch {
                        refreshing = true
                        delay(1500)
                        if (!apks.contains(apkToAdd)) apks.add(apkToAdd)
                        refreshing = false
                    }
                },
                onClick = { _ -> },
                onLoadingPackageArchiveInfo = { },
                isApkFileDeleted = { _ -> false },
                deletedDocumentFound = { _ -> },
                onStorageInfoClick = {})
        }
    }
}