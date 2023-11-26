package domilopment.apkextractor.ui.composables.appList

import android.R
import android.util.Log
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
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import domilopment.apkextractor.BuildConfig
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.ui.composables.attrColorResource
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
    swipeActionCallback: (ApplicationModel, ApkActionsOptions) -> Unit
) {
    val highlightColor = attrColorResource(attrId = R.attr.textColorHighlight)

    LazyColumn(state = rememberLazyListState(), modifier = Modifier.fillMaxSize()) {
        items(items = appList, key = { it.appPackageName }) { app ->
            val state = rememberDismissState(confirmValueChange = {
                if (it == DismissValue.DismissedToStart) {
                    swipeActionCallback(app, leftSwipeAction)
                } else if (it == DismissValue.DismissedToEnd) {
                    swipeActionCallback(app, rightSwipeAction)
                }
                false
            }, positionalThreshold = { it })

            SwipeToDismiss(state = state, background = {
                val color = when (state.dismissDirection) {
                    DismissDirection.StartToEnd, DismissDirection.EndToStart -> MaterialTheme.colorScheme.primaryContainer
                    null -> Color.Transparent
                }

                when (state.dismissDirection) {
                    DismissDirection.StartToEnd -> AppListItemSwipeRight(
                        rightSwipeAction = rightSwipeAction,
                        modifier = Modifier.background(color)
                    )

                    DismissDirection.EndToStart -> AppListItemSwipeLeft(
                        leftSwipeAction = leftSwipeAction, modifier = Modifier.background(color)
                    )

                    null -> this
                }
            }, dismissContent = {
                AppListItem(appName = getAnnotatedString(
                    app.appName, searchString, highlightColor
                )!!,
                    appPackageName = getAnnotatedString(
                        app.appPackageName, searchString, highlightColor
                    )!!,
                    appIcon = app.appIcon,
                    apkSize = app.apkSize,
                    isChecked = app.isChecked,
                    isFavorite = app.isFavorite,
                    onClick = { updateApp(app) },
                    onLongClick = { triggerActionMode(app) })
            }, directions = getSwipeDirections(
                app, isSwipeToDismiss, rightSwipeAction, leftSwipeAction
            )
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun getSwipeDirections(
    app: ApplicationModel,
    isSwipeToDismiss: Boolean,
    rightSwipeAction: ApkActionsOptions,
    leftSwipeAction: ApkActionsOptions
): Set<DismissDirection> {
    if (!isSwipeToDismiss) return setOf()

    val swipeDirs = mutableSetOf<DismissDirection>()

    if (ApkActionsOptions.isOptionSupported(
            app, leftSwipeAction
        )
    ) swipeDirs.add(DismissDirection.EndToStart)

    if (ApkActionsOptions.isOptionSupported(
            app, rightSwipeAction
        )
    ) swipeDirs.add(DismissDirection.StartToEnd)

    return swipeDirs
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
            painter = painterResource(id = leftSwipeAction.icon),
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
            painter = painterResource(id = rightSwipeAction.icon),
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
                leftSwipeAction = ApkActionsOptions.SHARE
            ) { app, action -> Log.e(action.name, app.appPackageName) }
        }
    }
}