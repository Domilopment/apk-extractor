package domilopment.apkextractor.data.model.apkList

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class ApkListScreenState (
    val isRefreshing: Boolean = true,
    val appList: PersistentList<ApkModel.ApkListModel> = persistentListOf(),
)