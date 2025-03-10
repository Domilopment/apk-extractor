package domilopment.apkextractor.ui.appList

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.ui.attrColorResource
import domilopment.apkextractor.ui.components.ScrollToTopLazyColumn
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.Utils.getAnnotatedString
import domilopment.apkextractor.utils.apkActions.ApkActionsOptions
import timber.log.Timber

@Composable
fun AppList(
    appList: List<ApplicationModel>,
    searchString: String?,
    isSwipeToDismiss: Boolean,
    updateApp: (ApplicationModel) -> Unit,
    triggerActionMode: (ApplicationModel) -> Unit,
    rightSwipeAction: ApkActionsOptions,
    leftSwipeAction: ApkActionsOptions,
    swipeActionCallback: (ApplicationModel, ApkActionsOptions) -> Unit,
    isSwipeActionCustomThreshold: Boolean,
    swipeActionThresholdModifier: Float,
    uninstalledAppFound: (ApplicationModel) -> Unit
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
        items(items = appList, key = { it.appPackageName }) { app ->
            if (!Utils.isPackageInstalled(
                    LocalContext.current.packageManager, app.appPackageName
                )
            ) {
                uninstalledAppFound(app)
                return@items
            }

            val density = LocalDensity.current
            val state = remember(
                leftSwipeAction,
                rightSwipeAction,
                isSwipeActionCustomThreshold,
                swipeActionThresholdModifier,
            ) {
                SwipeToDismissBoxState(
                    initialValue = SwipeToDismissBoxValue.Settled,
                    density = density,
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            swipeActionCallback(app, leftSwipeAction)
                        } else if (it == SwipeToDismissBoxValue.StartToEnd) {
                            swipeActionCallback(app, rightSwipeAction)
                        }
                        false
                    },
                    positionalThreshold = {
                        if (isSwipeActionCustomThreshold) it * swipeActionThresholdModifier
                        else with(density) { 56.dp.toPx() }
                    })
            }

            SwipeToDismissBox(
                state = state,
                backgroundContent = {
                    val color by animateColorAsState(
                        when (state.dismissDirection) {
                            SwipeToDismissBoxValue.Settled -> Color.Transparent
                            SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.primaryContainer
                        }, label = ""
                    )

                    when (state.dismissDirection) {
                        SwipeToDismissBoxValue.StartToEnd -> AppListItemSwipeRight(
                            rightSwipeAction = rightSwipeAction,
                            modifier = Modifier.background(color)
                        )

                        SwipeToDismissBoxValue.EndToStart -> AppListItemSwipeLeft(
                            leftSwipeAction = leftSwipeAction, modifier = Modifier.background(color)
                        )

                        else -> Unit
                    }
                },
                modifier = Modifier.animateItem(),
                enableDismissFromStartToEnd = getSwipeDirections(app, rightSwipeAction),
                enableDismissFromEndToStart = getSwipeDirections(app, leftSwipeAction),
                gesturesEnabled = isSwipeToDismiss
            ) {
                val appName = remember(app.appName, searchString) {
                    getAnnotatedString(
                        app.appName, searchString, highlightColor
                    )
                }

                val packageName = remember(app.appPackageName, searchString) {
                    getAnnotatedString(
                        app.appPackageName, searchString, highlightColor
                    )
                }

                AppListItem(
                    appName = appName!!,
                    appPackageName = packageName!!,
                    appIcon = app.appIcon,
                    apkSize = app.apkSize,
                    isChecked = app.isChecked,
                    isFavorite = app.isFavorite,
                    onClick = { updateApp(app) },
                    onLongClick = { triggerActionMode(app) },
                )
            }
        }
    }
}

private fun getSwipeDirections(
    app: ApplicationModel,
    swipeDirection: ApkActionsOptions,
): Boolean {
    return swipeDirection != ApkActionsOptions.NONE && ApkActionsOptions.isOptionSupported(
        app, swipeDirection
    )
}

@Composable
private fun AppListItemSwipeLeft(
    leftSwipeAction: ApkActionsOptions, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = leftSwipeAction.title),
            modifier = Modifier.padding(vertical = 6.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 20.sp,
            maxLines = 1
        )
        Icon(
            imageVector = leftSwipeAction.icon,
            contentDescription = null,
            modifier = Modifier
                .padding(6.dp)
                .fillMaxHeight()
                .width(40.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun AppListItemSwipeRight(
    rightSwipeAction: ApkActionsOptions, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = rightSwipeAction.icon,
            contentDescription = null,
            modifier = Modifier
                .padding(6.dp)
                .fillMaxHeight()
                .width(40.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = stringResource(id = rightSwipeAction.title),
            modifier = Modifier.padding(vertical = 6.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 20.sp,
            maxLines = 1
        )
    }
}

@Preview
@Composable
private fun AppListPreview() {
    val context = LocalContext.current
    val apps = remember {
        mutableStateListOf(
            ApplicationModel(
                appPackageName = BuildConfig.APPLICATION_ID,
                appName = "Apk Extractor",
                appSourceDirectory = "/data/app/${BuildConfig.APPLICATION_ID}/base.apk",
                appSplitSourceDirectories = null,
                appIcon = context.packageManager.getApplicationIcon(BuildConfig.APPLICATION_ID),
                appVersionName = "1.0",
                appVersionCode = 1,
                minSdkVersion = 39,
                targetSdkVersion = 34,
                appFlags = 0,
                appCategory = 0,
                appInstallTime = 0,
                appUpdateTime = 0,
                apkSize = 1024F,
                launchIntent = null,
                installationSource = null,
            ),
            ApplicationModel(
                appPackageName = "com.google.android.youtube",
                appName = "YouTube",
                appSourceDirectory = "/data/app/com.google.android.youtube/base.apk",
                appSplitSourceDirectories = null,
                appIcon = context.packageManager.defaultActivityIcon,
                appVersionName = "1.0",
                appVersionCode = 1,
                minSdkVersion = 39,
                targetSdkVersion = 34,
                appFlags = 0,
                appCategory = 0,
                appInstallTime = 0,
                appUpdateTime = 0,
                apkSize = 1024F * 5,
                launchIntent = null,
                installationSource = null,
            ),
            ApplicationModel(
                appPackageName = "com.google.android.apps.messaging",
                appName = "Messages",
                appSourceDirectory = "/data/app/com.google.android.apps.messaging/base.apk",
                appSplitSourceDirectories = null,
                appIcon = context.packageManager.defaultActivityIcon,
                appVersionName = "1.0",
                appVersionCode = 1,
                minSdkVersion = 39,
                targetSdkVersion = 34,
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
    MaterialTheme {
        Column {
            Text(text = "ActionMode is ${if (actionMode) "ON" else "OFF"}")
            Button(onClick = { actionMode = false }) {
                Text(text = "Stop ActionMode")
            }
            AppList(
                appList = apps,
                isSwipeToDismiss = !actionMode,
                searchString = "",
                updateApp = { app ->
                    if (actionMode) apps.replaceAll {
                        if (it.appPackageName == app.appPackageName) app.copy(
                            isChecked = !app.isChecked
                        ) else it
                    }
                },
                triggerActionMode = { if (!actionMode) actionMode = true },
                rightSwipeAction = ApkActionsOptions.SAVE,
                leftSwipeAction = ApkActionsOptions.SHARE,
                swipeActionCallback = { app, action ->
                    Timber.tag(action.name).i(app.appPackageName)
                },
                isSwipeActionCustomThreshold = false,
                swipeActionThresholdModifier = 0.5f,
                uninstalledAppFound = { _ -> })
        }
    }
}