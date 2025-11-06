package domilopment.apkextractor.domain.usecase.appList

import android.content.pm.PackageManager
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.data.repository.applications.ApplicationRepository
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.domain.mapper.AppModelToApplicationListModelMapper
import domilopment.apkextractor.domain.mapper.mapAll
import domilopment.apkextractor.utils.PackageName
import domilopment.apkextractor.utils.settings.ApplicationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

interface GetAppListUseCase {
    operator fun invoke(searchQuery: Flow<PackageName?>): Flow<List<ApplicationModel.ApplicationListModel>>
}

class GetAppListUseCaseImpl @Inject constructor(
    private val packageManager: PackageManager,
    private val isAppInstalled: IsAppInstalledUseCase,
    private val appsRepository: ApplicationRepository,
    private val settings: PreferenceRepository
) : GetAppListUseCase {
    @OptIn(FlowPreview::class)
    override operator fun invoke(searchQuery: Flow<PackageName?>): Flow<List<ApplicationModel.ApplicationListModel>> =
        combine(
            appsRepository.apps,
            settings.updatedSysApps,
            settings.sysApps,
            settings.userApps,
        ) { apps, updatedSysApps, sysApps, userApps ->
            // Filter selected app types
            ApplicationUtil.selectedAppTypes(
                apps, updatedSysApps, sysApps, userApps
            )
                // Make sure apps are still installed
                .filter { app -> isAppInstalled(app.packageName) }
                // Map ApplicationModel to ApplicationModel
                .let { AppModelToApplicationListModelMapper(packageManager).mapAll(it) }
                // Remove Null mapping even there shouldn't be any
                .filterNotNull()
        }.let { appList ->
            // Filter user preferences applied on app list
            combine(
                appList,
                settings.appFilterInstaller,
                settings.appFilterCategory,
                settings.appFilterOthers,
                settings.appListFavorites
            ) { appList, installer, category, others, favorites ->
                ApplicationUtil.getFavorites(appList, favorites).let {
                    ApplicationUtil.filterApps(it, installer, category, others)
                }
            }
        }.let {
            combine(
                it, settings.appSortOrder, settings.appSortFavorites, settings.appSortAsc
            ) { appList, sortMode, sortFavorites, sortAsc ->
                ApplicationUtil.sortAppData(appList, sortMode.ordinal, sortFavorites, sortAsc)
            }
        }.let { appList ->
            searchQuery.debounce(500L).combine(appList) { searchQuery, appList ->
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
        }.flowOn(Dispatchers.Default)
}