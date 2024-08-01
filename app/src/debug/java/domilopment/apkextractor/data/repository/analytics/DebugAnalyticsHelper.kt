package domilopment.apkextractor.data.repository.analytics

import android.os.Bundle
import timber.log.Timber

class DebugAnalyticsHelper : AnalyticsHelper {
    override fun logEvent(event: String, params: Bundle) {
        val keys = StringBuilder()
        val values = StringBuilder()
        params.keySet().forEach {
            keys.append("$it, ")
            values.append("${params.getString(it)}, ")
        }
        if (keys.length > 2) keys.delete(keys.length - 2, keys.length)
        if (values.length > 2) values.delete(values.length - 2, values.length)

        Timber.tag("AnalyticsHelper").d("Logging $event with $keys: $values")
    }

}