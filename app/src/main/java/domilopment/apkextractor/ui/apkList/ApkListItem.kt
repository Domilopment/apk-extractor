package domilopment.apkextractor.ui.apkList

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color = ListItemDefaults.containerColor)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = apkFileName,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .offset(y = 6.dp)
                .zIndex(zIndex = 1f),
            color = ListItemDefaults.contentColor,
            fontSize = 11.sp,
            lineHeight = TextUnit(16F, TextUnitType.Sp),
            overflow = TextOverflow.Clip,
            maxLines = 1,
        )
        ListItem(
            headlineContent = {
                if (appPackageName != null) Text(
                    text = appPackageName,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
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
            leadingContent = { ApkListItemAvatar(appIcon = appIcon) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
private fun ApkListItemAvatar(appIcon: ImageBitmap?) {
    val context = LocalContext.current
    val icon = remember(appIcon) {
        appIcon ?: ResourcesCompat.getDrawable(
            context.resources, android.R.drawable.sym_def_app_icon, context.theme
        )!!.toBitmap().asImageBitmap()
    }
    Image(
        bitmap = icon, contentDescription = null, modifier = Modifier.size(56.dp)
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