package domilopment.apkextractor.ui

import android.util.TypedValue
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * Load a color resource.
 * @param attrId Id of attr associated with the color
 * @param defaultColor fallback color if res could not be resolved
 * @return Color value associated with the attribute
 */
@Composable
@ReadOnlyComposable
fun attrColorResource(attrId: Int, defaultColor: Color): Color {
    val context = LocalContext.current
    val typedValue = TypedValue()
    val valid = context.theme.resolveAttribute(attrId, typedValue, true)
    return if (valid) Color(
        ContextCompat.getColor(context, typedValue.resourceId)
    ) else defaultColor
}

@Composable
@ReadOnlyComposable
fun getDarkModeConfiguration(theme: Int): Boolean {
    return when (theme) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> isSystemInDarkTheme()
    }
}

@ReadOnlyComposable
@Composable
fun PaddingValues.add(paddingValues: PaddingValues): PaddingValues {
    val localLayoutDirection = LocalLayoutDirection.current
    val start =
        this.calculateStartPadding(localLayoutDirection) + paddingValues.calculateStartPadding(
            localLayoutDirection
        )
    val top = this.calculateTopPadding() + paddingValues.calculateTopPadding()
    val end = this.calculateEndPadding(localLayoutDirection) + paddingValues.calculateEndPadding(
        localLayoutDirection
    )
    val bottom = this.calculateBottomPadding() + paddingValues.calculateBottomPadding()
    return PaddingValues(start = start, top = top, end = end, bottom = bottom)
}

@OptIn(ExperimentalLayoutApi::class)
val WindowInsets.Companion.tabletLazyListInsets: WindowInsets
    @Composable get() = if (DeviceTypeUtils.isTabletBars && !WindowInsets.isImeVisible) WindowInsets.navigationBars.only(
        WindowInsetsSides.Bottom
    ) else WindowInsets(left = 0.dp, top = 0.dp, right = 0.dp, bottom = 0.dp)
