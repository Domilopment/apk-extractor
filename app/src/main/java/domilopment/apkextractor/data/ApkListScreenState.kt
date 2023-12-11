package domilopment.apkextractor.data

data class ApkListScreenState (
    var isRefreshing: Boolean = true,
    var appList: List<PackageArchiveModel> = listOf(),
    var selectedPackageArchiveModel: PackageArchiveModel? = null
)