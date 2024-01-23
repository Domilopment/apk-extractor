package domilopment.apkextractor.ui.settings.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun BasePreference(
    name: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconDesc: String? = null,
    summary: @Composable () -> Unit,
    isPreferenceVisible: Boolean = true,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    if (isPreferenceVisible) Surface(
        color = Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        enabled = enabled,
        onClick = onClick,
    ) {
        Column {
            ListItem(
                headlineContent = name, supportingContent = summary, leadingContent = {
                    if (icon != null) Icon(
                        imageVector = icon,
                        contentDescription = iconDesc,
                        modifier = Modifier.size(24.dp)
                    ) else Spacer(modifier = Modifier.size(24.dp))
                }, trailingContent = trailingContent, colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                    headlineColor = if (enabled) MaterialTheme.colorScheme.surfaceTint else MaterialTheme.colorScheme.surfaceTint.copy(
                        alpha = 0.38f
                    ),
                    leadingIconColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.38f
                    ),
                    supportingColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.38f
                    ),
                    trailingIconColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.38f
                    )
                )
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}