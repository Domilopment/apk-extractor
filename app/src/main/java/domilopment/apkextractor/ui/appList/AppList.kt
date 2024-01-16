package domilopment.apkextractor.ui.appList

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.ui.attrColorResource
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.Utils.getAnnotatedString
import domilopment.apkextractor.utils.apkActions.ApkActionsOptions

@OptIn(ExperimentalMaterial3Api::class)
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
    uninstalledAppFound: (ApplicationModel) -> Unit
) {
    val highlightColor = attrColorResource(attrId = android.R.attr.textColorHighlight)

    LazyColumn(state = rememberLazyListState(), modifier = Modifier.fillMaxSize()) {
        items(items = appList, key = { it.appPackageName }) { app ->
            if (!Utils.isPackageInstalled(
                    LocalContext.current.packageManager, app.appPackageName
                )
            ) {
                uninstalledAppFound(app)
                return@items
            }

            val state = rememberSwipeToDismissState(confirmValueChange = {
                if (it == SwipeToDismissValue.EndToStart) {
                    swipeActionCallback(app, leftSwipeAction)
                } else if (it == SwipeToDismissValue.StartToEnd) {
                    swipeActionCallback(app, rightSwipeAction)
                }
                false
            }, positionalThreshold = { it })

            SwipeToDismissBox(
                state = state, backgroundContent = {
                    val color by animateColorAsState(
                        when (state.dismissDirection) {
                            SwipeToDismissValue.StartToEnd, SwipeToDismissValue.EndToStart -> MaterialTheme.colorScheme.primaryContainer
                            SwipeToDismissValue.Settled -> Color.Transparent
                        }, label = ""
                    )

                    when (state.dismissDirection) {
                        SwipeToDismissValue.StartToEnd -> AppListItemSwipeRight(
                            rightSwipeAction = rightSwipeAction,
                            modifier = Modifier.background(color)
                        )

                        SwipeToDismissValue.EndToStart -> AppListItemSwipeLeft(
                            leftSwipeAction = leftSwipeAction, modifier = Modifier.background(color)
                        )

                        else -> {
                            // Nothing to do
                        }
                    }
                }, enableDismissFromStartToEnd = getSwipeDirections(
                    app, isSwipeToDismiss, rightSwipeAction
                ), enableDismissFromEndToStart = getSwipeDirections(
                    app, isSwipeToDismiss, leftSwipeAction
                )
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

                AppListItem(appName = appName!!,
                    appPackageName = packageName!!,
                    appIcon = app.appIcon,
                    apkSize = app.apkSize,
                    isChecked = app.isChecked,
                    isFavorite = app.isFavorite,
                    onClick = { updateApp(app) },
                    onLongClick = { triggerActionMode(app) })
            }
        }
    }
}

private fun getSwipeDirections(
    app: ApplicationModel,
    isSwipeToDismiss: Boolean,
    swipeDirection: ApkActionsOptions,
): Boolean {
    return isSwipeToDismiss && swipeDirection != ApkActionsOptions.NONE && ApkActionsOptions.isOptionSupported(
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
            modifier = Modifier.padding(0.dp, 6.dp),
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
                .width(48.dp),
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
                .width(48.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = stringResource(id = rightSwipeAction.title),
            modifier = Modifier.padding(0.dp, 6.dp),
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
            ApplicationModel(context.packageManager, BuildConfig.APPLICATION_ID),
            ApplicationModel(context.packageManager, "com.google.android.youtube"),
            ApplicationModel(context.packageManager, "com.google.android.apps.messaging")
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
            AppList(appList = apps,
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
                swipeActionCallback = { app, action -> Log.e(action.name, app.appPackageName) },
                uninstalledAppFound = { _ -> })
        }
    }
}