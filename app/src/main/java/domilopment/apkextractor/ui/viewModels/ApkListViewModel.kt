package domilopment.apkextractor.ui.viewModels

import android.app.Application
import android.net.Uri
import android.provider.DocumentsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.data.ApkListScreenState
import domilopment.apkextractor.data.PackageArchiveModel
import domilopment.apkextractor.dependencyInjection.preferenceDataStore.PreferenceRepository
import domilopment.apkextractor.utils.eventHandler.Event
import domilopment.apkextractor.utils.eventHandler.EventDispatcher
import domilopment.apkextractor.utils.eventHandler.EventType
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.dependencyInjection.packageArchive.PackageArchiveRepository
import domilopment.apkextractor.utils.settings.PackageArchiveUtils
import domilopment.apkextractor.utils.settings.ApkSortOptions
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import domilopment.apkextractor.utils.eventHandler.Observer
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class)
class ApkListViewModel @Inject constructor(
    application: Application,
    private val preferenceRepository: PreferenceRepository,
    private val apksRepository: PackageArchiveRepository
) : AndroidViewModel(application), Observer {
    override val key: String = "ApkListViewModel"

    private val _apkListFragmentState: MutableStateFlow<ApkListScreenState> =
        MutableStateFlow(ApkListScreenState())
    val apkListFragmentState: StateFlow<ApkListScreenState> = _apkListFragmentState.asStateFlow()

    private val _searchQuery: MutableStateFlow<String?> = MutableStateFlow(null)

    val saveDir = preferenceRepository.saveDir
    val sortOrder = preferenceRepository.apkSortOrder

    private val context get() = getApplication<Application>().applicationContext

    private var loadArchiveInfoJob: Deferred<Unit>? = null

    init {
        viewModelScope.launch {
            _searchQuery.debounce(500)
                .combine(apksRepository.apks.combine(sortOrder) { apkList, sortOrder ->
                    PackageArchiveUtils.sortApkData(apkList, sortOrder)
                }) { searchQuery, apkList ->
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
                }.collect { apks ->
                    loadArchiveInfoJob?.cancel(CancellationException("New Data arrived"))
                    _apkListFragmentState.update { state ->
                        state.copy(
                            appList = apks, isRefreshing = false
                        )
                    }
                    loadArchiveInfoJob = viewModelScope.async(Dispatchers.IO) {
                        apks.forEach { model ->
                            model.packageArchiveInfo(context)
                            _apkListFragmentState.update { state ->
                                state.copy(appList = state.appList.toMutableList()
                                    .map { if (it.fileUri == model.fileUri) model.copy() else it })
                            }
                        }
                    }
                }
        }
        // Set applications in view once they are loaded
        EventDispatcher.registerObserver(this, EventType.SAVED, EventType.DELETED)
    }

    override fun onCleared() {
        EventDispatcher.unregisterObserver(this, EventType.ANY)
        super.onCleared()
    }

    override fun onEventReceived(event: Event<*>) {
        when (event.eventType) {
            EventType.SAVED -> addPackageArchiveModel(event.data as Uri)
            EventType.DELETED -> remove(event.data as PackageArchiveModel)
            else -> return
        }
    }

    /**
     * Select a specific Application from list in view
     * and set it in BottomSheet state
     * @param app selected application
     */
    fun selectPackageArchive(app: PackageArchiveModel?) {
        if (app?.isPackageArchiveInfoLoaded == false) app.packageArchiveInfo(context)
        _apkListFragmentState.update { state ->
            state.copy(
                selectedPackageArchiveModel = app
            )
        }
    }

    /**
     * Set query string from search in App List, sets empty string if null
     * @param query last input Search String
     */
    fun searchQuery(query: String?) {
        _searchQuery.value = query
    }

    /**
     * Update App list
     */
    fun updatePackageArchives() {
        _apkListFragmentState.update {
            it.copy(isRefreshing = true)
        }
        viewModelScope.launch {
            async { apksRepository.updateApps() }
        }
    }

    private fun addPackageArchiveModel(uri: Uri) {
        viewModelScope.launch {
            FileUtil(context).getDocumentInfo(
                uri,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                DocumentsContract.Document.COLUMN_SIZE
            )?.let {
                PackageArchiveModel(it.uri, it.displayName!!, it.lastModified!!, it.size!!)
            }?.also {
                _apkListFragmentState.update { state ->
                    state.copy(appList = state.appList.toMutableList().apply { add(it) })
                }
                apksRepository.addApk(it)
            }
        }
    }

    fun remove(apk: PackageArchiveModel) {
        if (FileUtil(context).doesDocumentExist(apk.fileUri)) return

        _apkListFragmentState.update { state ->
            state.copy(
                appList = state.appList.filter { it.fileUri != apk.fileUri },
                selectedPackageArchiveModel = if (state.selectedPackageArchiveModel?.fileUri == apk.fileUri) null else state.selectedPackageArchiveModel
            )
        }
        viewModelScope.launch {
            apksRepository.removeApk(apk)
        }
    }

    fun forceRefresh(apk: PackageArchiveModel) {
        viewModelScope.async(Dispatchers.IO) {
            apk.forceRefresh(context)
            _apkListFragmentState.update { state ->
                state.copy(appList = state.appList.toMutableList()
                    .map { if (it.fileUri == apk.fileUri) apk.copy() else it })
            }
        }
    }

    fun sort(sortPreferenceId: ApkSortOptions) {
        loadArchiveInfoJob?.cancel(CancellationException("New sort order"))

        _apkListFragmentState.update { state ->
            state.copy(isRefreshing = true)
        }
        viewModelScope.launch {
            preferenceRepository.setApkSortOrder(sortPreferenceId.name)
        }
    }
}
