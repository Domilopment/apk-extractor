package domilopment.apkextractor.ui.settings.dataCollection

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.R
import domilopment.apkextractor.ui.settings.preferences.SwitchPreferenceCompat
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemBottom
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemMiddle
import domilopment.apkextractor.ui.settings.preferences.preferenceCategoryItemTop
import domilopment.apkextractor.ui.tabletLazyListInsets

@Composable
fun SettingsDataCollectionContent(
    analytics: Boolean,
    onAnalytics: (Boolean) -> Unit,
    crashlytics: Boolean,
    onCrashlytics: (Boolean) -> Unit,
    performance: Boolean,
    onPerformance: (Boolean) -> Unit,
) {
    LazyColumn(
        state = rememberLazyListState(), contentPadding = WindowInsets.tabletLazyListInsets.union(
            WindowInsets(left = 8.dp, right = 8.dp)
        ).asPaddingValues()
    ) {
        preferenceCategoryItemTop {
            SwitchPreferenceCompat(
                icon = Icons.Default.Analytics,
                name = R.string.data_collection_analytics,
                summary = R.string.data_collection_analytics_summary,
                state = analytics,
                onClick = onAnalytics
            )
        }
        preferenceCategoryItemMiddle {
            SwitchPreferenceCompat(
                icon = Icons.Default.BugReport,
                name = R.string.data_collection_crashlytics,
                summary = R.string.data_collection_crashlytics_summary,
                state = crashlytics,
                onClick = onCrashlytics
            )
        }
        preferenceCategoryItemBottom {
            SwitchPreferenceCompat(
                name = R.string.data_collection_perf,
                summary = R.string.data_collection_perf_summary,
                state = performance,
                onClick = onPerformance
            )
        }
    }
}
