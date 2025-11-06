package domilopment.apkextractor.ui.appList

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.ui.components.PullToRefreshBox
import domilopment.apkextractor.utils.apkActions.ApkActionIntent
import domilopment.apkextractor.utils.apkActions.ApkActionsOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListContent(
    appList: List<ApplicationModel.ApplicationListModel>,
    searchString: String?,
    isSwipeToDismiss: Boolean,
    updateApp: (ApplicationModel.ApplicationListModel) -> Unit,
    triggerActionMode: (ApplicationModel.ApplicationListModel) -> Unit,
    isRefreshing: Boolean,
    isPullToRefresh: Boolean,
    onRefresh: () -> Unit,
    rightSwipeAction: ApkActionsOptions,
    leftSwipeAction: ApkActionsOptions,
    swipeActionCallback: (ApkActionIntent) -> Unit,
    isSwipeActionCustomThreshold: Boolean,
    swipeActionThresholdModifier: Float,
    uninstalledAppFound: (ApplicationModel.ApplicationListModel) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing, onRefresh = onRefresh, enabled = isPullToRefresh
    ) {
        AppList(
            appList = appList,
            searchString = searchString,
            isSwipeToDismiss = isSwipeToDismiss,
            updateApp = updateApp,
            triggerActionMode = triggerActionMode,
            rightSwipeAction = rightSwipeAction,
            leftSwipeAction = leftSwipeAction,
            swipeActionCallback = swipeActionCallback,
            isSwipeActionCustomThreshold = isSwipeActionCustomThreshold,
            swipeActionThresholdModifier = swipeActionThresholdModifier,
            uninstalledAppFound = uninstalledAppFound
        )
    }
}

@Preview
@Composable
private fun AppListScreenPreview() {
    val context = LocalContext.current
    val apps = remember {
        mutableStateListOf(
            ApplicationModel.ApplicationListModel(
                appPackageName = BuildConfig.APPLICATION_ID,
                appName = "Apk Ectractor",
                appIcon = context.packageManager.getApplicationIcon(BuildConfig.APPLICATION_ID),
                appFlags = 0,
                appCategory = 0,
                appInstallTime = 0,
                appUpdateTime = 0,
                apkSize = 1024F,
                launchIntent = null,
                installationSource = null,
                isFavorite = true
            ),
            ApplicationModel.ApplicationListModel(
                appPackageName = "com.google.android.youtube",
                appName = "YouTube",
                appIcon = context.packageManager.defaultActivityIcon,
                appFlags = 0,
                appCategory = 0,
                appInstallTime = 0,
                appUpdateTime = 0,
                apkSize = 1024F * 5,
                launchIntent = null,
                installationSource = null,
            ),
            ApplicationModel.ApplicationListModel(
                appPackageName = "com.google.android.apps.messaging",
                appName = "Messages",
                appIcon = context.packageManager.defaultActivityIcon,
                appFlags = 0,
                appCategory = 0,
                appInstallTime = 0,
                appUpdateTime = 0,
                apkSize = 1024F * 10,
                launchIntent = null,
                installationSource = null,
            ),
        )
    }
    var actionMode by remember {
        mutableStateOf(false)
    }
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    val appToAdd = ApplicationModel.ApplicationListModel(
        appPackageName = "com.android.vending",
        appName = "Play Store",
        appIcon = context.packageManager.defaultActivityIcon,
        appFlags = 0,
        appCategory = 0,
        appInstallTime = 0,
        appUpdateTime = 0,
        apkSize = 1024F * 20,
        launchIntent = null,
        installationSource = null,
    )

    MaterialTheme {
        Column {
            Text(text = "ActionMode is ${if (actionMode) "ON" else "OFF"}")
            Button(onClick = { actionMode = false }) {
                Text(text = "Stop ActionMode")
            }
            AppListContent(appList = apps,
                searchString = "",
                isSwipeToDismiss = !actionMode,
                updateApp = { app ->
                    if (actionMode) apps.replaceAll {
                        if (it.appPackageName == app.appPackageName) app.copy(
                            isChecked = !app.isChecked
                        ) else it
                    }
                },
                triggerActionMode = { if (!actionMode) actionMode = true },
                isRefreshing = refreshing,
                isPullToRefresh = !actionMode,
                onRefresh = {
                    refreshScope.launch {
                        refreshing = true
                        delay(1500)
                        if (!apps.contains(appToAdd)) apps.add(appToAdd)
                        refreshing = false
                    }
                },
                rightSwipeAction = ApkActionsOptions.SAVE,
                leftSwipeAction = ApkActionsOptions.SHARE,
                swipeActionCallback = { _ -> },
                isSwipeActionCustomThreshold = false,
                swipeActionThresholdModifier = 0.5f,
                uninstalledAppFound = { _ -> })
        }
    }
}