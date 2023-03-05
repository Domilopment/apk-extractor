package domilopment.apkextractor.data

import domilopment.apkextractor.UpdateTrigger

data class MainFragmentUIState(
    var isRefreshing: Boolean = true,
    var appList: List<ApplicationModel> = listOf(),
    var actionMode: Boolean = false,
    var updateTrigger: UpdateTrigger = UpdateTrigger(false) // State Flow does not recognize if property of element in AppList changes, so we trigger an update manually
)