package domilopment.apkextractor.domain.usecase.apkList

import android.content.Context
import domilopment.apkextractor.data.model.apkList.ApkModel
import domilopment.apkextractor.data.repository.packageArchive.PackageArchiveRepository
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.domain.mapper.PackageArchiveEntityMapperToApkListModelMapper
import domilopment.apkextractor.domain.mapper.mapAll
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.settings.PackageArchiveUtils
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

interface GetApkListUseCase {
    operator fun invoke(searchQuery: Flow<String?>): Flow<PersistentList<ApkModel.ApkListModel>>
}

class GetApkListUseCaseImpl(
    private val context: Context,
    private val apksRepository: PackageArchiveRepository,
    private val settings: PreferenceRepository
) : GetApkListUseCase {
    @OptIn(FlowPreview::class)
    override fun invoke(searchQuery: Flow<String?>) =
        apksRepository.apks.combine(settings.apkSortOrder) { apkList, sortOrder ->
            PackageArchiveUtils.sortApkData(apkList, sortOrder)
                .filter { apk -> FileUtil.doesDocumentExist(context, apk.fileUri) }
        }.let { apkList ->
            searchQuery.debounce(500).combine(apkList) { searchQuery, apkList ->
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
        }.map { list ->
            PackageArchiveEntityMapperToApkListModelMapper.mapAll(list).toPersistentList()
        }.flowOn(Dispatchers.Default)
}