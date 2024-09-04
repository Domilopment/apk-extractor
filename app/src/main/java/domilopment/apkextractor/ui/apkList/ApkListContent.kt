package domilopment.apkextractor.ui.apkList

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
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
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.ui.components.PullToRefreshBox
import domilopment.apkextractor.utils.FileUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkListContent(
    apkList: List<PackageArchiveEntity>,
    totalSpace: Long,
    takenSpace: Long,
    freeSpace: Long,
    searchString: String?,
    isRefreshing: Boolean,
    isPullToRefresh: Boolean,
    onRefresh: () -> Unit,
    onClick: (PackageArchiveEntity) -> Unit,
    isApkFileDeleted: (PackageArchiveEntity) -> Boolean,
    deletedDocumentFound: (PackageArchiveEntity) -> Unit,
    onStorageInfoClick: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing, onRefresh = onRefresh, enabled = isPullToRefresh
    ) {
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
    }
}

@Preview
@Composable
private fun ApkListScreenPreview() {
    val apks = remember {
        mutableStateListOf(
            PackageArchiveEntity(
                fileUri = Uri.parse("test"),
                fileName = "test.apk",
                fileType = FileUtil.FileInfo.APK.mimeType,
                fileLastModified = 0L,
                fileSize = 1024 * 1024,
                appName = "Test",
                appPackageName = "com.example.test",
                appIcon = null,
                appVersionName = "v0",
                appVersionCode = 0L,
                appMinSdkVersion = 28,
                appTargetSdkVersion = 33,
            ), PackageArchiveEntity(
                fileUri = Uri.parse("test2"),
                fileName = "test2.apk",
                fileType = FileUtil.FileInfo.APK.mimeType,
                fileLastModified = 0L,
                fileSize = 1024 * 1024,
                appName = "Test",
                appPackageName = "com.example.test2",
                appIcon = null,
                appVersionName = "v0",
                appVersionCode = 0L,
                appMinSdkVersion = 28,
                appTargetSdkVersion = 33,
            ), PackageArchiveEntity(
                fileUri = Uri.parse("test (2)"),
                fileName = "test (2).apk",
                fileType = FileUtil.FileInfo.APK.mimeType,
                fileLastModified = 0L,
                fileSize = 1024 * 1024,
                appName = "Test",
                appPackageName = "com.example.test",
                appIcon = null,
                appVersionName = "v1.0.1",
                appVersionCode = 2L,
                appMinSdkVersion = 28,
                appTargetSdkVersion = 33,
            )
        )
    }
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    val apkToAdd = PackageArchiveEntity(
        fileUri = Uri.parse("test (3)"),
        fileName = "test (3).apk",
        fileType = FileUtil.FileInfo.APK.mimeType,
        fileLastModified = 0L,
        fileSize = 1024 * 1024,
        appName = "Test",
        appPackageName = "com.example.test",
        appIcon = null,
        appVersionName = "v1.1.0",
        appVersionCode = 3L,
        appMinSdkVersion = 28,
        appTargetSdkVersion = 33,
    )
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
                isApkFileDeleted = { _ -> false },
                deletedDocumentFound = { _ -> },
                onStorageInfoClick = {})
        }
    }
}