package domilopment.apkextractor.ui.settings.interactions

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwipeLeft
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.settings.preferences.ListPreference
import domilopment.apkextractor.ui.settings.preferences.SeekBarPreference
import domilopment.apkextractor.ui.settings.preferences.SwitchPreferenceCompat
import domilopment.apkextractor.ui.settings.preferences.preferenceCategory
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemBottom
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemMiddle
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemTop

@Composable
fun SettingsInteractionsContent(
    rightSwipeAction: String,
    onRightSwipeAction: (String) -> Unit,
    leftSwipeAction: String,
    onLeftSwipeAction: (String) -> Unit,
    swipeActionCustomThreshold: Boolean,
    onSwipeActionCustomThreshold: (Boolean) -> Unit,
    swipeActionThresholdMod: Float,
    onSwipeActionThresholdMod: (Float) -> Unit,
) {
    LazyColumn(
        state = rememberLazyListState(),
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
            .union(WindowInsets(left = 8.dp, right = 8.dp)).asPaddingValues()
    ) {
        preferenceCategory(title = R.string.app_list_swipe_actions) {
            preferenceCategoryItemTop {
                ListPreference(
                    name = R.string.apk_swipe_action_right_title,
                    icon = Icons.Default.SwipeRight,
                    summary = R.string.apk_swipe_action_right_summary,
                    entries = R.array.apk_swipe_options_entries,
                    entryValues = R.array.apk_swipe_options_values,
                    state = rightSwipeAction,
                    onClick = onRightSwipeAction
                )
            }
            preferenceCategoryItemMiddle {
                ListPreference(
                    name = R.string.apk_swipe_action_left_title,
                    icon = Icons.Default.SwipeLeft,
                    summary = R.string.apk_swipe_action_left_summary,
                    entries = R.array.apk_swipe_options_entries,
                    entryValues = R.array.apk_swipe_options_values,
                    state = leftSwipeAction,
                    onClick = onLeftSwipeAction
                )
            }
        }
        preferenceCategory(title = R.string.advanced) {
            preferenceCategoryItemMiddle {
                SwitchPreferenceCompat(
                    name = R.string.apk_swipe_action_custom_threshold_title,
                    summary = R.string.apk_swipe_action_custom_threshold_summary,
                    state = swipeActionCustomThreshold,
                    onClick = onSwipeActionCustomThreshold
                )
            }
            preferenceCategoryItemBottom {
                SeekBarPreference(
                    enabled = swipeActionCustomThreshold,
                    name = R.string.apk_swipe_action_threshold_title,
                    summary = R.string.apk_swipe_action_threshold_summary,
                    min = 0f,
                    max = 100f,
                    steps = 100,
                    showValue = true,
                    state = swipeActionThresholdMod,
                    onValueChanged = onSwipeActionThresholdMod,
                )
            }
        }
    }
}