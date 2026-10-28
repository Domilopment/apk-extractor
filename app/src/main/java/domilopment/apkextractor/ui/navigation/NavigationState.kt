package domilopment.apkextractor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import domilopment.apkextractor.ui.navigation.entryDecorators.rememberNavigationAnalyticsNavEntryDecorator
import domilopment.apkextractor.ui.navigation.entryDecorators.rememberSharedViewModelStoreNavEntryDecorator

/**
 * Create a navigation state that persists config changes and process death.
 *
 * @param startRoute - The top level route to start on. This should also be in `topLevelRoutes`.
 * @param topLevelRoutes - The top level routes in the app.
 */
@Composable
fun <T : NavKey> rememberNavigationState(
    startRoute: T, topLevelRoutes: Set<T>
): NavigationState<T> {

    val topLevelRoute = rememberNavKey(startRoute)

    // Create a back stack for each top level route.
    val backStacks = topLevelRoutes.associateWith { key ->
        rememberNavBackStack(key) as NavBackStack<T>
    }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            startRoute = startRoute, topLevelRoute = topLevelRoute, backStacks = backStacks
        )
    }
}

/**
 * State holder for navigation state. This class does not modify its own state. It is designed
 * to be modified using the `Navigator` class.
 *
 * @param startRoute - the start route. The user will exit the app through this route.
 * @param topLevelRoute - the state object that backs the top level route.
 * @param backStacks - the back stacks for each top level route.
 */
class NavigationState<T : NavKey>(
    val startRoute: T, topLevelRoute: MutableState<T>, val backStacks: Map<T, NavBackStack<T>>
) {

    /**
     * The top level route.
     */
    var topLevelRoute: T by topLevelRoute

    /**
     * Convert the navigation state into `NavEntry`s that have been decorated with a
     * `SaveableStateHolder`.
     *
     * @param entryProvider - the entry provider used to convert the keys in the
     * back stacks to `NavEntry`s.
     */
    @Composable
    fun toDecoratedEntries(
        entryProvider: (T) -> NavEntry<T>
    ): List<NavEntry<T>> {

        // For each back stack, create a `SaveableStateHolder` decorator and use it to decorate
        // the entries from that stack. When backStacks changes, `rememberDecoratedNavEntries` will
        // be recomposed and a new list of decorated entries is returned.
        val decoratedEntries = backStacks.mapValues { (_, stack) ->
            val decorators = listOf<NavEntryDecorator<T>>(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberSharedViewModelStoreNavEntryDecorator(),
                rememberNavigationAnalyticsNavEntryDecorator(),
            )
            rememberDecoratedNavEntries(
                backStack = stack, entryDecorators = decorators, entryProvider = entryProvider
            )
        }

        // Only return the entries for the stacks that are currently in use.
        return getTopLevelRoutesInUse().flatMap { decoratedEntries[it] ?: emptyList() }
    }

    /**
     * Get the top level routes that are currently in use. The start route is always the first route
     * in the list. This means the user will always exit the app through the starting route
     * ("exit through home" pattern). The list will contain a maximum of one other route. This is a
     * design decision. In your app, you may wish to allow more than two top level routes to be
     * active.
     *
     * Note that even if a top level route is not in use its state is still retained.
     *
     * @return the current top level routes that are in use.
     */
    private fun getTopLevelRoutesInUse(): List<T> = if (topLevelRoute == startRoute) {
        listOf(startRoute)
    } else {
        listOf(startRoute, topLevelRoute)
    }
}