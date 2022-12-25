package domilopment.apkextractor.fragments

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import domilopment.apkextractor.Event
import domilopment.apkextractor.R
import domilopment.apkextractor.utils.SettingsManager
import domilopment.apkextractor.data.ApplicationModel
import domilopment.apkextractor.data.ListOfAPKs
import domilopment.apkextractor.data.ProgressDialogUiState
import domilopment.apkextractor.utils.FileHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel(
    application: Application
) : AndroidViewModel(application), HasDefaultViewModelProviderFactory {
    private val applications: MutableLiveData<List<ApplicationModel>> by lazy {
        MutableLiveData<List<ApplicationModel>>().also {
            it.value = loadApps()
        }
    }
    private val isRefreshing: MutableLiveData<Boolean> = MutableLiveData(false)
    private val _progressDialogState: MutableStateFlow<ProgressDialogUiState> =
        MutableStateFlow(ProgressDialogUiState())
    val progressDialogState: StateFlow<ProgressDialogUiState> = _progressDialogState.asStateFlow()
    private val extractionResult: MutableLiveData<Event<Triple<Boolean?, ApplicationModel?, Int>>> =
        MutableLiveData(null)
    private val shareResult: MutableLiveData<Event<ArrayList<Uri>?>> = MutableLiveData(null)

    private val context get() = getApplication<Application>().applicationContext

    private val ioDispatcher get() = Dispatchers.IO

    /**
     * Get app list from ViewModel
     * @return
     * List of APKs
     */
    fun getApps(): LiveData<List<ApplicationModel>> {
        return applications
    }

    /**
     * Get if app list dataset is updated or sorted
     * @return
     * Refresh state
     */
    fun getIsRefreshing(): LiveData<Boolean> {
        return isRefreshing
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
        isRefreshing.value = true
        viewModelScope.launch {
            val load = async(ioDispatcher) {
                ListOfAPKs(context.packageManager).updateData()
                return@async loadApps()
            }
            applications.postValue(load.await())
            isRefreshing.value = false
        }
    }

    /**
     * Remove app from app list, for example on uninstall
     * @param app
     * uninstalled app
     */
    fun removeApp(app: ApplicationModel) {
        applications.value?.toMutableList()
            ?.apply {
                remove(app)
            }?.also {
                applications.postValue(it)
            }
    }

    /**
     * Sorts data on Call after Selected Sort type
     */
    fun sortApps() {
        isRefreshing.value = true
        viewModelScope.launch {
            val load = async(ioDispatcher) {
                applications.value?.let {
                    return@async SettingsManager(context).sortData(it)
                }
            }
            applications.postValue(load.await())
            isRefreshing.postValue(false)
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
    private fun loadApps(): List<ApplicationModel> {
        // Do an asynchronous operation to fetch users.
        return SettingsManager(context).selectedAppTypes()
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