package domilopment.apkextractor.domain.usecase.appList

import android.content.pm.PackageManager
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.data.repository.applications.ApplicationRepository
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.domain.mapper.AppModelToApplicationModelMapper
import domilopment.apkextractor.domain.mapper.mapAll
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
    private val packageManager: PackageManager,
    private val isAppInstalled: IsAppInstalledUseCase,
    private val appsRepository: ApplicationRepository,
    private val settings: PreferenceRepository
) : GetAppListUseCase {
    @OptIn(FlowPreview::class)
    override operator fun invoke(searchQuery: Flow<String?>): Flow<List<ApplicationModel>> =
        combine(
            appsRepository.apps,
            settings.updatedSysApps,
            settings.sysApps,
            settings.userApps,
            settings.appListFavorites
        ) { apps, updatedSysApps, sysApps, userApps, favorites ->
            // Filter selected app types
            ApplicationUtil.selectedAppTypes(
                apps, updatedSysApps, sysApps, userApps
            )
                // Make sure apps are still installed
                .filter { app -> isAppInstalled(app.applicationInfo.packageName) }
                // Map ApplicationModel to ApplicationModel
                .let { AppModelToApplicationModelMapper(packageManager).mapAll(it) }
        }.let {
            // Filter user preferences applied on app list
            combine(
                it,
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
        }.let {
            searchQuery.debounce(500L).combine(it) { searchQuery, appList ->
                val searchString = searchQuery?.trim()

                return@combine if (searchString.isNullOrBlank()) {
                    appList
                } else {
                    appList.filter {
                        it.appName.contains(
                            searchString, ignoreCase = true
                        ) == true || it.appPackageName.contains(
                            searchString, ignoreCase = true
                        )
                    }
                }
            }
        }.flowOn(Dispatchers.Default)
}