package domilopment.apkextractor.data.apkList

import domilopment.apkextractor.data.room.entities.PackageArchiveEntity

data class ApkListScreenState (
    var isRefreshing: Boolean = true,
    var appList: List<PackageArchiveEntity> = listOf(),
    var selectedPackageArchiveModel: PackageArchiveEntity? = null,
    var errorMessage: String? = null,
)