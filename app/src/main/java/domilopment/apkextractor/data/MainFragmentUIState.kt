package domilopment.apkextractor.data

import domilopment.apkextractor.UpdateTrigger

data class MainFragmentUIState(
    var isRefreshing: Boolean = true,
    var appList: List<ApplicationModel> = listOf(),
    var actionMode: Boolean = false,
)