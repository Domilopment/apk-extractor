package domilopment.apkextractor.data.repository.logger

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.setCustomKeys
import timber.log.Timber

class CrashlyticsLogger : Timber.Tree() {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        when (priority) {
            Log.VERBOSE, Log.DEBUG, Log.INFO -> return
        }

        crashlytics.setCustomKeys {
            key(CRASHLYTICS_KEY_PRIORITY, priority)
            if (tag != null) key(CRASHLYTICS_KEY_TAG, tag)
            key(CRASHLYTICS_KEY_MESSAGE, message)
        }

        crashlytics.recordException(t ?: Exception(message))
    }

    companion object {
        private const val CRASHLYTICS_KEY_PRIORITY = "priority"
        private const val CRASHLYTICS_KEY_TAG = "tag"
        private const val CRASHLYTICS_KEY_MESSAGE = "message"
    }
}