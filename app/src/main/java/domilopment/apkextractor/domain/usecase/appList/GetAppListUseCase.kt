package domilopment.apkextractor.domain.usecase.appList

import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.data.repository.applications.ApplicationRepository
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.utils.settings.ApplicationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

interface GetAppListUseCase {
    operator fun invoke(searchQuery: Flow<String?>): Flow<List<ApplicationModel>>
}

class GetAppListUseCaseImpl @Inject constructor(
    private val isAppInstalled: IsAppInstalledUseCase,
    private val appsRepository: ApplicationRepository,
    private val settings: PreferenceRepository
): GetAppListUseCase {
    @OptIn(FlowPreview::class)
    override operator fun invoke(searchQuery: Flow<String?>): Flow<List<ApplicationModel>> = combine(
        appsRepository.apps,
        settings.updatedSysApps,
        settings.sysApps,
        settings.userApps,
        settings.appListFavorites
    ) { appList, updatedSysApps, sysApps, userApps, favorites ->
        ApplicationUtil.selectedAppTypes(
            appList, updatedSysApps, sysApps, userApps, favorites
        ).filter { app ->
            isAppInstalled(app.appPackageName)
        }
    }.let {
        combine(
            it, settings.appFilterInstaller, settings.appFilterCategory, settings.appFilterOthers
        ) { appList, installer, category, others ->
            ApplicationUtil.filterApps(appList, installer, category, others)
        }
    }.let {
        combine(
            it, settings.appSortOrder, settings.appSortFavorites, settings.appSortAsc
        ) { appList, sortMode, sortFavorites, sortAsc ->
            ApplicationUtil.sortAppData(appList, sortMode.ordinal, sortFavorites, sortAsc)
        }
    }.let {
        searchQuery.debounce(500L).combine(it) { searchQuery, appList ->
            val searchString = searchQuery?.trim()

            return@combine if (searchString.isNullOrBlank()) {
                appList
            } else {
                appList.filter {
                    it.appName.contains(
                        searchString, ignoreCase = true
                    ) || it.appPackageName.contains(
                        searchString, ignoreCase = true
                    )
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}