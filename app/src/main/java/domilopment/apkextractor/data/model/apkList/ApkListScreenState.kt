package domilopment.apkextractor.data.model.apkList

import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class ApkListScreenState (
    val isRefreshing: Boolean = true,
    val appList: PersistentList<PackageArchiveEntity> = persistentListOf(),
)