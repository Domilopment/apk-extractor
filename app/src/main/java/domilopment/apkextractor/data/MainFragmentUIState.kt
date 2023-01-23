package domilopment.apkextractor.data

data class MainFragmentUIState(
    var isRefreshing: Boolean = true,
    var appList: List<ApplicationModel> = listOf(),
    var actionMode: Boolean = false
)