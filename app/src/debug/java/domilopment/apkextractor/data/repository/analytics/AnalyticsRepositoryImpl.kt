package domilopment.apkextractor.data.repository.analytics

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AnalyticsRepository {
    override fun setAnalyticsCollectionEnabled(boolean: Boolean) {
        Timber.tag("AnalyticsRepository").d("Analytics enabled : $boolean")
    }

    override fun setCrashlyticsCollectionEnabled(boolean: Boolean) {
        Timber.tag("AnalyticsRepository").d("Crashlytics enabled : $boolean")
    }

    override fun setPerformanceCollectionEnabled(boolean: Boolean) {
        Timber.tag("AnalyticsRepository").d("Performance enabled : $boolean")
    }

    override suspend fun delete() {
        Timber.tag("AnalyticsRepository").d("delete")
    }
}