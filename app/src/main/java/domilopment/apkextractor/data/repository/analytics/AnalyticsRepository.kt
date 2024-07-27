package domilopment.apkextractor.data.repository.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.perf.FirebasePerformance
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface AnalyticsRepository {
    fun setAnalyticsCollectionEnabled(boolean: Boolean)
    fun setCrashlyticsCollectionEnabled(boolean: Boolean)
    fun setPerformanceCollectionEnabled(boolean: Boolean)
    fun delete()
}

class AnalyticsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : AnalyticsRepository {
    private val analytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private val performance = FirebasePerformance.getInstance()
    private val installations = FirebaseInstallations.getInstance()

    override fun setAnalyticsCollectionEnabled(boolean: Boolean) {
        analytics.setAnalyticsCollectionEnabled(boolean)
    }

    override fun setCrashlyticsCollectionEnabled(boolean: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(boolean)
    }

    override fun setPerformanceCollectionEnabled(boolean: Boolean) {
        performance.isPerformanceCollectionEnabled = boolean
    }

    override fun delete() {
        installations.delete()
    }

}