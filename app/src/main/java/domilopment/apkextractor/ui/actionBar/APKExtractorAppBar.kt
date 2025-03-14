package domilopment.apkextractor.ui.actionBar

import android.app.Activity
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.ArcMode
import androidx.compose.animation.core.ExperimentalAnimationSpecApi
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.R
import domilopment.apkextractor.data.AppBarState
import domilopment.apkextractor.data.UiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun APKExtractorAppBar(
    appBarState: AppBarState,
    modifier: Modifier = Modifier,
    uiState: UiState,
    searchText: String,
    isAllItemsChecked: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onTriggerSearch: () -> Unit,
    onReturnUiMode: () -> Unit,
    onCheckAllItems: (Boolean) -> Unit,
    selectedApplicationsCount: Int
) {
    Column(modifier = Modifier.background(color = TopAppBarDefaults.topAppBarColors().containerColor)) {
        SharedTransitionLayout {
            AnimatedContent(targetState = uiState, transitionSpec = {
                fadeIn(
                    animationSpec = tween(durationMillis = 220, delayMillis = 90)
                ) togetherWith fadeOut(animationSpec = tween(durationMillis = 90))
            }, label = "Actionbar Content") { state ->
                when (state) {
                    UiState.Default -> DefaultAppBar(
                        appBarState = appBarState,
                        modifier,
                        onActionSearch = onTriggerSearch,
                        animatedVisibilityScope = this@AnimatedContent,
                        sharedTransitionScope = this@SharedTransitionLayout
                    )

                    UiState.Search -> SearchBar(
                        text = searchText,
                        onTextChange = onSearchQueryChanged,
                        onCloseClicked = onReturnUiMode,
                        onSearchClicked = onSearchQueryChanged,
                        animatedVisibilityScope = this@AnimatedContent,
                        sharedTransitionScope = this@SharedTransitionLayout
                    )

                    is UiState.ActionMode -> ActionModeBar(
                        modifier,
                        isAllItemsChecked,
                        selectedApplicationsCount,
                        onReturnUiMode,
                        onCheckAllItems
                    )
                }
            }
        }
        HorizontalDivider(
            thickness = Dp.Hairline, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
    }

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val view = LocalView.current
        if (!view.isInEditMode) {
            val color = TopAppBarDefaults.topAppBarColors().containerColor
            SideEffect {
                val window = (view.context as Activity).window
                window.statusBarColor = color.toArgb()
            }
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalAnimationSpecApi::class
)
@Composable
private fun DefaultAppBar(
    appBarState: AppBarState,
    modifier: Modifier = Modifier,
    onActionSearch: () -> Unit,
    animatedVisibilityScope: AnimatedContentScope,
    sharedTransitionScope: SharedTransitionScope
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
    }, navigationIcon = {
        if (appBarState.hasNavigationIcon) IconButton(
            onClick = appBarState.navigationIcon!!.onClick ?: {},
            enabled = appBarState.navigationIcon!!.onClick != null,
            colors = IconButtonDefaults.iconButtonColors(
                disabledContentColor = LocalContentColor.current
            ),
        ) {
            domilopment.apkextractor.ui.Icon(
                iconResource = appBarState.navigationIcon!!.icon,
                contentDescription = null,
            )
        }
    }, actions = {
        if (appBarState.isSearchable) with(sharedTransitionScope) {
            IconButton(
                onClick = onActionSearch, modifier = Modifier.sharedBounds(
                    rememberSharedContentState(key = "search_icon_bounds"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = BoundsTransform { initialBounds, targetBounds ->
                        keyframes {
                            durationMillis = AnimationConstants.DefaultDurationMillis
                            initialBounds at 0 using ArcMode.ArcLinear using LinearOutSlowInEasing
                            targetBounds at durationMillis
                        }
                    },
                    resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "search_icon"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                )
            }
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
    }, modifier = modifier, navigationIcon = {
        IconButton(onClick = onTriggerActionMode) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null
            )
        }
    }, actions = {
        Checkbox(
            checked = allItemsChecked, onCheckedChange = onCheckAllItems
        )
    })

    BackHandler {
        onTriggerActionMode()
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalAnimationSpecApi::class
)
@Composable
private fun SearchBar(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    onCloseClicked: () -> Unit,
    onSearchClicked: (String) -> Unit,
    animatedVisibilityScope: AnimatedContentScope,
    sharedTransitionScope: SharedTransitionScope
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }

    with(sharedTransitionScope) {
        Surface(
            modifier = modifier, color = TopAppBarDefaults.topAppBarColors().containerColor
        ) {
            Box(
                modifier = Modifier
                    .windowInsetsPadding(insets = TopAppBarDefaults.windowInsets)
                    .clipToBounds()
                    .heightIn(max = TopAppBarDefaults.TopAppBarExpandedHeight),
                contentAlignment = Alignment.CenterStart
            ) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .sharedBounds(
                            rememberSharedContentState(key = "search_icon_bounds"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = BoundsTransform { initialBounds, targetBounds ->
                                keyframes {
                                    durationMillis = AnimationConstants.DefaultDurationMillis
                                    initialBounds at 0 using ArcMode.ArcLinear using LinearOutSlowInEasing
                                    targetBounds at durationMillis
                                }
                            },
                            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                        ),
                    shape = RoundedCornerShape(64.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier
                            .clickable { focusRequester.requestFocus() }
                            .focusRequester(focusRequester)
                            .fillMaxWidth(),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current),
                        cursorBrush = SolidColor(LocalContentColor.current.copy(alpha = 0.6f)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            onSearchClicked(text)
                            keyboard?.hide()
                        }),
                        interactionSource = interactionSource,
                        decorationBox = { innerTextField ->
                            TextFieldDefaults.DecorationBox(
                                value = text,
                                innerTextField = innerTextField,
                                enabled = true,
                                singleLine = true,
                                visualTransformation = VisualTransformation.None,
                                interactionSource = interactionSource,
                                placeholder = {
                                    Text(
                                        text = stringResource(id = R.string.menu_search),
                                        color = LocalContentColor.current.copy(alpha = 0.6f)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Icon",
                                        modifier = Modifier.sharedElement(
                                            rememberSharedContentState(key = "search_icon"),
                                            animatedVisibilityScope = animatedVisibilityScope
                                        ),
                                        tint = LocalContentColor.current.copy(alpha = 0.6f)
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
                                            contentDescription = "Close Icon"
                                        )
                                    }
                                },
                                container = {},
                            )
                        })
                }
            }
        }
    }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    BackHandler {
        if (text.isNotEmpty()) {
            onTextChange("")
        } else {
            onCloseClicked()
        }
    }
}
