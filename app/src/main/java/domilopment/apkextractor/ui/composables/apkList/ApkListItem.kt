package domilopment.apkextractor.ui.composables.apkList

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import domilopment.apkextractor.data.PackageArchiveModel

@Composable
fun ApkListItem(
    apkFileName: AnnotatedString,
    appName: AnnotatedString?,
    appPackageName: AnnotatedString?,
    appIcon: Drawable?,
    apkVersionInfo: AnnotatedString?,
    isLoading: Boolean,
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
                .height(84.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick),
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
            trailingContent = {
                if (isLoading) CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            })
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
private fun ApkListItemAvatar(appIcon: Drawable?) {
    val context = LocalContext.current
    val placeholder = remember {
        ResourcesCompat.getDrawable(
            context.resources, android.R.drawable.sym_def_app_icon, context.theme
        )!!.let { it.toBitmap(it.intrinsicWidth, it.intrinsicHeight, null).asImageBitmap() }
    }
    val icon = appIcon?.let {
        it.toBitmap(it.intrinsicWidth, it.intrinsicHeight, null).asImageBitmap()
    }

    Image(
        bitmap = icon ?: placeholder,
        contentDescription = null,
        modifier = Modifier.padding(0.dp, 6.dp)
    )
}

@Preview
@Composable
private fun ApkListItemPreview() {
    var apk by remember {
        mutableStateOf(
            PackageArchiveModel(
                fileUri = Uri.parse(""),
                fileName = "Test.apk",
                fileLastModified = 0L,
                fileSizeLong = 1024L,
                appName = "Test",
                appPackageName = "com.example.test",
                appVersionCode = 1L,
                appVersionName = "1.0",
            )
        )
    }
    MaterialTheme {
        Column {
            ApkListItem(
                AnnotatedString(apk.fileName!!),
                appName = AnnotatedString(apk.appName.toString()),
                appPackageName = AnnotatedString(apk.appPackageName.toString()),
                appIcon = apk.appIcon,
                apkVersionInfo = AnnotatedString("Version: ${apk.appVersionName} (${apk.appVersionCode})"),
                isLoading = apk.isPackageArchiveInfoLoading
            ) { }
        }
    }
}