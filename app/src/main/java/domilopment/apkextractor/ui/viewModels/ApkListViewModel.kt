package domilopment.apkextractor.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import domilopment.apkextractor.UpdateTrigger
import domilopment.apkextractor.data.ApkListFragmentUIState
import domilopment.apkextractor.data.ApkOptionsBottomSheetUIState
import domilopment.apkextractor.data.PackageArchiveModel
import domilopment.apkextractor.utils.ListOfAPKs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApkListViewModel(application: Application) : AndroidViewModel(application) {
    private val _packageArchives: MutableLiveData<List<PackageArchiveModel>> by lazy {
        MutableLiveData<List<PackageArchiveModel>>().also {
            viewModelScope.launch {
                it.value = loadApks()
            }
        }
    }
    val packageArchives: LiveData<List<PackageArchiveModel>> = _packageArchives

    private val _apkListFragmentState: MutableStateFlow<ApkListFragmentUIState> =
        MutableStateFlow(ApkListFragmentUIState())
    val apkListFragmentState: StateFlow<ApkListFragmentUIState> =
        _apkListFragmentState.asStateFlow()

    private val _apkOptionsBottomSheetState: MutableStateFlow<ApkOptionsBottomSheetUIState> =
        MutableStateFlow(ApkOptionsBottomSheetUIState())
    val akpOptionsBottomSheetUIState: StateFlow<ApkOptionsBottomSheetUIState> =
        _apkOptionsBottomSheetState.asStateFlow()

    private val _searchQuery: MutableLiveData<String?> = MutableLiveData(null)
    val searchQuery: LiveData<String?> = _searchQuery

    private val context get() = getApplication<Application>().applicationContext

    init {
        // Set applications in view once they are loaded
        _packageArchives.observeForever { apps ->
            _apkListFragmentState.update { state ->
                state.copy(
                    appList = apps, isRefreshing = false, updateTrigger = UpdateTrigger(true)
                )
            }
            viewModelScope.async(Dispatchers.IO) {
                apps.map {
                    it.loadPackageArchiveInfo()
                    _apkListFragmentState.update { state ->
                        state.copy(
                            updateTrigger = UpdateTrigger(true)
                        )
                    }
                    _apkOptionsBottomSheetState.update { state ->
                        state.copy(
                            updateTrigger = UpdateTrigger(true)
                        )
                    }
                }
            }
        }
    }

    /**
     * Select a specific Application from list in view
     * and set it in BottomSheet state
     * @param app selected application
     */
    fun selectPackageArchive(app: PackageArchiveModel?) {
        _apkOptionsBottomSheetState.update { state ->
            state.copy(
                selectedApplicationModel = app?.apply {
                    this.loadPackageArchiveInfo()
                }
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
            val load = async(Dispatchers.IO) {
                return@async loadApks()
            }
            val apps = load.await()
            _packageArchives.postValue(apps)
        }
    }

    fun remove(apk: PackageArchiveModel) {
        _packageArchives.value = _packageArchives.value?.let { apps ->
            apps.toMutableList().apply {
                remove(apk)
            }
        }
    }

    /**
     * Load apps from device
     */
    private suspend fun loadApks(): List<PackageArchiveModel> = withContext(Dispatchers.IO) {
        // Do an asynchronous operation to fetch users.
        return@withContext ListOfAPKs(context).apkFiles()
    }
}
