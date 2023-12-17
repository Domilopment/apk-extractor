package domilopment.apkextractor.ui.composables.avtionMenu

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource

/**
 * Based on implementation by Francesc Vilarino Guell
 * https://fvilarino.medium.com/creating-a-reusable-actions-menu-in-jetpack-compose-95aec8eeb493
 */
sealed interface ActionMenuItem {
    val titleRes: Int
    val onClick: () -> Unit

    sealed interface IconMenuItem : ActionMenuItem {
        val icon: ImageVector
        val contentDescription: String?

        data class AlwaysShown(
            override val titleRes: Int,
            override val contentDescription: String?,
            override val onClick: () -> Unit,
            override val icon: ImageVector,
        ) : IconMenuItem

        data class ShownIfRoom(
            override val titleRes: Int,
            override val contentDescription: String?,
            override val onClick: () -> Unit,
            override val icon: ImageVector,
        ) : IconMenuItem
    }

    data class NeverShown(
        override val titleRes: Int,
        override val onClick: () -> Unit,
    ) : ActionMenuItem
}

@Composable
fun ActionsMenu(
    items: List<ActionMenuItem>,
    hasSearchIcon: Boolean,
    isOpen: Boolean,
    onToggleOverflow: (Boolean) -> Unit,
    maxVisibleItems: Int
) {
    val menuItems = remember(
        key1 = items,
        key2 = maxVisibleItems,
    ) {
        splitMenuItems(items, hasSearchIcon, maxVisibleItems)
    }

    menuItems.alwaysShownItems.forEach { item ->
        IconButton(onClick = item.onClick) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.contentDescription,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    if (menuItems.overflowItems.isNotEmpty()) {
        Box {
            IconButton(onClick = { onToggleOverflow(true) }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Overflow",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            DropdownMenu(
                expanded = isOpen,
                onDismissRequest = { onToggleOverflow(false) },
            ) {
                menuItems.overflowItems.forEach { item ->
                    DropdownMenuItem(text = {
                        Text(text = stringResource(id = item.titleRes))
                    }, onClick = {
                        item.onClick()
                        onToggleOverflow(false)
                    })
                }
            }
        }
    }
}

private data class MenuItems(
    val alwaysShownItems: List<ActionMenuItem.IconMenuItem>,
    val overflowItems: List<ActionMenuItem>,
)

private fun splitMenuItems(
    items: List<ActionMenuItem>,
    hasSearchIcon: Boolean,
    maxVisibleItems: Int,
): MenuItems {
    val alwaysShownItems: MutableList<ActionMenuItem.IconMenuItem> =
        items.filterIsInstance<ActionMenuItem.IconMenuItem.AlwaysShown>().toMutableList()
    val ifRoomItems: MutableList<ActionMenuItem.IconMenuItem> =
        items.filterIsInstance<ActionMenuItem.IconMenuItem.ShownIfRoom>().toMutableList()
    val overflowItems = items.filterIsInstance<ActionMenuItem.NeverShown>()

    val hasSearchIconInt = if (hasSearchIcon) 1 else 0
    val hasOverflow =
        overflowItems.isNotEmpty() || (alwaysShownItems.size + ifRoomItems.size + hasSearchIconInt) > maxVisibleItems
    val usedSlots = alwaysShownItems.size + (if (hasOverflow) 1 else 0) + hasSearchIconInt
    val availableSlots = maxVisibleItems - usedSlots
    if (availableSlots > 0 && ifRoomItems.isNotEmpty()) {
        val visible = ifRoomItems.subList(0, availableSlots.coerceAtMost(ifRoomItems.size))
        alwaysShownItems.addAll(visible)
        ifRoomItems.removeAll(visible)
    }

    return MenuItems(
        alwaysShownItems = alwaysShownItems,
        overflowItems = ifRoomItems + overflowItems,
    )
}