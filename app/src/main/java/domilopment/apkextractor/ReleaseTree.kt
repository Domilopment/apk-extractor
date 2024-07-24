package domilopment.apkextractor

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.setCustomKeys
import timber.log.Timber

class ReleaseTree : Timber.Tree() {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }

        crashlytics.setCustomKeys {
            key(CRASHLYTICS_KEY_PRIORITY, priority)
            if (tag != null) key(CRASHLYTICS_KEY_TAG, tag)
            key(CRASHLYTICS_KEY_MESSAGE, message)
        }

        if (t == null) {
            crashlytics.recordException(Exception(message))
        } else {
            crashlytics.recordException(t)
        }
    }

    companion object {
        private const val CRASHLYTICS_KEY_PRIORITY = "priority"
        private const val CRASHLYTICS_KEY_TAG = "tag"
        private const val CRASHLYTICS_KEY_MESSAGE = "message"
    }
}