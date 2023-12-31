package domilopment.apkextractor.ui.settings

import androidx.annotation.StringRes
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun SwitchPreferenceCompat(
    icon: ImageVector? = null,
    @StringRes iconDesc: Int? = null,
    @StringRes name: Int,
    @StringRes summary: Int? = null,
    state: State<Boolean>,
    isVisible: Boolean = true,
    onClick: (Boolean) -> Unit
) {
    Preference(icon = icon,
        iconDesc = iconDesc,
        name = name,
        summary = summary,
        isPreferenceVisible = isVisible,
        onClick = { onClick(!state.value) },
        trailingContent = {
            Switch(
                checked = state.value, onCheckedChange = null
            )
        })
}