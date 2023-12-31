package domilopment.apkextractor.ui.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun Preference(
    @StringRes name: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    @StringRes iconDesc: Int? = null,
    @StringRes summary: Int? = null,
    isPreferenceVisible: Boolean = true,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Preference(
        name = stringResource(id = name),
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        iconDesc = iconDesc?.let { stringResource(id = it) },
        summary = summary?.let { stringResource(id = it) },
        isPreferenceVisible = isPreferenceVisible,
        onClick = onClick,
        trailingContent = trailingContent
    )
}

@Composable
fun Preference(
    name: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconDesc: String? = null,
    summary: String? = null,
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
            ListItem(headlineContent = {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    overflow = TextOverflow.Ellipsis
                )
            }, supportingContent = {
                if (summary != null) Text(text = summary)
            }, leadingContent = {
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