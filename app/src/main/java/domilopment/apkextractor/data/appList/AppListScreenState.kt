package domilopment.apkextractor.data.appList

import domilopment.apkextractor.data.appList.ApplicationModel

data class AppListScreenState(
    var isRefreshing: Boolean = true,
    var appList: List<ApplicationModel> = listOf(),
    var selectedApp: ApplicationModel? = null
)