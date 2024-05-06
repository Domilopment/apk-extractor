package domilopment.apkextractor.domain.usecase.apkList

import android.content.Context
import domilopment.apkextractor.data.apkList.PackageArchiveModel
import domilopment.apkextractor.dependencyInjection.packageArchive.PackageArchiveRepository
import domilopment.apkextractor.dependencyInjection.preferenceDataStore.PreferenceRepository
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.settings.PackageArchiveUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn

interface GetApkListUseCase {
    operator fun invoke(searchQuery: Flow<String?>): Flow<List<PackageArchiveModel>>
}

class GetApkListUseCaseImpl(
    private val context: Context,
    private val apksRepository: PackageArchiveRepository,
    private val settings: PreferenceRepository
) : GetApkListUseCase {
    override fun invoke(searchQuery: Flow<String?>) =
        apksRepository.apks.combine(settings.apkSortOrder) { apkList, sortOrder ->
            PackageArchiveUtils.sortApkData(apkList, sortOrder)
                .filter { apk -> FileUtil.doesDocumentExist(context, apk.fileUri) }
        }.let {
            searchQuery.debounce(500).combine(it) { searchQuery, apkList ->
                val searchString = searchQuery?.trim()

                return@combine if (searchString.isNullOrBlank()) {
                    apkList
                } else {
                    apkList.filter {
                        it.fileName.contains(
                            searchString, ignoreCase = true
                        ) || it.appName?.contains(
                            searchString, ignoreCase = true
                        ) ?: false || it.appPackageName?.contains(
                            searchString, ignoreCase = true
                        ) ?: false || it.appVersionName?.contains(
                            searchString, ignoreCase = true
                        ) ?: false || it.appVersionCode?.toString()?.contains(
                            searchString, ignoreCase = true
                        ) ?: false
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
}