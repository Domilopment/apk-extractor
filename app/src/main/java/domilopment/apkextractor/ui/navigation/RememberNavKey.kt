package domilopment.apkextractor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.serialization.NavKeySerializer

/**
 * Remember a [MutableState] holding a [NavKey] that persists config changes and process death.
 *
 * This is similar to `rememberNavBackStack`, but for a single key. It's useful when you need to
 * track a key alongside your back stacks, such as the current top level route when using
 * multiple back stacks:
 *
 * ```kotlin
 * var topLevelRoute by rememberNavKey(RouteA)
 * val backStacks = mapOf(
 *     RouteA to rememberNavBackStack(RouteA),
 *     RouteB to rememberNavBackStack(RouteB)
 * )
 * ```
 *
 * The key is saved using [NavKeySerializer], so it must be annotated with `@Serializable`.
 *
 * Note that the state is typed as [NavKey] rather than the type of the key you pass in. This means
 * you can set it to any other key, e.g. `topLevelRoute = RouteB`.
 *
 * @param initialKey The key to use when the state is first created.
 * @return A [MutableState] whose value is saved and restored automatically.
 */
@Composable
fun <T: NavKey> rememberNavKey(initialKey: T): MutableState<T> {
    return rememberSerializable(stateSerializer = NavKeySerializer()) {
        mutableStateOf(initialKey)
    }
}