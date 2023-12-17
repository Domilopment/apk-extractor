package domilopment.apkextractor.ui.composables

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.IconToggleButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.ui.ExpandableText
import domilopment.apkextractor.utils.MySnackbarVisuals
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.apkActions.ApkActionsManager
import domilopment.apkextractor.utils.appFilterOptions.AppFilterCategories
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AppOptionsBottomSheet(
    app: ApplicationModel,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    onFavoriteChanged: (Boolean) -> Unit,
    onActionShare: ManagedActivityResultLauncher<Intent, ActivityResult>,
    onActionSaveImage: PermissionState,
    intentUninstallApp: ManagedActivityResultLauncher<Intent, ActivityResult>,
    onActionUninstall: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val apkOptions = ApkActionsManager(LocalContext.current, app)

    ModalBottomSheet(onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        windowInsets = WindowInsets(bottom = 24.dp),
        dragHandle = {
            AppSheetHeader(
                appName = app.appName,
                packageName = app.appPackageName,
                appIcon = app.appIcon,
                isFavorite = app.isFavorite,
                snackbarHostState = snackbarHostState,
                onFavoriteChanged = onFavoriteChanged
            )
        }) {
        AppSheetInfo(sourceDirectory = app.appSourceDirectory,
            apkSize = app.apkSize,
            versionName = app.appVersionName,
            versionNumber = app.appVersionCode,
            appCategory = app.appCategory,
            installTime = app.appInstallTime,
            updateTime = app.appUpdateTime,
            installationSource = app.installationSource,
            onOpenStorePage = {
                apkOptions.actionOpenShop {
                    scope.launch { snackbarHostState.showSnackbar(it) }
                }
            })
        HorizontalDivider(modifier = Modifier.padding(4.dp))
        AppSheetActions(app = app,
            onActionSave = {
                apkOptions.actionSave {
                    scope.launch { snackbarHostState.showSnackbar(it) }
                }
            },
            onActionShare = { apkOptions.actionShare(onActionShare) },
            onActionSaveImage = label@{
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !onActionSaveImage.status.isGranted) {
                    onActionSaveImage.launchPermissionRequest()
                    return@label
                }
                apkOptions.actionSaveImage {
                    scope.launch { snackbarHostState.showSnackbar(it) }
                }
            },
            onActionOpenApp = apkOptions::actionOpenApp,
            onActionOpenSettings = apkOptions::actionShowSettings,
            onActionUninstall = {
                onActionUninstall()
                apkOptions.actionUninstall(intentUninstallApp)
            })
    }
}

@Composable
private fun AppSheetActions(
    app: ApplicationModel,
    onActionSave: () -> Unit,
    onActionShare: () -> Unit,
    onActionSaveImage: () -> Unit,
    onActionOpenApp: () -> Unit,
    onActionOpenSettings: () -> Unit,
    onActionUninstall: () -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(8.dp, 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(onClick = onActionSave,
            label = { Text(text = stringResource(id = R.string.action_bottom_sheet_save)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Save, contentDescription = null
                )
            })
        AssistChip(onClick = onActionShare,
            label = { Text(text = stringResource(id = R.string.action_bottom_sheet_share)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Share, contentDescription = null
                )
            })
        AssistChip(onClick = onActionSaveImage,
            label = { Text(text = stringResource(id = R.string.action_bottom_sheet_save_image)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Image, contentDescription = null
                )
            })
        if (app.launchIntent != null) AssistChip(onClick = onActionOpenApp,
            label = { Text(text = stringResource(id = R.string.action_bottom_sheet_open)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Android, contentDescription = null
                )
            })
        AssistChip(onClick = onActionOpenSettings,
            label = { Text(text = stringResource(id = R.string.action_bottom_sheet_settings)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Settings, contentDescription = null
                )
            })
        if ((app.appFlags and ApplicationInfo.FLAG_SYSTEM != ApplicationInfo.FLAG_SYSTEM) || (app.appUpdateTime > app.appInstallTime)) AssistChip(
            onClick = onActionUninstall,
            label = { Text(text = stringResource(id = R.string.action_bottom_sheet_uninstall)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Close, contentDescription = null
                )
            })
    }
}

@Composable
private fun AppSheetInfo(
    sourceDirectory: String,
    apkSize: Float,
    versionName: String?,
    versionNumber: Long,
    appCategory: Int,
    installTime: Long,
    updateTime: Long,
    installationSource: String?,
    onOpenStorePage: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(8.dp, 0.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ExpandableText(
            text = stringResource(
                id = R.string.info_bottom_sheet_source_directory, sourceDirectory
            ), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(id = R.string.info_bottom_sheet_apk_size, apkSize),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(
                id = R.string.info_bottom_sheet_version_name, versionName.toString()
            ), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(id = R.string.info_bottom_sheet_version_number, versionNumber),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(
                id = R.string.info_bottom_sheet_category,
                AppFilterCategories.getByCategory(appCategory)?.getTitleString(
                    LocalContext.current
                ) ?: ""
            ), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(
                id = R.string.info_bottom_sheet_install_time, Utils.getAsFormattedDate(installTime)
            ), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(
                id = R.string.info_bottom_sheet_update_time, Utils.getAsFormattedDate(updateTime)
            ), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            val packageManager = LocalContext.current.packageManager
            installationSource?.runCatching {
                Utils.getPackageInfo(packageManager, this).applicationInfo
            }?.onSuccess { applicationInfo ->
                val isKnownStore = applicationInfo.packageName in Utils.listOfKnownStores
                val sourceName = if (isKnownStore) "" else packageManager.getApplicationLabel(
                    applicationInfo
                )
                Text(
                    text = stringResource(
                        id = R.string.info_bottom_sheet_installation_source, sourceName
                    )
                )
                if (isKnownStore) Button(
                    onClick = onOpenStorePage,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 0.dp, minHeight = 0.dp)
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(12.dp, 0.dp)
                ) {
                    Image(
                        painter = rememberDrawablePainter(
                            drawable = packageManager.getApplicationIcon(
                                applicationInfo
                            )
                        ), contentDescription = null, modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = packageManager.getApplicationLabel(applicationInfo).toString()
                    )
                }
            }

        }
    }
}

@Composable
private fun AppSheetHeader(
    appName: String,
    packageName: String,
    appIcon: Drawable,
    isFavorite: Boolean,
    snackbarHostState: SnackbarHostState,
    onFavoriteChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.Center
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
        ) {
            val visuals = it.visuals as MySnackbarVisuals
            Snackbar(
                snackbarData = it,
                contentColor = visuals.messageColor ?: SnackbarDefaults.contentColor
            )
        }
        ListItem(headlineContent = {
            Text(
                text = appName,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }, modifier = Modifier.height(72.dp), supportingContent = {
            Text(
                text = packageName,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }, leadingContent = {
            Image(
                painter = rememberDrawablePainter(drawable = appIcon),
                contentDescription = stringResource(id = R.string.list_item_Image_description),
                modifier = Modifier.width(72.dp)
            )
        }, trailingContent = {
            IconToggleButton(checked = isFavorite, onCheckedChange = onFavoriteChanged) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = null
                )
            }
        })
        HorizontalDivider(modifier = Modifier.padding(4.dp))
    }
}
