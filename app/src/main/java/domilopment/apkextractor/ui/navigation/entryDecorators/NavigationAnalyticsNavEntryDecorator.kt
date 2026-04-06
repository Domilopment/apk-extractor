package domilopment.apkextractor.ui.navigation.entryDecorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntryDecorator
import domilopment.apkextractor.MainActivity
import domilopment.apkextractor.data.repository.analytics.LocalAnalyticsHelper
import domilopment.apkextractor.data.repository.analytics.logScreenView
import timber.log.Timber

@Composable
fun <T : Any> rememberNavigationAnalyticsNavEntryDecorator(): NavigationAnalyticsNavEntryDecorator<T> {
    return remember { NavigationAnalyticsNavEntryDecorator() }
}

class NavigationAnalyticsNavEntryDecorator<T : Any> : NavEntryDecorator<T>(
    decorate = { entry ->
        val analytics = LocalAnalyticsHelper.current
        analytics.logScreenView(entry.contentKey.toString(), MainActivity::class.simpleName)
        entry.Content()
    },
    onPop = { contentKey ->
        Timber.tag("NavigationAnalyticsNavEntryDecorator").d("entry with $contentKey was popped")
    },
)