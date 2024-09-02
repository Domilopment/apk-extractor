package domilopment.apkextractor.ui.actionBar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.R
import domilopment.apkextractor.data.AppBarState
import domilopment.apkextractor.data.UiMode
import domilopment.apkextractor.ui.keyboardAsState

@Composable
fun APKExtractorAppBar(
    appBarState: AppBarState,
    modifier: Modifier = Modifier,
    uiMode: UiMode,
    searchText: String,
    isAllItemsChecked: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onTriggerSearch: () -> Unit,
    onReturnUiMode: () -> Unit,
    onCheckAllItems: (Boolean) -> Unit,
    selectedApplicationsCount: Int
) {
    Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.primary)) {
        AnimatedContent(targetState = uiMode, transitionSpec = {
            fadeIn() togetherWith fadeOut()
        }, label = "Actionbar Content") { uiMode ->
            when (uiMode) {
                UiMode.Home -> DefaultAppBar(
                    appBarState = appBarState, modifier, onActionSearch = onTriggerSearch
                )

                UiMode.Search -> SearchBar(
                    text = searchText, onTextChange = onSearchQueryChanged, onCloseClicked = {
                        if (searchText.isNotEmpty()) onSearchQueryChanged("") else onReturnUiMode()
                    }, onSearchClicked = onSearchQueryChanged
                )

                is UiMode.Action -> ActionModeBar(
                    modifier,
                    isAllItemsChecked,
                    selectedApplicationsCount,
                    onReturnUiMode,
                    onCheckAllItems
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultAppBar(
    appBarState: AppBarState, modifier: Modifier = Modifier, onActionSearch: () -> Unit
) {
    val localDensity = LocalDensity.current

    var menuExpanded by remember {
        mutableStateOf(false)
    }

    var barWidth by remember {
        mutableStateOf(0.dp)
    }

    val visibleItems by remember {
        derivedStateOf {
            (barWidth * 0.4f / 48.dp).toInt()
        }
    }

    TopAppBar(title = {
        Text(
            text = stringResource(id = appBarState.title),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }, modifier = modifier.onGloballyPositioned {
        barWidth = with(localDensity) { it.size.width.toDp() }
    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary
    ), navigationIcon = {
        if (appBarState.isBackArrow) IconButton(onClick = appBarState.onBackArrowClick!!) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }, actions = {
        if (appBarState.isSearchable) IconButton(onClick = onActionSearch) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        appBarState.actions.takeIf { it.isNotEmpty() }?.let { action ->
            ActionsMenu(
                items = action,
                hasSearchIcon = appBarState.isSearchable,
                isOpen = menuExpanded,
                onToggleOverflow = { menuExpanded = it },
                maxVisibleItems = visibleItems,
            )
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionModeBar(
    modifier: Modifier = Modifier,
    allItemsChecked: Boolean,
    selectedApplicationsCount: Int,
    onTriggerActionMode: () -> Unit,
    onCheckAllItems: (Boolean) -> Unit
) {
    TopAppBar(title = {
        Text(
            text = stringResource(
                id = R.string.action_mode_title, selectedApplicationsCount
            )
        )
    }, modifier = modifier, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary
    ), navigationIcon = {
        IconButton(onClick = onTriggerActionMode) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }, actions = {
        Checkbox(
            checked = allItemsChecked,
            onCheckedChange = onCheckAllItems,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.onPrimary,
                uncheckedColor = MaterialTheme.colorScheme.onPrimary,
                checkmarkColor = MaterialTheme.colorScheme.primary
            )
        )
    })

    BackHandler {
        onTriggerActionMode()
    }
}

@Composable
private fun SearchBar(
    text: String,
    onTextChange: (String) -> Unit,
    onCloseClicked: () -> Unit,
    onSearchClicked: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isKeyboardOpen by keyboardAsState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.primary
    ) {
        TextField(modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null && !isKeyboardOpen) {
                        focusManager.clearFocus()
                        focusRequester.requestFocus()
                        keyboard?.show()
                    }
                }
            }, value = text, onValueChange = {
            onTextChange(it)
        }, placeholder = {
            Text(
                text = stringResource(id = R.string.menu_search),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            )
        }, textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            textDecoration = TextDecoration.Underline
        ), singleLine = true, leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            )
        }, trailingIcon = {
            IconButton(onClick = {
                if (text.isNotEmpty()) {
                    onTextChange("")
                } else {
                    onCloseClicked()
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Icon",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }, keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ), keyboardActions = KeyboardActions(onSearch = {
            onSearchClicked(text)
            focusManager.clearFocus()
        }), colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
        )
        )

        LaunchedEffect(focusRequester) {
            focusRequester.requestFocus()
            keyboard?.show()
        }

        BackHandler {
            onCloseClicked()
        }
    }
}
