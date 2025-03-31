/*
 * Copyright 2023 The Android Open Source Project
 * Modifications Copyright 2025 Dominic Narwutsch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This file is based on:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3-adaptive-navigation-suite/src/commonMain/kotlin/androidx/compose/material3/adaptive/navigationsuite/NavigationSuiteScaffold.kt
 *
 * Modifications include:
 * - Changed NavigationSuite to use AnimatedContent with NavigationSuiteType
 * - Changed measurement of content and bars in NavigationSuiteScaffoldLayout
 * - Changed name of components to use Animated* at the start
 * - Removed NavigationDrawer implementation
 * - Fixed Crash when measuring the width with NavigationSuiteType.NavigationDrawer and Animations.
 */

package domilopment.apkextractor.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collection.MutableVector
import androidx.compose.runtime.collection.mutableVectorOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.util.fastFirst

/**
 * The Navigation Suite Scaffold wraps the provided content and places the adequate provided
 * navigation component on the screen according to the current [NavigationSuiteType].
 *
 * Example default usage:
 *
 * @sample androidx.compose.material3.adaptive.navigationsuite.samples.NavigationSuiteScaffoldSample
 * Example custom configuration usage:
 *
 * @sample androidx.compose.material3.adaptive.navigationsuite.samples.NavigationSuiteScaffoldCustomConfigSample
 *
 * @param navigationSuiteItems the navigation items to be displayed
 * @param modifier the [Modifier] to be applied to the navigation suite scaffold
 * @param layoutType the current [NavigationSuiteType]. Defaults to
 *   [NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo]
 * @param navigationSuiteColors [NavigationSuiteColors] that will be used to determine the container
 *   (background) color of the navigation component and the preferred color for content inside the
 *   navigation component
 * @param containerColor the color used for the background of the navigation suite scaffold,
 *   including the passed [content] composable. Use [Color.Transparent] to have no color
 * @param contentColor the preferred color to be used for typography and iconography within the
 *   passed in [content] lambda inside the navigation suite scaffold.
 * @param content the content of your screen
 */
@Composable
fun AnimatedNavigationSuiteScaffold(
    navigationSuiteItems: NavigationSuiteScope.() -> Unit,
    modifier: Modifier = Modifier,
    layoutType: NavigationSuiteType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
        WindowAdaptiveInfoDefault
    ),
    navigationSuiteColors: NavigationSuiteColors = NavigationSuiteDefaults.colors(),
    containerColor: Color = NavigationSuiteScaffoldDefaults.containerColor,
    contentColor: Color = NavigationSuiteScaffoldDefaults.contentColor,
    navigationRailHeader: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable () -> Unit = {},
) {
    Surface(modifier = modifier, color = containerColor, contentColor = contentColor) {
        AnimatedNavigationSuiteScaffoldLayout(navigationSuite = {
            AnimatedNavigationSuite(
                layoutType = layoutType,
                colors = navigationSuiteColors,
                navigationRailHeader = navigationRailHeader,
                content = navigationSuiteItems
            )
        }, layoutType = layoutType, content = {
            Box(
                Modifier.consumeWindowInsets(
                    when (layoutType) {
                        NavigationSuiteType.NavigationBar -> NavigationBarDefaults.windowInsets.only(
                            WindowInsetsSides.Bottom
                        )

                        NavigationSuiteType.NavigationRail -> NavigationRailDefaults.windowInsets.only(
                            WindowInsetsSides.Start
                        )

                        else -> NoWindowInsets
                    }
                )
            ) {
                content()
            }
        })
    }
}

/**
 * Layout for a [AnimatedNavigationSuiteScaffold]'s content. This function wraps the [content] and places
 * the [navigationSuite] component according to the given [layoutType].
 *
 * The usage of this function is recommended when you need some customization that is not viable via
 * the use of [AnimatedNavigationSuiteScaffold]. Example usage:
 *
 * @sample androidx.compose.material3.adaptive.navigationsuite.samples.NavigationSuiteScaffoldCustomNavigationRail
 *
 * @param navigationSuite the navigation component to be displayed, typically [AnimatedNavigationSuite]
 * @param layoutType the current [NavigationSuiteType]. Defaults to
 *   [NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo]
 * @param content the content of your screen
 */
@Composable
fun AnimatedNavigationSuiteScaffoldLayout(
    navigationSuite: @Composable () -> Unit,
    layoutType: NavigationSuiteType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
        WindowAdaptiveInfoDefault
    ),
    content: @Composable () -> Unit = {}
) {
    Layout({
        // Wrap the navigation suite and content composables each in a Box to not propagate the
        // parent's (Surface) min constraints to its children (see b/312664933).
        Box(Modifier.layoutId(NavigationSuiteLayoutIdTag)) { navigationSuite() }
        Box(Modifier.layoutId(ContentLayoutIdTag)) { content() }
    }) { measurables, constraints ->
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        // Find the navigation suite composable through it's layoutId tag
        val navigationPlaceable =
            measurables.fastFirst { it.layoutId == NavigationSuiteLayoutIdTag }
                .measure(looseConstraints)
        val isNavigationBar = layoutType == NavigationSuiteType.NavigationBar
        val isNavigation = layoutType != NavigationSuiteType.None
        val layoutHeight = constraints.maxHeight
        val layoutWidth = constraints.maxWidth
        // Find the content composable through it's layoutId tag
        val contentPlaceable = measurables.fastFirst { it.layoutId == ContentLayoutIdTag }.measure(
            if (isNavigationBar) {
                val height = layoutHeight - navigationPlaceable.height
                constraints.copy(
                    minHeight = height, maxHeight = height
                )
            } else if (isNavigation) {
                val width = layoutWidth - navigationPlaceable.width
                constraints.copy(
                    minWidth = width, maxWidth = width
                )
            } else constraints.copy(
                minWidth = layoutWidth,
                maxWidth = layoutWidth,
                minHeight = layoutHeight,
                maxHeight = layoutHeight
            )
        )

        layout(layoutWidth, layoutHeight) {
            if (isNavigationBar) {
                // Place content above the navigation component.
                contentPlaceable.placeRelative(0, 0)
                // Place the navigation component at the bottom of the screen.
                navigationPlaceable.placeRelative(0, layoutHeight - (navigationPlaceable.height))
            } else if (isNavigation) {
                // Place the navigation component at the start of the screen.
                navigationPlaceable.placeRelative(0, 0)
                // Place content to the side of the navigation component.
                contentPlaceable.placeRelative((navigationPlaceable.width), 0)
            } else {
                // Place content in the center of the screen.
                contentPlaceable.placeRelative(0, 0)
            }
        }
    }
}

/**
 * The default Material navigation component according to the current [NavigationSuiteType] to be
 * used with the [AnimatedNavigationSuiteScaffold].
 *
 * For specifics about each navigation component, see [NavigationBar], [NavigationRail], and
 * [PermanentDrawerSheet].
 *
 * @param modifier the [Modifier] to be applied to the navigation component
 * @param layoutType the current [NavigationSuiteType] of the [AnimatedNavigationSuiteScaffold]. Defaults to
 *   [NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo]
 * @param colors [NavigationSuiteColors] that will be used to determine the container (background)
 *   color of the navigation component and the preferred color for content inside the navigation
 *   component
 * @param content the content inside the current navigation component, typically
 *   [NavigationSuiteScope.item]s
 */
@Composable
fun AnimatedNavigationSuite(
    modifier: Modifier = Modifier,
    layoutType: NavigationSuiteType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
        WindowAdaptiveInfoDefault
    ),
    colors: NavigationSuiteColors = NavigationSuiteDefaults.colors(),
    navigationRailHeader: @Composable (ColumnScope.() -> Unit)? = null,
    content: NavigationSuiteScope.() -> Unit
) {
    val scope by rememberStateOfItems(content)
    // Define defaultItemColors here since we can't set NavigationSuiteDefaults.itemColors() as a
    // default for the colors param of the NavigationSuiteScope.item non-composable function.
    val defaultItemColors = NavigationSuiteDefaults.itemColors()

    AnimatedContent(targetState = layoutType, modifier = modifier, transitionSpec = {
        val enterTransition = if (targetState == NavigationSuiteType.NavigationBar) {
            slideInVertically(
                animationSpec = tween(
                    durationMillis = 100,
                    delayMillis = 100,
                ), initialOffsetY = { it }) + fadeIn(
                animationSpec = tween(
                    durationMillis = 100,
                    delayMillis = 100,
                )
            )
        } else if (targetState == NavigationSuiteType.NavigationRail) {
            slideInHorizontally(
                animationSpec = tween(
                    durationMillis = 100,
                    delayMillis = 100,
                ), initialOffsetX = { -it }) + fadeIn(
                animationSpec = tween(
                    durationMillis = 100,
                    delayMillis = 100,
                )
            )
        } else {
            EnterTransition.None
        }
        val exitTransition = if (initialState == NavigationSuiteType.NavigationBar) {
            slideOutVertically(
                animationSpec = tween(
                    durationMillis = 100, easing = LinearOutSlowInEasing
                ), targetOffsetY = { it }) + fadeOut(
                animationSpec = tween(durationMillis = 100, easing = LinearOutSlowInEasing)
            )
        } else if (initialState == NavigationSuiteType.NavigationRail) {
            slideOutHorizontally(
                animationSpec = tween(
                    durationMillis = 100, easing = LinearOutSlowInEasing
                ), targetOffsetX = { -it }) + fadeOut(
                animationSpec = tween(durationMillis = 100, easing = LinearOutSlowInEasing)
            )
        } else {
            ExitTransition.None
        }

        enterTransition togetherWith exitTransition
    }) { type ->
        when (type) {
            NavigationSuiteType.NavigationBar -> {
                NavigationBar(
                    containerColor = colors.navigationBarContainerColor,
                    contentColor = colors.navigationBarContentColor
                ) {
                    scope.itemList.forEach {
                        NavigationBarItem(
                            modifier = it.modifier,
                            selected = it.selected,
                            onClick = it.onClick,
                            icon = { NavigationItemIcon(icon = it.icon, badge = it.badge) },
                            enabled = it.enabled,
                            label = it.label,
                            alwaysShowLabel = it.alwaysShowLabel,
                            colors = it.colors?.navigationBarItemColors
                                ?: defaultItemColors.navigationBarItemColors,
                            interactionSource = it.interactionSource
                        )
                    }
                }
            }

            NavigationSuiteType.NavigationRail -> {
                NavigationRail(
                    containerColor = colors.navigationRailContainerColor,
                    contentColor = colors.navigationRailContentColor,
                    header = navigationRailHeader
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    scope.itemList.forEach {
                        NavigationRailItem(
                            modifier = it.modifier,
                            selected = it.selected,
                            onClick = it.onClick,
                            icon = { NavigationItemIcon(icon = it.icon, badge = it.badge) },
                            enabled = it.enabled,
                            label = it.label,
                            alwaysShowLabel = it.alwaysShowLabel,
                            colors = it.colors?.navigationRailItemColors
                                ?: defaultItemColors.navigationRailItemColors,
                            interactionSource = it.interactionSource
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            else -> {
                /* Do nothing. */
            }
        }
    }
}

/** The scope associated with the [NavigationSuiteScope]. */
sealed interface NavigationSuiteScope {

    /**
     * This function sets the parameters of the default Material navigation item to be used with the
     * Navigation Suite Scaffold. The item is called in [AnimatedNavigationSuite], according to the current
     * [NavigationSuiteType].
     *
     * For specifics about each item component, see [NavigationBarItem], [NavigationRailItem].
     *
     * @param selected whether this item is selected
     * @param onClick called when this item is clicked
     * @param icon icon for this item, typically an [Icon]
     * @param modifier the [Modifier] to be applied to this item
     * @param enabled controls the enabled state of this item. When `false`, this component will not
     *   respond to user input, and it will appear visually disabled and disabled to accessibility services.
     * @param label the text label for this item
     * @param alwaysShowLabel whether to always show the label for this item. If `false`, the label
     *   will only be shown when this item is selected.
     * @param badge optional badge to show on this item
     * @param colors [NavigationSuiteItemColors] that will be used to resolve the colors used for
     *   this item in different states. If null, [NavigationSuiteDefaults.itemColors] will be used.
     * @param interactionSource an optional hoisted [MutableInteractionSource] for observing and
     *   emitting [Interaction]s for this item. You can use this to change the item's appearance or
     *   preview the item in different states. Note that if `null` is provided, interactions will
     *   still happen internally.
     */
    fun item(
        selected: Boolean,
        onClick: () -> Unit,
        icon: @Composable () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        label: @Composable (() -> Unit)? = null,
        alwaysShowLabel: Boolean = true,
        badge: (@Composable () -> Unit)? = null,
        colors: NavigationSuiteItemColors? = null,
        interactionSource: MutableInteractionSource? = null
    )
}

internal val WindowAdaptiveInfoDefault
    @Composable get() = currentWindowAdaptiveInfo()

private interface NavigationSuiteItemProvider {
    val itemsCount: Int
    val itemList: MutableVector<NavigationSuiteItem>
}

private class NavigationSuiteItem(
    val selected: Boolean,
    val onClick: () -> Unit,
    val icon: @Composable () -> Unit,
    val modifier: Modifier,
    val enabled: Boolean,
    val label: @Composable (() -> Unit)?,
    val alwaysShowLabel: Boolean,
    val badge: (@Composable () -> Unit)?,
    val colors: NavigationSuiteItemColors?,
    val interactionSource: MutableInteractionSource?
)

private class NavigationSuiteScopeImpl : NavigationSuiteScope, NavigationSuiteItemProvider {

    override fun item(
        selected: Boolean,
        onClick: () -> Unit,
        icon: @Composable () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        label: @Composable (() -> Unit)?,
        alwaysShowLabel: Boolean,
        badge: (@Composable () -> Unit)?,
        colors: NavigationSuiteItemColors?,
        interactionSource: MutableInteractionSource?
    ) {
        itemList.add(
            NavigationSuiteItem(
                selected = selected,
                onClick = onClick,
                icon = icon,
                modifier = modifier,
                enabled = enabled,
                label = label,
                alwaysShowLabel = alwaysShowLabel,
                badge = badge,
                colors = colors,
                interactionSource = interactionSource
            )
        )
    }

    override val itemList: MutableVector<NavigationSuiteItem> = mutableVectorOf()

    override val itemsCount: Int
        get() = itemList.size
}

@Composable
private fun rememberStateOfItems(
    content: NavigationSuiteScope.() -> Unit
): State<NavigationSuiteItemProvider> {
    val latestContent = rememberUpdatedState(content)
    return remember { derivedStateOf { NavigationSuiteScopeImpl().apply(latestContent.value) } }
}

@Composable
private fun NavigationItemIcon(
    icon: @Composable () -> Unit,
    badge: (@Composable () -> Unit)? = null,
) {
    if (badge != null) {
        BadgedBox(badge = { badge.invoke() }) { icon() }
    } else {
        icon()
    }
}

private val NoWindowInsets = WindowInsets(0, 0, 0, 0)

private const val NavigationSuiteLayoutIdTag = "navigationSuite"
private const val ContentLayoutIdTag = "content"
