package domilopment.apkextractor.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.utils.apkActions.ApkActionsOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppListContent(
    appList: List<ApplicationModel>,
    searchString: String?,
    isSwipeToDismiss: Boolean,
    updateApp: (ApplicationModel) -> Unit,
    triggerActionMode: (ApplicationModel) -> Unit,
    refreshing: Boolean,
    isPullToRefresh: Boolean,
    onRefresh: () -> Unit,
    rightSwipeAction: ApkActionsOptions,
    leftSwipeAction: ApkActionsOptions,
    swipeActionCallback: (ApplicationModel, ApkActionsOptions) -> Unit
) {
    val state = rememberPullRefreshState(refreshing = refreshing, onRefresh = onRefresh)

    Box(Modifier.then(if (isPullToRefresh) Modifier.pullRefresh(state) else Modifier)) {
        AppList(
            appList = appList,
            searchString = searchString,
            isSwipeToDismiss = isSwipeToDismiss,
            updateApp = updateApp,
            triggerActionMode = triggerActionMode,
            rightSwipeAction = rightSwipeAction,
            leftSwipeAction = leftSwipeAction,
            swipeActionCallback = swipeActionCallback
        )

        PullRefreshIndicator(
            refreshing = refreshing,
            state = state,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Preview
@Composable
private fun AppListScreenPreview() {
    val context = LocalContext.current
    val apps = remember {
        mutableStateListOf(
            ApplicationModel(
                context.packageManager, BuildConfig.APPLICATION_ID
            ).apply { isFavorite = true },
            ApplicationModel(context.packageManager, "com.google.android.youtube"),
            ApplicationModel(context.packageManager, "com.google.android.apps.messaging")
        )
    }
    var actionMode by remember {
        mutableStateOf(false)
    }
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    val appToAdd = ApplicationModel(context.packageManager, "com.android.vending")

    MaterialTheme {
        Column {
            Text(text = "ActionMode is ${if (actionMode) "ON" else "OFF"}")
            Button(onClick = { actionMode = false }) {
                Text(text = "Stop ActionMode")
            }
            AppListContent(
                appList = apps,
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
                refreshing = refreshing,
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
                leftSwipeAction = ApkActionsOptions.SHARE
            ) { app, action -> null }
        }
    }
}