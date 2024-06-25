package domilopment.apkextractor.data.model.appList

data class AppListScreenState(
    var isRefreshing: Boolean = true,
    var appList: List<ApplicationModel> = listOf(),
    var selectedApp: ApplicationModel? = null
)