package domilopment.apkextractor.ui.composables

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import domilopment.apkextractor.R
import domilopment.apkextractor.data.PackageArchiveModel
import domilopment.apkextractor.utils.Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkOptionBottomSheet(
    apk: PackageArchiveModel,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    onRefresh: () -> Unit,
    onActionShare: () -> Unit,
    onActionInstall: () -> Unit,
    onActionDelete: () -> Unit,
    onActionUninstall: () -> Unit,
    onDialogPositioned: (Dp) -> Unit
) {
    val localDensity = LocalDensity.current
    ModalBottomSheet(
        onDismissRequest = onDismissRequest, modifier = Modifier.onGloballyPositioned {
            onDialogPositioned(with(localDensity) { it.boundsInParent().height.toDp() })
        }, sheetState = sheetState, windowInsets = WindowInsets(bottom = 24.dp)
    ) {
        ApkSheetHeader(
            apkName = apk.appName,
            fileName = apk.fileName,
            packageName = apk.appPackageName,
            appIcon = apk.appIcon,
            isRefreshing = apk.isPackageArchiveInfoLoading,
            onRefresh = onRefresh
        )
        HorizontalDivider()
        ApkSheetInfo(
            sourceDirectory = apk.fileUri,
            apkFileName = apk.fileName,
            apkSize = apk.fileSize,
            apkCreated = apk.fileLastModified,
            versionName = apk.appVersionName,
            versionNumber = apk.appVersionCode,
        )
        HorizontalDivider()
        ApkSheetActions(
            packageName = apk.appPackageName,
            onActionShare = onActionShare,
            onActionInstall = onActionInstall,
            onActionDelete = onActionDelete,
            onActionUninstall = onActionUninstall
        )
    }
}

@Composable
fun ApkSheetActions(
    packageName: String?,
    onActionShare: () -> Unit,
    onActionInstall: () -> Unit,
    onActionDelete: () -> Unit,
    onActionUninstall: () -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(8.dp, 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(onClick = onActionShare,
            label = { Text(text = stringResource(id = R.string.alert_apk_selected_share)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Share, contentDescription = null
                )
            })
        AssistChip(onClick = onActionInstall,
            label = { Text(text = stringResource(id = R.string.alert_apk_selected_install)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Android, contentDescription = null
                )
            })
        AssistChip(onClick = onActionDelete,
            label = { Text(text = stringResource(id = R.string.alert_apk_selected_delete)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete, contentDescription = null
                )
            })
        AssistChip(onClick = onActionUninstall,
            label = { Text(text = stringResource(id = R.string.apk_action_uninstall_app)) },
            enabled = packageName != null && Utils.isPackageInstalled(
                LocalContext.current.packageManager, packageName
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Clear, contentDescription = null
                )
            })
    }
}

@Composable
fun ApkSheetInfo(
    sourceDirectory: Uri,
    apkFileName: String,
    apkSize: Float,
    apkCreated: Long,
    versionName: String?,
    versionNumber: Long?
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(8.dp, 0.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(
                id = R.string.apk_bottom_sheet_source_uri, sourceDirectory
            ), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(id = R.string.apk_bottom_sheet_file_name, apkFileName),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(
                id = R.string.info_bottom_sheet_apk_size, apkSize
            ), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(
                id = R.string.apk_bottom_sheet_last_modified, Utils.getAsFormattedDate(apkCreated)
            ), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(
                id = R.string.info_bottom_sheet_version_name, versionName.toString()
            ), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(
                id = R.string.info_bottom_sheet_version_number, versionNumber ?: 0
            ), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ApkSheetHeader(
    apkName: CharSequence?,
    fileName: String,
    packageName: String?,
    appIcon: Drawable?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    ListItem(headlineContent = {
        if (packageName != null) Text(
            text = packageName,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 12.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }, modifier = Modifier.height(72.dp), overlineContent = {
        Text(
            text = apkName?.toString() ?: fileName,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 16.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }, leadingContent = {
        val context = LocalContext.current
        Image(
            painter = rememberDrawablePainter(
                drawable = appIcon ?: ResourcesCompat.getDrawable(
                    context.resources, android.R.drawable.sym_def_app_icon, context.theme
                )
            ),
            contentDescription = stringResource(id = R.string.list_item_Image_description),
            modifier = Modifier.width(72.dp)
        )
    }, trailingContent = {
        if (isRefreshing) CircularProgressIndicator(
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        ) else FilledTonalIconButton(onClick = onRefresh) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
        }
    })

}
