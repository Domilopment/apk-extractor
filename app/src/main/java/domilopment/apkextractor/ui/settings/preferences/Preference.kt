package domilopment.apkextractor.ui.settings.preferences

import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

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
    BasePreference(
        name = {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        iconDesc = iconDesc,
        summary = {
            if (summary != null) Text(text = summary)
        },
        isPreferenceVisible = isPreferenceVisible,
        onClick = onClick,
        trailingContent = trailingContent
    )
}

@Composable
fun Preference(
    name: AnnotatedString,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconDesc: String? = null,
    summary: AnnotatedString? = null,
    isPreferenceVisible: Boolean = true,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    BasePreference(
        name = {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        iconDesc = iconDesc,
        summary = {
            if (summary != null) Text(text = summary)
        },
        isPreferenceVisible = isPreferenceVisible,
        onClick = onClick,
        trailingContent = trailingContent
    )
}