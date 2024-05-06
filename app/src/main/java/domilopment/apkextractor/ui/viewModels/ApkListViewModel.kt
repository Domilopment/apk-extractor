package domilopment.apkextractor.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.data.apkList.ApkListScreenState
import domilopment.apkextractor.data.apkList.PackageArchiveModel
import domilopment.apkextractor.dependencyInjection.preferenceDataStore.PreferenceRepository
import domilopment.apkextractor.domain.usecase.apkList.DeleteApkUseCase
import domilopment.apkextractor.utils.eventHandler.Event
import domilopment.apkextractor.utils.eventHandler.EventDispatcher
import domilopment.apkextractor.utils.eventHandler.EventType
import domilopment.apkextractor.domain.usecase.apkList.GetApkListUseCase
import domilopment.apkextractor.domain.usecase.apkList.LoadApkInfoUseCase
import domilopment.apkextractor.domain.usecase.apkList.UpdateApksUseCase
import domilopment.apkextractor.utils.settings.ApkSortOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import domilopment.apkextractor.utils.eventHandler.Observer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ApkListViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val deleteApk: DeleteApkUseCase,
    private val apkList: GetApkListUseCase,
    private val loadApkInfo: LoadApkInfoUseCase,
    private val updateApks: UpdateApksUseCase,
) : ViewModel(), Observer {
    override val key: String = "ApkListViewModel"

    private val _apkListFragmentState: MutableStateFlow<ApkListScreenState> =
        MutableStateFlow(ApkListScreenState())
    val apkListFragmentState: StateFlow<ApkListScreenState> = _apkListFragmentState.asStateFlow()

    private val _searchQuery: MutableStateFlow<String?> = MutableStateFlow(null)

    val saveDir = preferenceRepository.saveDir.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), null
    )
    val sortOrder = preferenceRepository.apkSortOrder.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        ApkSortOptions.SORT_BY_FILE_SIZE_DESC
    )

    init {
        viewModelScope.launch {
            apkList(_searchQuery).collect { apks ->
                _apkListFragmentState.update { state ->
                    state.copy(
                        appList = apks, isRefreshing = false, selectedPackageArchiveModel = null
                    )
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
        _apkListFragmentState.update { state ->
            state.copy(
                selectedPackageArchiveModel = app
            )
        }
        if (app?.isPackageArchiveInfoLoaded == false) viewModelScope.launch {
            val update = async(Dispatchers.IO) {
                loadApkInfo(app)
            }
            _apkListFragmentState.update { state ->
                state.copy(selectedPackageArchiveModel = update.await()
                    .let { newInfo -> state.selectedPackageArchiveModel.let { if (it?.fileUri == newInfo.fileUri) newInfo else it } })
            }
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
        viewModelScope.launch(Dispatchers.IO) {
            updateApks()
        }
    }

    fun remove(apk: PackageArchiveModel) {
        viewModelScope.launch {
            deleteApk(apk)
        }
    }

    fun loadPackageArchiveInfo(apk: PackageArchiveModel) {
        viewModelScope.launch {
            val newApk = loadApkInfo(apk, forceRefresh = true)
            _apkListFragmentState.update { state ->
                state.copy(appList = state.appList.toMutableList()
                    .map { if (it.fileUri == apk.fileUri) newApk else it },
                    selectedPackageArchiveModel = state.selectedPackageArchiveModel.let { if (it?.fileUri == apk.fileUri) newApk else it })
            }
        }
    }

    fun sort(sortPreferenceId: ApkSortOptions) {
        _apkListFragmentState.update { state ->
            state.copy(isRefreshing = true)
        }
        viewModelScope.launch {
            preferenceRepository.setApkSortOrder(sortPreferenceId.name)
        }
    }
}
