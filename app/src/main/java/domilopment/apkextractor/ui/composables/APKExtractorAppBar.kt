package domilopment.apkextractor.ui.composables

import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.R
import domilopment.apkextractor.data.AppBarState
import domilopment.apkextractor.ui.composables.avtionMenu.ActionsMenu

@Composable
fun APKExtractorAppBar(
    appBarState: AppBarState,
    modifier: Modifier = Modifier,
    searchText: String,
    isSearchActive: Boolean,
    isAllItemsChecked: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onTriggerSearch: (Boolean) -> Unit,
    isActionModeActive: Boolean,
    onEndActionMode: () -> Unit,
    onCheckAllItems: (Boolean) -> Unit,
    selectedApplicationsCount: Int
) {
    Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.primary)) {
        AnimatedVisibility(
            visible = !isActionModeActive && !isSearchActive, enter = fadeIn(), exit = fadeOut()
        ) {
            DefaultAppBar(
                appBarState = appBarState,
                modifier,
                onActionSearch = { onTriggerSearch(true) })
        }
        AnimatedVisibility(
            visible = !isActionModeActive && isSearchActive, enter = fadeIn(), exit = fadeOut()
        ) {
            SearchBar(
                text = searchText, onTextChange = onSearchQueryChanged, onCloseClicked = {
                    if (searchText.isNotEmpty()) onSearchQueryChanged("") else onTriggerSearch(
                        false
                    )
                }, onSearchClicked = onSearchQueryChanged
            )
        }
        AnimatedVisibility(visible = isActionModeActive, enter = fadeIn(), exit = fadeOut()) {
            ActionModeBar(
                modifier,
                isAllItemsChecked,
                selectedApplicationsCount,
                onEndActionMode,
                onCheckAllItems
            )
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

    TopAppBar(title = { Text(text = stringResource(id = appBarState.title)) },
        modifier = modifier.onGloballyPositioned {
            barWidth = with(localDensity) { it.size.width.toDp() }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        navigationIcon = {
            if (appBarState.isBackArrow) IconButton(onClick = appBarState.onBackArrowClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        actions = {
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
                    maxVisibleItems = (barWidth * 0.4f / 48.dp).toInt(),
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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        elevation = AppBarDefaults.BottomAppBarElevation,
        color = MaterialTheme.colorScheme.primary
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            value = text,
            onValueChange = {
                onTextChange(it)
            },
            placeholder = {
                Text(
                    modifier = Modifier.alpha(ContentAlpha.medium),
                    text = stringResource(id = R.string.menu_search),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                textDecoration = TextDecoration.Underline
            ),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    modifier = Modifier.alpha(ContentAlpha.medium),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            },
            trailingIcon = {
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
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = {
                onSearchClicked(text)
            }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = ContentAlpha.medium),
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
