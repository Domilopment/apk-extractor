package domilopment.apkextractor.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.data.model.apkList.ApkListScreenState
import domilopment.apkextractor.data.model.apkList.ApkModel
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.domain.usecase.apkList.DeleteApkUseCase
import domilopment.apkextractor.domain.usecase.apkList.GetApkListUseCase
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
        ApkSortOptions.SORT_BY_LAST_MODIFIED_DESC
    )

    init {
        viewModelScope.launch {
            apkList(_searchQuery).collect { apks ->
                _apkListFragmentState.update { state ->
                    state.copy(
                        appList = apks, isRefreshing = false
                    )
                }
            }
        }
        viewModelScope.launch {
            saveDir.collect {
                if (it != null) updatePackageArchives()
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
        updateJob?.cancel()
        _apkListFragmentState.update {
            it.copy(isRefreshing = true)
        }
        updateJob = viewModelScope.launch(Dispatchers.IO) {
            updateApks()
        }
    }

    fun remove(apk: ApkModel.ApkListModel) {
        viewModelScope.launch {
            deleteApk(apk)
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
