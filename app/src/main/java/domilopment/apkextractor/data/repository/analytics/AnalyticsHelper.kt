package domilopment.apkextractor.data.repository.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

interface AnalyticsHelper {
    fun logEvent(event: String, params: Bundle)

    fun logEvent(event: String, params: Bundle.() -> Unit) {
        val bundle = Bundle()
        bundle.params()
        logEvent(event, bundle)
    }

    object Events {
        const val SCREEN_VIEW = FirebaseAnalytics.Event.SCREEN_VIEW
        const val SELECT_ITEM = FirebaseAnalytics.Event.SELECT_ITEM
        const val SELECT_CONTENT = FirebaseAnalytics.Event.SELECT_CONTENT
        const val SET_DATA_COLLECTION = "data_collection"
        const val SAVE_DIR_DIALOG = "save_dir_dialog"
    }

    object Param {
        const val SCREEN_NAME = FirebaseAnalytics.Param.SCREEN_NAME
        const val SCREEN_CLASS = FirebaseAnalytics.Param.SCREEN_CLASS
        const val ITEM_LIST_ID = FirebaseAnalytics.Param.ITEM_LIST_ID
        const val ITEM_LIST_NAME = FirebaseAnalytics.Param.ITEM_LIST_NAME
        const val CONTENT_TYPE = FirebaseAnalytics.Param.CONTENT_TYPE
        const val ITEM_ID = FirebaseAnalytics.Param.ITEM_ID
        const val COLLECT_ANALYTICS = "collect_analytics"
        const val COLLECT_CRASHLYTICS = "collect_crashlytics"
        const val COLLECT_PERFORMANCE = "collect_performance"
    }
}

fun AnalyticsHelper.logScreenView(screenName: String?, screenClass: String?) {
    logEvent(AnalyticsHelper.Events.SCREEN_VIEW) {
        putString(AnalyticsHelper.Param.SCREEN_NAME, screenName)
        putString(AnalyticsHelper.Param.SCREEN_CLASS, screenClass)
    }
}

fun AnalyticsHelper.logListItem(itemListId: String?, itemListName: String?) {
    logEvent(AnalyticsHelper.Events.SELECT_ITEM) {
        putString(AnalyticsHelper.Param.ITEM_LIST_ID, itemListId)
        putString(AnalyticsHelper.Param.ITEM_LIST_NAME, itemListName)
    }
}

fun AnalyticsHelper.logItemClick(contentType: String?, itemId: String?) {
    logEvent(AnalyticsHelper.Events.SELECT_CONTENT) {
        putString(AnalyticsHelper.Param.CONTENT_TYPE, contentType)
        putString(AnalyticsHelper.Param.ITEM_ID, itemId)
    }
}
