package domilopment.apkextractor.data.model.apkList

import domilopment.apkextractor.data.room.entities.PackageArchiveEntity

data class ApkDetailScreenState(
    val app: PackageArchiveEntity? = null,
    val isLoading: Boolean = true
)
