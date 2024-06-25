package domilopment.apkextractor.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.data.apkList.ApkListScreenState
import domilopment.apkextractor.data.room.entities.PackageArchiveEntity
import domilopment.apkextractor.dependencyInjection.preferenceDataStore.PreferenceRepository
import domilopment.apkextractor.domain.usecase.apkList.DeleteApkUseCase
import domilopment.apkextractor.domain.usecase.apkList.GetApkListUseCase
import domilopment.apkextractor.domain.usecase.apkList.LoadApkInfoUseCase
import domilopment.apkextractor.domain.usecase.apkList.UpdateApksUseCase
import domilopment.apkextractor.utils.settings.ApkSortOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
) : ViewModel() {
    private val _apkListFragmentState: MutableStateFlow<ApkListScreenState> =
        MutableStateFlow(ApkListScreenState())
    val apkListFragmentState: StateFlow<ApkListScreenState> = _apkListFragmentState.asStateFlow()

    private val _searchQuery: MutableStateFlow<String?> = MutableStateFlow(null)

    private var updateJob: Job? = null

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
                    state.copy(appList = apks,
                        isRefreshing = false,
                        selectedPackageArchiveModel = state.selectedPackageArchiveModel?.let { selected -> apks.find { it.fileUri == selected.fileUri } })
                }
            }
        }
    }

    /**
     * Select a specific Application from list in view
     * and set it in BottomSheet state
     * @param app selected application
     */
    fun selectPackageArchive(app: PackageArchiveEntity?) {
        _apkListFragmentState.update { state ->
            state.copy(
                selectedPackageArchiveModel = app
            )
        }
        if (app?.loaded == false) viewModelScope.launch(Dispatchers.IO) {
            loadApkInfo(app)
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
        updateJob?.cancel()
        _apkListFragmentState.update {
            it.copy(isRefreshing = true)
        }
        updateJob = viewModelScope.launch(Dispatchers.IO) {
            updateApks()
        }
    }

    fun remove(apk: PackageArchiveEntity) {
        viewModelScope.launch {
            deleteApk(apk)
        }
    }

    fun loadPackageArchiveInfo(apk: PackageArchiveEntity) {
        viewModelScope.launch {
            loadApkInfo(apk)
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

    fun dismissError() {
        _apkListFragmentState.update { state ->
            state.copy(errorMessage = null)
        }
    }
}
