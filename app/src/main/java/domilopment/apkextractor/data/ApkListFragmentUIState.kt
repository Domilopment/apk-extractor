package domilopment.apkextractor.data

import domilopment.apkextractor.UpdateTrigger

data class ApkListFragmentUIState (
    var isRefreshing: Boolean = true,
    var appList: List<PackageArchiveModel> = listOf(),
    var updateTrigger: UpdateTrigger = UpdateTrigger(false)
)