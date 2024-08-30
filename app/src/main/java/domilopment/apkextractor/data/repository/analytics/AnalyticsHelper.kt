package domilopment.apkextractor.data.repository.analytics

import android.os.Bundle

interface AnalyticsHelper {
    fun logEvent(event: String, params: Bundle)

    fun logEvent(event: String, params: Bundle.() -> Unit) {
        val bundle = Bundle()
        bundle.params()
        logEvent(event, bundle)
    }

    object Events {
        const val SCREEN_VIEW = "screen_view"
        const val SELECT_ITEM = "select_item"
        const val SELECT_CONTENT = "select_content"
        const val SET_DATA_COLLECTION = "data_collection"
        const val SAVE_DIR_DIALOG = "save_dir_dialog"
    }

    object Param {
        const val SCREEN_NAME = "screen_name"
        const val SCREEN_CLASS = "screen_class"
        const val ITEM_LIST_ID = "item_list_id"
        const val ITEM_LIST_NAME = "item_list_name"
        const val CONTENT_TYPE = "content_type"
        const val ITEM_ID = "item_id"
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
