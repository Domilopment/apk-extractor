package domilopment.apkextractor.data.model.appList

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class AppListScreenState(
    val isRefreshing: Boolean = true,
    val appList: PersistentList<ApplicationModel.ApplicationListModel> = persistentListOf(),
    val selectedApp: ApplicationModel.ApplicationDetailModel? = null
)