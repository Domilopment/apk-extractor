package domilopment.apkextractor.ui.apkList

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import domilopment.apkextractor.data.apkList.PackageArchiveModel
import domilopment.apkextractor.utils.FileUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkListContent(
    apkList: List<PackageArchiveModel>,
    totalSpace: Long,
    takenSpace: Long,
    freeSpace: Long,
    searchString: String?,
    refreshing: Boolean,
    isPullToRefresh: Boolean,
    onRefresh: () -> Unit,
    onClick: (PackageArchiveModel) -> Unit,
    isApkFileDeleted: (PackageArchiveModel) -> Boolean,
    deletedDocumentFound: (PackageArchiveModel) -> Unit,
    onStorageInfoClick: () -> Unit
) {
    val state = rememberPullToRefreshState(enabled = { isPullToRefresh })
    if (state.isRefreshing) {
        LaunchedEffect(true) {
            if (!refreshing) onRefresh()
        }
    }
    LaunchedEffect(refreshing) {
        if (refreshing && !state.isRefreshing) state.startRefresh()
        else if (!refreshing && state.isRefreshing) state.endRefresh()
    }

    Box(Modifier.nestedScroll(state.nestedScrollConnection)) {
        ApkList(
            apkList = apkList,
            totalSpace = totalSpace,
            takenSpace = takenSpace,
            freeSpace = freeSpace,
            searchString = searchString,
            onClick = onClick,
            isApkFileDeleted = isApkFileDeleted,
            deletedDocumentFound = deletedDocumentFound,
            onStorageInfoClick = onStorageInfoClick
        )

        PullToRefreshContainer(state = state, modifier = Modifier.align(Alignment.TopCenter))
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
                refreshing = refreshing,
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
                isApkFileDeleted = { _ -> false },
                deletedDocumentFound = { _ -> },
                onStorageInfoClick = {})
        }
    }
}