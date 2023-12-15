package domilopment.apkextractor.data

data class AppListScreenState(
    var isRefreshing: Boolean = true,
    var appList: List<ApplicationModel> = listOf(),
    var selectedApp: ApplicationModel? = null
)