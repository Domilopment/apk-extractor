package domilopment.apkextractor.ui.apkList

import android.net.Uri
import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import domilopment.apkextractor.R
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.ui.attrColorResource
import domilopment.apkextractor.ui.components.ScrollToTopLazyColumn
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.Utils
import timber.log.Timber

@Composable
fun ApkList(
    apkList: List<PackageArchiveEntity>,
    totalSpace: Long,
    takenSpace: Long,
    freeSpace: Long,
    searchString: String?,
    onClick: (PackageArchiveEntity) -> Unit,
    isApkFileDeleted: (PackageArchiveEntity) -> Boolean,
    deletedDocumentFound: (PackageArchiveEntity) -> Unit,
    onStorageInfoClick: () -> Unit
) {
    val highlightColor = attrColorResource(
        attrId = android.R.attr.textColorHighlight,
        defaultColor = MaterialTheme.colorScheme.inversePrimary
    )

    ScrollToTopLazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item {
            StorageInfo(
                totalSpace = totalSpace,
                takenSpace = takenSpace,
                freeSpace = freeSpace,
                onStorageInfoClick = onStorageInfoClick
            )
        }

        items(items = apkList, key = { it.fileUri }) { apk ->
            if (isApkFileDeleted(apk)) {
                deletedDocumentFound(apk)
                return@items
            }

            val fileName = remember(apk.fileName, searchString) {
                Utils.getAnnotatedString(
                    apk.fileName, searchString, highlightColor
                )
            }

            val appName = remember(apk.appName, searchString) {
                Utils.getAnnotatedString(
                    apk.appName, searchString, highlightColor
                )
            }

            val packageName = remember(apk.appPackageName, searchString) {
                Utils.getAnnotatedString(
                    apk.appPackageName, searchString, highlightColor
                )
            }

            val versionName =
                if (apk.appVersionName != null && apk.appVersionCode != null) stringResource(
                    id = R.string.apk_holder_version, apk.appVersionName!!, apk.appVersionCode!!
                ) else null

            val versionInfo = remember(versionName, searchString) {
                Utils.getAnnotatedString(versionName, searchString, highlightColor)
            }

            ApkListItem(
                apkFileName = fileName!!,
                appName = appName,
                appPackageName = packageName,
                appIcon = apk.appIcon,
                apkVersionInfo = versionInfo,
                onClick = { onClick(apk) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun StorageInfo(
    modifier: Modifier = Modifier,
    totalSpace: Long,
    takenSpace: Long,
    freeSpace: Long,
    onStorageInfoClick: () -> Unit
) {
    val context = LocalContext.current

    var expanded by remember {
        mutableStateOf(false)
    }

    val formatTotalSpace = remember(totalSpace) {
        Formatter.formatFileSize(context, totalSpace)
    }

    val formatTakenSpace = remember(takenSpace) {
        Formatter.formatFileSize(context, takenSpace)
    }

    Surface(
        modifier = modifier
            .zIndex(1f)
            .clickable(onClick = onStorageInfoClick),
        shape = RoundedCornerShape(bottomEnd = 8.dp, bottomStart = 8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        var width by remember {
            mutableFloatStateOf(0f)
        }
        val backupsOffset = remember {
            Animatable(initialValue = 0f)
        }
        val nonFreeOffset = remember {
            Animatable(initialValue = 0f)
        }

        LaunchedEffect(width, takenSpace) {
            backupsOffset.animateTo(targetValue = width * (takenSpace.toFloat() / totalSpace))
        }

        LaunchedEffect(width, freeSpace) {
            nonFreeOffset.animateTo(targetValue = width * ((totalSpace - freeSpace).toFloat() / totalSpace))
        }

        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(id = R.string.apk_list_sum_backup_size_title),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            val color = MaterialTheme.colorScheme.primary
            val nonFree = MaterialTheme.colorScheme.inversePrimary
            val trackColor = MaterialTheme.colorScheme.onSurfaceVariant
            Canvas(
                Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(8.dp)
            ) {
                val y = size.height / 2
                width = size.width
                val strokeWidth = size.height
                drawLine(
                    trackColor,
                    Offset(0f, y),
                    Offset(width, y),
                    strokeWidth,
                    StrokeCap.Round,
                )
                drawLine(
                    nonFree,
                    Offset(backupsOffset.value, y),
                    Offset(nonFreeOffset.value, y),
                    strokeWidth,
                    StrokeCap.Round
                )
                drawLine(
                    color,
                    Offset(0f, y),
                    Offset(backupsOffset.value, y),
                    strokeWidth,
                    StrokeCap.Round,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(
                        id = R.string.apk_list_sum_backup_size_percentage,
                        takenSpace * 100F / totalSpace
                    )
                )
                Text(text = "${formatTakenSpace}/${formatTotalSpace}")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Circle,
                            contentDescription = null,
                            modifier = Modifier.height(12.dp),
                            tint = color
                        )
                        Text(text = stringResource(id = R.string.apk_list_sum_backup_size_info_backups))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Circle,
                            contentDescription = null,
                            modifier = Modifier.height(12.dp),
                            tint = nonFree
                        )
                        Text(text = stringResource(id = R.string.apk_list_sum_backup_size_info_used))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Circle,
                            contentDescription = null,
                            modifier = Modifier.height(12.dp),
                            tint = trackColor
                        )
                        Text(text = stringResource(id = R.string.apk_list_sum_backup_size_info_total))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ApkListPreview() {
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

    val space = remember(apks) {
        apks.sumOf { it.fileSize }
    }

    MaterialTheme {
        Column {
            ApkList(apkList = apks,
                totalSpace = 6L * 1000 * 1000 * 1000,
                takenSpace = space,
                freeSpace = 4L * 1000 * 1000 * 1000,
                searchString = "",
                onClick = { apk -> Timber.tag(apk.fileName).i(apk.appPackageName.toString()) },
                isApkFileDeleted = { _ -> false },
                deletedDocumentFound = { _ -> },
                onStorageInfoClick = { })
        }
    }
}