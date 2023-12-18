package domilopment.apkextractor.ui.appList

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.ui.attrColorResource
import domilopment.apkextractor.utils.Utils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    appName: AnnotatedString,
    apkSize: Float,
    appPackageName: AnnotatedString,
    appIcon: Drawable,
    isChecked: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = appName, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(id = R.string.app_list_item_size, apkSize),
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(vertical = 4.dp),
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp
                )
            }
        },
        modifier = Modifier
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        supportingContent = {
            Text(
                text = appPackageName,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = { AppListItemAvatar(appIcon = appIcon, isChecked = isChecked) },
        trailingContent = {
            if (isFavorite) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = if (isChecked) MaterialTheme.colorScheme.surfaceVariant else ListItemDefaults.containerColor)
    )

}

@Composable
private fun AppListItemAvatar(appIcon: Drawable, isChecked: Boolean) {
    Box {
        Image(
            painter = rememberDrawablePainter(drawable = appIcon),
            contentDescription = stringResource(id = R.string.list_item_Image_description)
        )
        AppListItemCheckmark(
            isChecked = isChecked, modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(6.dp, 2.dp)
        )
    }
}

@Composable
private fun AppListItemCheckmark(isChecked: Boolean, modifier: Modifier = Modifier) {
    if (isChecked) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = stringResource(id = R.string.list_item_checkbox_description),
            modifier = modifier.background(
                color = MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape
            ),
            tint = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Preview
@Composable
private fun AppListItemPreview() {
    val context = LocalContext.current
    var app by remember {
        mutableStateOf(ApplicationModel(context.packageManager, BuildConfig.APPLICATION_ID))
    }
    var actionMode by remember {
        mutableStateOf(false)
    }
    val color = attrColorResource(attrId = android.R.attr.textColorHighlight)
    MaterialTheme {
        Column {
            Text(text = "ActionMode is ${if (actionMode) "ON" else "OFF"}")
            Button(onClick = { actionMode = false }) {
                Text(text = "Stop ActionMode")
            }
            AppListItem(
                appName = Utils.getAnnotatedString(app.appName, "", color)!!,
                appPackageName = Utils.getAnnotatedString(app.appPackageName, "", color)!!,
                appIcon = app.appIcon,
                apkSize = app.apkSize,
                isChecked = app.isChecked,
                isFavorite = app.isFavorite,
                onClick = { if (actionMode) app = app.copy(isChecked = !app.isChecked) },
                onLongClick = { if (!actionMode) actionMode = true },
            )
        }
    }
}