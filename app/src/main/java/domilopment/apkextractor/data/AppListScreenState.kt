package domilopment.apkextractor.data

data class AppListScreenState(
    var isRefreshing: Boolean = true,
    var appList: List<ApplicationModel> = listOf(),
    var actionMode: Boolean = false,
    var selectedApp: ApplicationModel? = null
)