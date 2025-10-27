package domilopment.apkextractor.ui.actionBar

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.max

/**
 * Based on implementation by Francesc Vilarino Guell
 * https://fvilarino.medium.com/creating-a-reusable-actions-menu-in-jetpack-compose-95aec8eeb493
 */
sealed interface ActionMenuItem {
    @get:StringRes
    val labelRes: Int
    val onClick: () -> Unit

    sealed interface IconMenuItem : ActionMenuItem {
        val icon: ImageVector
        val contentDescription: String?

        data class AlwaysShown(
            override val labelRes: Int,
            override val contentDescription: String?,
            override val onClick: () -> Unit,
            override val icon: ImageVector,
        ) : IconMenuItem

        data class ShownIfRoom(
            override val labelRes: Int,
            override val contentDescription: String?,
            override val onClick: () -> Unit,
            override val icon: ImageVector,
        ) : IconMenuItem
    }

    data class NeverShown(
        override val labelRes: Int,
        override val onClick: () -> Unit,
    ) : ActionMenuItem

    companion object {
        fun calculateShownItems(
            items: List<ActionMenuItem>,
            maxVisibleItems: Int = Int.MAX_VALUE,
        ): Int {
            val alwaysShownItems = items.filterIsInstance<IconMenuItem.AlwaysShown>().size
            return max(maxVisibleItems, alwaysShownItems)
        }
    }
}
