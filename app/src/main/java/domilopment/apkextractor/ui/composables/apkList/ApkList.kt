package domilopment.apkextractor.ui.composables.apkList

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import domilopment.apkextractor.R
import domilopment.apkextractor.data.PackageArchiveModel
import domilopment.apkextractor.ui.composables.attrColorResource
import domilopment.apkextractor.utils.Utils

@Composable
fun ApkList(
    apkList: List<PackageArchiveModel>,
    searchString: String?,
    onClick: (PackageArchiveModel) -> Unit,
) {
    val highlightColor = attrColorResource(attrId = android.R.attr.textColorHighlight)

    LazyColumn(state = rememberLazyListState(), modifier = Modifier.fillMaxSize()) {
        items(items = apkList, key = { it.fileUri }) { apk ->
            ApkListItem(
                apkFileName = Utils.getAnnotatedString(
                    apk.fileName, searchString, highlightColor
                )!!,
                appName = Utils.getAnnotatedString(apk.appName, searchString, highlightColor),
                appPackageName = Utils.getAnnotatedString(
                    apk.appPackageName, searchString, highlightColor
                ),
                appIcon = apk.appIcon,
                apkVersionInfo = if (apk.appVersionName != null && apk.appVersionCode != null) Utils.getAnnotatedString(
                    stringResource(
                        id = R.string.apk_holder_version, apk.appVersionName!!, apk.appVersionCode!!
                    ), searchString, highlightColor
                ) else null,
                isLoading = apk.isPackageArchiveInfoLoading
            ) { onClick(apk) }
        }
    }
}

@Preview
@Composable
private fun ApkListPreview() {
    val apks = remember {
        mutableStateListOf(
            PackageArchiveModel(
                fileUri = Uri.parse("test"),
                fileName = "Test.apk",
                fileLastModified = 0L,
                fileSizeLong = 1024L,
            ), PackageArchiveModel(
                fileUri = Uri.parse("test2"),
                fileName = "Test2.apk",
                fileLastModified = 0L,
                fileSizeLong = 1024L,
            ), PackageArchiveModel(
                fileUri = Uri.parse("test (2)"),
                fileName = "Test (2).apk",
                fileLastModified = 0L,
                fileSizeLong = 1024L,
                appName = "Test",
                appPackageName = "com.example.test",
                appVersionCode = 2L,
                appVersionName = "1.0.1",
            )
        )
    }
    MaterialTheme {
        Column {
            ApkList(
                apkList = apks, searchString = ""
            ) { apk -> Log.e(apk.fileName, apk.appPackageName.toString()) }
        }
    }
}