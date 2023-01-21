package domilopment.apkextractor.fragments

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import domilopment.apkextractor.Event
import domilopment.apkextractor.R
import domilopment.apkextractor.data.*
import domilopment.apkextractor.utils.FileHelper
import domilopment.apkextractor.utils.SettingsManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel(
    application: Application
) : AndroidViewModel(application), HasDefaultViewModelProviderFactory {
    private val _applications: MutableLiveData<Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>> by lazy {
        MutableLiveData<Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>>().also {
            viewModelScope.launch {
                it.value = loadApps()
            }
        }
    }
    val applications: LiveData<Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>>> =
        _applications

    private val _mainFragmantState: MutableStateFlow<MainFragmentUIState> =
        MutableStateFlow(MainFragmentUIState())
    val mainFragmantState: StateFlow<MainFragmentUIState> = _mainFragmantState.asStateFlow()

    private val _progressDialogState: MutableStateFlow<ProgressDialogUiState> =
        MutableStateFlow(ProgressDialogUiState())
    val progressDialogState: StateFlow<ProgressDialogUiState> = _progressDialogState.asStateFlow()

    private val _appOptionsBottomSheetState: MutableStateFlow<AppOptionsBottomSheetUIState> =
        MutableStateFlow(AppOptionsBottomSheetUIState())
    val appOptionsBottomSheetUIState: StateFlow<AppOptionsBottomSheetUIState> =
        _appOptionsBottomSheetState.asStateFlow()

    private val _searchQuery: MutableLiveData<String> = MutableLiveData(String())
    val searchQuery: LiveData<String> = _searchQuery

    private val extractionResult: MutableLiveData<Event<Triple<Boolean?, ApplicationModel?, Int>>> =
        MutableLiveData(null)
    private val shareResult: MutableLiveData<Event<ArrayList<Uri>?>> = MutableLiveData(null)

    private val context get() = getApplication<Application>().applicationContext

    private val ioDispatcher get() = Dispatchers.IO

    init {
        _applications.observeForever { apps ->
            _mainFragmantState.update { state ->
                state.copy(
                    appList = SettingsManager(context).let {
                        it.sortData(it.selectedAppTypes(apps))
                    },
                    isRefreshing = false
                )
            }
        }
    }

    fun selectApplication(packageName: String) {
        val app = _mainFragmantState.value.appList.find { it.appPackageName == packageName }
        _appOptionsBottomSheetState.update { state ->
            state.copy(
                selectedApplicationModel = app
            )
        }
    }

    /**
     * Set query string from search in App List, sets empty string if null
     * @param query last input Search String
     */
    fun searchQuery(query: String?) {
        _searchQuery.value = query ?: ""
    }

    fun getExtractionResult(): LiveData<Event<Triple<Boolean?, ApplicationModel?, Int>>> {
        return extractionResult
    }

    fun getShareResult(): LiveData<Event<ArrayList<Uri>?>> {
        return shareResult
    }

    /**
     * Update App list
     */
    fun updateApps() {
        _mainFragmantState.update {
            it.copy(isRefreshing = true)
        }
        viewModelScope.launch {
            val load = async(ioDispatcher) {
                ListOfAPKs(context.packageManager).updateData()
                return@async loadApps()
            }
            val apps = load.await()
            _applications.postValue(apps)
        }
    }

    /**
     * Remove app from app list, for example on uninstall
     * @param app
     * uninstalled app
     */
    fun removeApp(app: ApplicationModel) {
        _applications.value = _applications.value?.also {
            it.third.toMutableList().apply {
                removeIf { userApp -> userApp.appPackageName == app.appPackageName }
            }.let { newUserApps ->
                Triple(it.first, it.second, newUserApps)
            }
        }
    }

    /**
     * Sorts data on Call after Selected Sort type
     */
    fun sortApps() {
        _mainFragmantState.update {
            it.copy(isRefreshing = true)
        }
        viewModelScope.launch {
            _mainFragmantState.update { state ->
                state.copy(
                    appList = withContext(Dispatchers.IO) {
                        SettingsManager(
                            context
                        ).sortData(state.appList)
                    },
                    isRefreshing = false
                )
            }
        }
    }

    fun changeSelection(key: String, b: Boolean) {
        val settingsManager = SettingsManager(context)
        _applications.value?.let {
            when (key) {
                "updated_system_apps" -> settingsManager.selectedAppTypes(
                    it,
                    selectUpdatedSystemApps = b
                )
                "system_apps" -> settingsManager.selectedAppTypes(it, selectSystemApps = b)
                "user_apps" -> settingsManager.selectedAppTypes(it, selectUserApps = b)
                else -> null
            }?.let { selectesAppTypes ->
                viewModelScope.launch {
                    _mainFragmantState.update { state ->
                        state.copy(
                            appList = withContext(Dispatchers.IO) {
                                settingsManager.sortData(selectesAppTypes)
                            }
                        )
                    }
                }
            }
        }
    }

    fun saveApps(list: List<ApplicationModel>) {
        viewModelScope.launch {
            val settingsManager = SettingsManager(context)
            val fileHelper = FileHelper(context)
            var application: ApplicationModel? = null
            var failure = false

            _progressDialogState.update {
                it.copy(
                    title = context.getString(R.string.progress_dialog_title_save),
                    tasks = list.size,
                    shouldBeShown = true
                )
            }

            val job = launch extract@{
                list.forEach { app ->
                    application = app
                    withContext(Dispatchers.Main) {
                        _progressDialogState.update { state ->
                            state.copy(
                                process = app.appPackageName,
                                progress = state.progress
                            )
                        }
                    }
                    withContext(Dispatchers.IO) {
                        failure = fileHelper.copy(
                            app.appSourceDirectory,
                            settingsManager.saveDir()!!,
                            settingsManager.appName(app)
                        ) == null
                    }
                    withContext(Dispatchers.Main) {
                        _progressDialogState.update { state ->
                            state.copy(
                                process = app.appPackageName,
                                progress = state.progress + 1
                            )
                        }
                    }
                    if (failure) {
                        this@extract.cancel()
                    }
                }
            }
            job.join()
            extractionResult.value = Event(Triple(failure, application, list.size))
        }
    }

    fun createShareUrisForApps(list: List<ApplicationModel>) {
        viewModelScope.launch {
            val files = ArrayList<Uri>()
            val fileHelper = FileHelper(context)
            val jobList = ArrayList<Deferred<Any?>>()

            _progressDialogState.update {
                it.copy(
                    title = context.getString(R.string.progress_dialog_title_share),
                    tasks = list.size,
                    shouldBeShown = true
                )
            }

            list.filter {
                it.isChecked
            }.forEach { app ->
                jobList.add(async {
                    withContext(Dispatchers.IO) {
                        fileHelper.shareURI(app).also {
                            files.add(it)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        _progressDialogState.update { state ->
                            state.copy(
                                progress = state.progress + 1
                            )
                        }
                    }
                })
            }
            jobList.awaitAll()
            shareResult.value = Event(files)
        }
    }

    fun resetProgress() {
        _progressDialogState.value = ProgressDialogUiState()
    }

    /**
     * Load apps from device
     */
    private suspend fun loadApps(): Triple<List<ApplicationModel>, List<ApplicationModel>, List<ApplicationModel>> =
        withContext(Dispatchers.IO) {
            // Do an asynchronous operation to fetch users.
            return@withContext SettingsManager(context).getApps()
        }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return modelClass.getConstructor(Application::class.java)
                    .newInstance(getApplication())
            }
        }
    }
}