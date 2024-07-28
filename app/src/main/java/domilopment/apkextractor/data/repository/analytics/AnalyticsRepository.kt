package domilopment.apkextractor.data.repository.analytics

import android.content.Context
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.setConsent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.perf.FirebasePerformance
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

interface AnalyticsRepository {
    fun setAnalyticsCollectionEnabled(boolean: Boolean)
    fun setCrashlyticsCollectionEnabled(boolean: Boolean)
    fun setPerformanceCollectionEnabled(boolean: Boolean)
    suspend fun delete()
}

class AnalyticsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AnalyticsRepository {
    private val analytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics = FirebaseCrashlytics.getInstance()
    private val performance = FirebasePerformance.getInstance()
    private val installations = FirebaseInstallations.getInstance()

    override fun setAnalyticsCollectionEnabled(boolean: Boolean) {
        analytics.setAnalyticsCollectionEnabled(boolean)
        analytics.setConsent {
            analyticsStorage =
                if (boolean) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED
        }
        if (!boolean) analytics.resetAnalyticsData()
    }

    override fun setCrashlyticsCollectionEnabled(boolean: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(boolean)
    }

    override fun setPerformanceCollectionEnabled(boolean: Boolean) {
        performance.isPerformanceCollectionEnabled = boolean
    }

    override suspend fun delete() {
        installations.delete().addOnCompleteListener { task ->
            if (task.isComplete) {
                Timber.tag("firebase-Installations").d("Installation deleted")
                Toast.makeText(context, "Firebase-Installation-ID deleted", Toast.LENGTH_LONG)
                    .show()
            } else {
                Timber.tag("firebase-Installations").d("Unable to delete Installation")
                Toast.makeText(
                    context,
                    "Unable to delete Firebase-Installation-ID, try again later or contact the developer",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}