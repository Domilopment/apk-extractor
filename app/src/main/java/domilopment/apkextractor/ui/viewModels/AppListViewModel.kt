package domilopment.apkextractor.ui.viewModels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.R
import domilopment.apkextractor.data.*
import domilopment.apkextractor.data.appList.AppListScreenState
import domilopment.apkextractor.data.appList.ApplicationModel
import domilopment.apkextractor.data.appList.ExtractionResult
import domilopment.apkextractor.data.appList.ShareResult
import domilopment.apkextractor.dependencyInjection.preferenceDataStore.PreferenceRepository
import domilopment.apkextractor.dependencyInjection.applications.ApplicationRepository
import domilopment.apkextractor.utils.SaveApkResult
import domilopment.apkextractor.utils.settings.ApplicationUtil
import domilopment.apkextractor.utils.eventHandler.Event
import domilopment.apkextractor.utils.eventHandler.EventDispatcher
import domilopment.apkextractor.utils.eventHandler.EventType
import domilopment.apkextractor.utils.eventHandler.Observer
import domilopment.apkextractor.utils.Utils
import domilopment.apkextractor.utils.apkActions.ApkActionsOptions
import domilopment.apkextractor.utils.settings.AppSortOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class)
class AppListViewModel @Inject constructor(
    application: Application,
    private val preferenceRepository: PreferenceRepository,
    private val appsRepository: ApplicationRepository
) : AndroidViewModel(application), Observer, ProgressDialogViewModel {
    override val key: String = "AppListViewModel"

    private val _mainFragmentState: MutableStateFlow<AppListScreenState> =
        MutableStateFlow(AppListScreenState())
    val mainFragmentState: StateFlow<AppListScreenState> = _mainFragmentState.asStateFlow()

    private val _searchQuery: MutableStateFlow<String?> = MutableStateFlow(null)

    private val _progressDialogState: MutableStateFlow<ProgressDialogUiState> =
        MutableStateFlow(ProgressDialogUiState())
    override val progressDialogState: StateFlow<ProgressDialogUiState> =
        _progressDialogState.asStateFlow()

    private val _extractionResult: MutableSharedFlow<ExtractionResult> = MutableSharedFlow()
    val extractionResult: SharedFlow<ExtractionResult> = _extractionResult.asSharedFlow()

    private val _shareResult: MutableSharedFlow<ShareResult> = MutableSharedFlow()
    val shareResult: SharedFlow<ShareResult> = _shareResult.asSharedFlow()

    private val appListFavorites = preferenceRepository.appListFavorites
    private val saveDir = preferenceRepository.saveDir
    private val backupXapk = preferenceRepository.backupModeXapk
    private val appName = preferenceRepository.appSaveName
    val appSortOrder = preferenceRepository.appSortOrder.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        AppSortOptions.SORT_BY_NAME
    )
    val appSortFavorites = preferenceRepository.appSortFavorites.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), true
    )
    val appSortAsc = preferenceRepository.appSortAsc.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), true
    )
    val updatedSystemApps = preferenceRepository.updatedSysApps.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), false
    )
    val systemApps = preferenceRepository.sysApps.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), false
    )
    val userApps = preferenceRepository.userApps.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), true
    )
    val filterInstaller = preferenceRepository.appFilterInstaller.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), null
    )
    val filterCategory = preferenceRepository.appFilterCategory.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), null
    )
    val filterOthers = preferenceRepository.appFilterOthers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), emptySet()
    )
    val rightSwipeAction = preferenceRepository.appRightSwipeAction.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        ApkActionsOptions.SAVE
    )
    val leftSwipeAction = preferenceRepository.appLeftSwipeAction.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        ApkActionsOptions.SHARE
    )
    val swipeActionCustomThreshold = preferenceRepository.appSwipeActionCustomThreshold.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), false
    )
    val swipeActionThresholdMod =
        preferenceRepository.appSwipeActionThresholdMod.map { it / 100 }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), 0.32f
        )

    private val context get() = getApplication<Application>().applicationContext

    // Task for ProgressDialog to Cancel
    private var runningTask: Job? = null

    init {
        // Set applications in view once they are loaded
        viewModelScope.launch {
            combine(
                appsRepository.apps, updatedSystemApps, systemApps, userApps, appListFavorites
            ) { appList, updatedSysApps, sysApps, userApps, favorites ->
                ApplicationUtil.selectedAppTypes(
                    appList, updatedSysApps, sysApps, userApps, favorites
                ).filter { app ->
                    Utils.isPackageInstalled(
                        context.packageManager, app.appPackageName
                    )
                }
            }.let {
                combine(
                    it, filterInstaller, filterCategory, filterOthers
                ) { appList, installer, category, others ->
                    ApplicationUtil.filterApps(appList, installer, category, others)
                }
            }.let {
                combine(
                    it, appSortOrder, appSortFavorites, appSortAsc
                ) { appList, sortMode, sortFavorites, sortAsc ->
                    ApplicationUtil.sortAppData(appList, sortMode.ordinal, sortFavorites, sortAsc)
                }
            }.let {
                _searchQuery.debounce(500L).combine(it) { searchQuery, appList ->
                    val searchString = searchQuery?.trim()

                    return@combine if (searchString.isNullOrBlank()) {
                        appList
                    } else {
                        appList.filter {
                            it.appName.contains(
                                searchString, ignoreCase = true
                            ) || it.appPackageName.contains(
                                searchString, ignoreCase = true
                            )
                        }
                    }
                }
            }.collect { appList ->
                _mainFragmentState.update { state ->
                    state.copy(appList = appList,
                        isRefreshing = false,
                        selectedApp = state.selectedApp?.let { sa ->
                            appList.find {
                                it.appPackageName == sa.appPackageName
                            }
                        })
                }
            }
        }

        EventDispatcher.registerObserver(this, EventType.INSTALLED, EventType.UNINSTALLED)
    }

    override fun onCleared() {
        EventDispatcher.unregisterObserver(this, EventType.ANY)
        super.onCleared()
    }

    override fun onEventReceived(event: Event<*>) {
        when (event.eventType) {
            EventType.INSTALLED -> addApps(event.data as String)
            EventType.UNINSTALLED -> if (!Utils.isPackageInstalled(
                    context.packageManager, event.data as String
                )
            ) uninstallApps(event.data)

            else -> return
        }
    }

    /**
     * Select a specific Application from list in view
     * and set it in BottomSheet state
     * @param app selected application
     */
    fun selectApplication(app: ApplicationModel?) {
        _mainFragmentState.update { state ->
            state.copy(selectedApp = app)
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
    fun updateApps() {
        _mainFragmentState.update {
            it.copy(isRefreshing = true)
        }
        viewModelScope.launch {
            async { appsRepository.updateApps() }
        }
    }

    fun addApps(packageName: String) {
        viewModelScope.launch {
            async { appsRepository.addApp(ApplicationModel(context.packageManager, packageName)) }
        }
    }

    fun uninstallApps(app: ApplicationModel) {
        if (Utils.isPackageInstalled(context.packageManager, app.appPackageName)) return

        _mainFragmentState.update { state ->
            state.copy(
                isRefreshing = true,
                appList = state.appList.filter { it.appPackageName != app.appPackageName },
                selectedApp = if (state.selectedApp?.appPackageName == app.appPackageName) null else state.selectedApp
            )
        }
        viewModelScope.launch {
            async { appsRepository.removeApp(app) }
        }
    }

    private fun uninstallApps(packageName: String) {
        _mainFragmentState.value.appList.find { it.appPackageName == packageName }?.let {
            uninstallApps(it)
        }
    }

    /**
     * update State for List view inside UI once user changed selected app types inside the Settings
     * @param key key of switch preference who has changed
     * @param b true for should be selected, false if no longer should be included
     * @throws Exception if key is not "updated_system_apps", "system_apps" or "user_apps"
     */
    @Throws(Exception::class)
    fun changeSelection(key: String, b: Boolean) {
        _mainFragmentState.update { state ->
            state.copy(isRefreshing = true)
        }
        viewModelScope.launch {
            when (key) {
                "updated_system_apps" -> preferenceRepository.setUpdatedSysApps(b)
                "system_apps" -> preferenceRepository.setSysApps(b)
                "user_apps" -> preferenceRepository.setUserApps(b)
                else -> throw Exception("No available key provided!")
            }
        }
    }

    fun updateApp(app: ApplicationModel) {
        _mainFragmentState.update { state ->
            state.copy(appList = state.appList.toMutableList()
                .map { if (it.appPackageName == app.appPackageName) app else it })
        }

    }

    fun selectAllApps(select: Boolean) {
        _mainFragmentState.update { state ->
            state.copy(appList = state.appList.toMutableList().map { it.copy(isChecked = select) })
        }
    }

    fun setSortAsc(b: Boolean) {
        viewModelScope.launch { preferenceRepository.setAppSortAsc(b) }
    }

    fun setSortOrder(value: Int) {
        viewModelScope.launch { preferenceRepository.setAppSortOrder(value) }
    }

    fun setSortFavorites(b: Boolean) {
        viewModelScope.launch { preferenceRepository.setAppSortFavorites(b) }
    }

    fun setInstallationSource(value: String?) {
        viewModelScope.launch { preferenceRepository.setAppFilterInstaller(value) }
    }

    fun setCategory(s: String?) {
        viewModelScope.launch { preferenceRepository.setAppFilterCategory(s) }
    }

    fun setOtherFilter(strings: Set<String>) {
        viewModelScope.launch { preferenceRepository.setAppFilterOthers(strings) }
    }

    fun editFavorites(appPackageName: String, checked: Boolean) {
        viewModelScope.launch {
            ApplicationUtil.editFavorites(appListFavorites.first(), appPackageName, checked).let {
                preferenceRepository.setAppListFavorites(it)
            }
        }
    }

    fun saveSelectedApps() {
        runningTask = viewModelScope.launch {
            saveApps(mainFragmentState.value.appList.filter { it.isChecked })
        }
    }

    fun saveApp(app: ApplicationModel) {
        runningTask = viewModelScope.launch { saveApps(listOf(app)) }
    }

    /**
     * save multiple apps to filesystem
     * @param list of apps user wants to save
     */
    private suspend fun saveApps(list: List<ApplicationModel>) = coroutineScope {
        if (list.isEmpty()) {
            _extractionResult.emit(ExtractionResult.None)
            return@coroutineScope
        }

        val backupMode = backupXapk.first()
        var application: ApplicationModel? = null
        var errorMessage: String? = null

        _progressDialogState.update {
            val taskSize = if (backupMode) list.fold(0) { acc, applicationModel ->
                acc + (applicationModel.appSplitSourceDirectories?.size ?: 0) + 1
            } else list.size
            it.copy(
                title = this@AppListViewModel.context.getString(
                    R.string.progress_dialog_title_save, if (backupMode) "XAPK" else "APK"
                ), tasks = taskSize, shouldBeShown = true
            )
        }

        val job = launch extract@{
            list.forEach { app ->
                application = app
                val splits = arrayListOf(app.appSourceDirectory)
                if (!app.appSplitSourceDirectories.isNullOrEmpty() && backupMode) splits.addAll(
                    app.appSplitSourceDirectories!!
                )
                val appName = ApplicationUtil.appName(app, appName.first())

                _progressDialogState.update { state ->
                    state.copy(process = app.appPackageName)
                }

                val newFile = if (splits.size == 1) {
                    val savedApk = ApplicationUtil.saveApk(
                        this@AppListViewModel.context,
                        app.appSourceDirectory,
                        saveDir.first()!!,
                        appName
                    )
                    _progressDialogState.update { state ->
                        state.copy(
                            process = app.appPackageName, progress = state.progress + 1
                        )
                    }
                    savedApk
                } else ApplicationUtil.saveXapk(
                    this@AppListViewModel.context, splits.toTypedArray(), saveDir.first()!!, appName
                ) {
                    _progressDialogState.update { state ->
                        state.copy(
                            process = app.appPackageName, progress = state.progress + 1
                        )
                    }
                }
                when (newFile) {
                    is SaveApkResult.Failure -> errorMessage = newFile.errorMessage
                    is SaveApkResult.Success -> EventDispatcher.emitEvent(
                        Event(
                            EventType.SAVED, newFile.uri
                        )
                    )
                }
                if (errorMessage != null) {
                    _extractionResult.emit(
                        ExtractionResult.Failure(
                            application!!, errorMessage!!
                        )
                    )
                    this@extract.cancel()
                }
            }
        }
        job.join()
        if (list.size == 1) _extractionResult.emit(ExtractionResult.SuccessSingle(application!!))
        else _extractionResult.emit(ExtractionResult.SuccessMultiple(application!!, list.size))
        resetProgress()
    }

    fun createShareUrisForSelectedApps() {
        runningTask =
            viewModelScope.launch { createShareUrisForApps(mainFragmentState.value.appList.filter { it.isChecked }) }
    }

    fun createShareUrisForApp(app: ApplicationModel) {
        runningTask = viewModelScope.launch { createShareUrisForApps(listOf(app)) }
    }

    /**
     * create temp files for apps user want to save and get share Uris for them
     * @param list list of all apps
     */
    private suspend fun createShareUrisForApps(list: List<ApplicationModel>) = coroutineScope {
        if (list.isEmpty()) {
            _shareResult.emit(ShareResult.None)
            return@coroutineScope
        }

        val files = ArrayList<Uri>()
        val jobList = ArrayList<Deferred<Any?>>()
        val backupMode = backupXapk.first()

        _progressDialogState.update {
            it.copy(title = context.getString(R.string.progress_dialog_title_share),
                tasks = if (backupMode) list.fold(0) { acc, applicationModel ->
                    acc + (applicationModel.appSplitSourceDirectories?.size ?: 0) + 1
                } else list.size,
                shouldBeShown = true)
        }

        list.forEach { app ->
            jobList.add(async {
                val splits = arrayListOf(app.appSourceDirectory)
                if (!app.appSplitSourceDirectories.isNullOrEmpty() && backupMode) splits.addAll(
                    app.appSplitSourceDirectories!!
                )
                val name = ApplicationUtil.appName(app, appName.first())

                val uri = if (splits.size == 1) {
                    val shareUri = ApplicationUtil.shareApk(context, app, name)
                    _progressDialogState.update { state ->
                        state.copy(
                            progress = state.progress + 1
                        )
                    }
                    shareUri
                } else ApplicationUtil.shareXapk(
                    context, app, name
                ) {
                    _progressDialogState.update { state ->
                        state.copy(
                            progress = state.progress + 1
                        )
                    }
                }
                files.add(uri)
            })
        }
        jobList.awaitAll()
        if (files.size == 1) _shareResult.emit(ShareResult.SuccessSingle(files[0]))
        else _shareResult.emit(ShareResult.SuccessMultiple(files))
        resetProgress()
    }

    override fun resetProgress() {
        runningTask?.cancel()
        runningTask = null
        _progressDialogState.value = ProgressDialogUiState()
    }
}