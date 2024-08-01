package domilopment.apkextractor.data.repository.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext

class FirebaseAnalyticsHelper(@ApplicationContext context: Context) : AnalyticsHelper {
    private val analytics = FirebaseAnalytics.getInstance(context)

    override fun logEvent(event: String, params: Bundle) {
        analytics.logEvent(event, params)
    }
}