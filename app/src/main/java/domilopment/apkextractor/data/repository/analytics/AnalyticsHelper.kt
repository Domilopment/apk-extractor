package domilopment.apkextractor.data.repository.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

interface AnalyticsHelper {
    fun logEvent(event: String, params: Bundle)

    object Events {
        const val SCREEN_VIEW = FirebaseAnalytics.Event.SCREEN_VIEW
        const val SELECT_ITEM = FirebaseAnalytics.Event.SELECT_ITEM
        const val SELECT_CONTENT = FirebaseAnalytics.Event.SELECT_CONTENT
        const val SET_DATA_COLLECTION = "data_collection"
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
    val bundle = Bundle().apply {
        putString(AnalyticsHelper.Param.SCREEN_NAME, screenName)
        putString(AnalyticsHelper.Param.SCREEN_CLASS, screenClass)
    }
    logEvent(AnalyticsHelper.Events.SCREEN_VIEW, bundle)
}

fun AnalyticsHelper.logListItem(itemListId: String?, itemListName: String?) {
    val bundle = Bundle().apply {
        putString(AnalyticsHelper.Param.ITEM_LIST_ID, itemListId)
        putString(AnalyticsHelper.Param.ITEM_LIST_NAME, itemListName)
    }
    logEvent(AnalyticsHelper.Events.SELECT_ITEM, bundle)
}

fun AnalyticsHelper.logItemClick(contentType: String?, itemId: String?) {
    val bundle = Bundle().apply {
        putString(AnalyticsHelper.Param.CONTENT_TYPE, contentType)
        putString(AnalyticsHelper.Param.ITEM_ID, itemId)
    }
    logEvent(AnalyticsHelper.Events.SELECT_CONTENT, bundle)
}
