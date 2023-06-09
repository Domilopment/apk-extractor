package domilopment.apkextractor.ui.viewModels

import android.app.Application
import android.net.Uri
import android.provider.DocumentsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.*
import domilopment.apkextractor.UpdateTrigger
import domilopment.apkextractor.data.ApkListFragmentUIState
import domilopment.apkextractor.data.ApkOptionsBottomSheetUIState
import domilopment.apkextractor.data.PackageArchiveModel
import domilopment.apkextractor.utils.eventHandler.Event
import domilopment.apkextractor.utils.eventHandler.EventDispatcher
import domilopment.apkextractor.utils.eventHandler.EventType
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.ListOfAPKs
import domilopment.apkextractor.utils.settings.ApkSortOptions
import domilopment.apkextractor.utils.settings.SettingsManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException
import domilopment.apkextractor.utils.eventHandler.Observer

class ApkListViewModel(application: Application) : AndroidViewModel(application), Observer {
    override val key: String = "ApkListViewModel"

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

    private var loadArchiveInfoJob: Deferred<Unit>? = null

    private val observer = Observer<List<PackageArchiveModel>> { apps ->
        loadArchiveInfoJob?.cancel(CancellationException("New Data arrived"))
        val sortedApps = SettingsManager(context).sortApkData(apps)
        _apkListFragmentState.update { state ->
            state.copy(
                appList = sortedApps, isRefreshing = false, updateTrigger = UpdateTrigger(true)
            )
        }
        loadArchiveInfoJob = viewModelScope.async(Dispatchers.IO) {
            sortedApps.forEach {
                it.loadPackageArchiveInfo(context)
            }
        }
    }

    init {
        // Set applications in view once they are loaded
        _packageArchives.observeForever(observer)
        EventDispatcher.registerObserver(this, EventType.SAVED, EventType.DELETED)
    }

    override fun onCleared() {
        EventDispatcher.unregisterObserver(this, EventType.ANY)
        _packageArchives.removeObserver(observer)
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
        if (app?.isPackageArchiveInfoLoaded == false) app.loadPackageArchiveInfo(context)
        _apkOptionsBottomSheetState.update { state ->
            state.copy(
                packageArchiveModel = app
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

    private fun addPackageArchiveModel(uri: Uri) {
        val packages = _packageArchives.value?.toMutableList() ?: return
        FileUtil(context).getDocumentInfo(
            uri,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_SIZE
        )?.let {
            PackageArchiveModel(it.uri, it.displayName!!, it.lastModified!!, it.size!!)
        }?.let { packages.add(it) }
        _packageArchives.value = packages
    }

    fun remove(apk: PackageArchiveModel) {
        _packageArchives.value = _packageArchives.value?.let { apps ->
            apps.toMutableList().apply {
                remove(apk)
            }
        }
    }

    fun forceRefresh(apk: PackageArchiveModel) {
        viewModelScope.async(Dispatchers.IO) { apk.forceRefresh(context) }
    }

    fun sort(sortPreferenceId: ApkSortOptions) {
        loadArchiveInfoJob?.cancel(CancellationException("New sort order"))

        _apkListFragmentState.update { state ->
            state.copy(isRefreshing = true)
        }

        var sortedApps: List<PackageArchiveModel>? = null

        _apkListFragmentState.update { state ->
            sortedApps = SettingsManager(context).sortApkData(state.appList, sortPreferenceId)
            state.copy(
                appList = sortedApps!!, isRefreshing = false
            )
        }
        loadArchiveInfoJob = viewModelScope.async(Dispatchers.IO) {
            sortedApps?.forEach {
                it.loadPackageArchiveInfo(context)
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
