package domilopment.apkextractor.data.apkList

import domilopment.apkextractor.data.apkList.PackageArchiveModel

data class ApkListScreenState (
    var isRefreshing: Boolean = true,
    var appList: List<PackageArchiveModel> = listOf(),
    var selectedPackageArchiveModel: PackageArchiveModel? = null
)