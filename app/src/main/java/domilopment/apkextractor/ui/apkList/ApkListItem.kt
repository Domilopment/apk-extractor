package domilopment.apkextractor.ui.apkList

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.utils.FileUtil

@Composable
fun ApkListItem(
    apkFileName: AnnotatedString,
    appName: AnnotatedString?,
    appPackageName: AnnotatedString?,
    appIcon: ImageBitmap?,
    apkVersionInfo: AnnotatedString?,
    onClick: () -> Unit
) {
    Box {
        ListItem(headlineContent = {
            if (appPackageName != null) Text(
                text = appPackageName,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
            modifier = Modifier
                .height(96.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(0.dp, 8.dp),
            overlineContent = {
                Text(
                    text = appName ?: apkFileName,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                if (apkVersionInfo != null) Text(
                    text = apkVersionInfo,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingContent = { ApkListItemAvatar(appIcon = appIcon) })
        Text(
            text = apkFileName,
            modifier = Modifier.padding(16.dp, 0.dp),
            color = ListItemDefaults.contentColor,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ApkListItemAvatar(appIcon: ImageBitmap?) {
    val context = LocalContext.current
    Image(
        bitmap = appIcon ?: ResourcesCompat.getDrawable(
            context.resources, android.R.drawable.sym_def_app_icon, context.theme
        )!!.toBitmap().asImageBitmap(),
        contentDescription = null,
        modifier = Modifier
            .padding(0.dp, 6.dp)
            .size(50.dp)
    )
}

@Preview
@Composable
private fun ApkListItemPreview() {
    val apk by remember {
        mutableStateOf(
            PackageArchiveEntity(
                fileUri = Uri.parse("test"),
                fileName = "test.apk",
                fileType = FileUtil.FileInfo.APK.mimeType,
                fileLastModified = 0L,
                fileSize = 1024,
                appName = "Test",
                appPackageName = "com.example.test",
                appIcon = null,
                appVersionName = "v1.0",
                appVersionCode = 2L,
                appMinSdkVersion = 28,
                appTargetSdkVersion = 33,
            )
        )
    }
    MaterialTheme {
        Column {
            ApkListItem(
                AnnotatedString(apk.fileName),
                appName = AnnotatedString(apk.appName.toString()),
                appPackageName = AnnotatedString(apk.appPackageName.toString()),
                appIcon = apk.appIcon,
                apkVersionInfo = AnnotatedString("Version: ${apk.appVersionName} (${apk.appVersionCode})"),
            ) { }
        }
    }
}