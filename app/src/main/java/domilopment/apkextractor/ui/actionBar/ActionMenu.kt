package domilopment.apkextractor.ui.actionBar

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import domilopment.apkextractor.utils.toInt
import kotlin.math.max
import kotlin.math.min

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
            hasSearchIcon: Boolean = false,
            maxVisibleItems: Int = Int.MAX_VALUE,
        ): Int {
            val alwaysShownItems = items.count { it is IconMenuItem.AlwaysShown }
            val ifRoomItems = items.count { it is IconMenuItem.ShownIfRoom }
            val hasNeverShownItems = items.any { it is NeverShown }

            // An overflow icon is needed if there are "NeverShown" items,
            // or if there are "IfRoom" items that won't fit alongside the "AlwaysShown" items.
            val hasOverflowIcon =
                hasNeverShownItems || (ifRoomItems > 0 && alwaysShownItems + ifRoomItems + hasSearchIcon.toInt() > maxVisibleItems)

            // Calculate the slots already used by "AlwaysShown" items, the search icon, and the potential overflow menu icon.
            val usedSlots = alwaysShownItems + hasSearchIcon.toInt() + hasOverflowIcon.toInt()

            // Calculate the remaining available slots for "IfRoom" items.
            val availableSlots = max(0, maxVisibleItems - usedSlots)

            // The number of "IfRoom" items that can be shown is the smaller value between
            // the actual number of "IfRoom" items and the available slots.
            val shownIfRoomItems = min(ifRoomItems, availableSlots)

            // The total number of visible items is the sum of the used slots
            // (AlwaysShown, search, overflow) and the "if room" items that fit.
            return usedSlots + shownIfRoomItems
        }
    }
}
