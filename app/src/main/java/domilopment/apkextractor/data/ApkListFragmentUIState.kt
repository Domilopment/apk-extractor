package domilopment.apkextractor.data

data class ApkListFragmentUIState (
    var isRefreshing: Boolean = true,
    var appList: List<PackageArchiveModel> = listOf(),
)