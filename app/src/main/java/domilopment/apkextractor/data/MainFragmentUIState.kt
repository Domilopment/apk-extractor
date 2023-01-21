package domilopment.apkextractor.data

data class MainFragmentUIState(
    var isRefreshing: Boolean = false,
    var appList: List<ApplicationModel> = listOf(),
)