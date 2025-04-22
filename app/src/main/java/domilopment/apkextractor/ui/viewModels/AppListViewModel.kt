package domilopment.apkextractor.ui.viewModels

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.R
import domilopment.apkextractor.data.*
import domilopment.apkextractor.data.model.appList.AppListScreenState
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.data.model.appList.ExtractionResult
import domilopment.apkextractor.data.model.appList.ShareResult
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.domain.usecase.appList.AddAppUseCase
import domilopment.apkextractor.domain.usecase.appList.GetAppListUseCase
import domilopment.apkextractor.domain.usecase.appList.SaveAppsUseCase
import domilopment.apkextractor.domain.usecase.appList.ShareAppsUseCase
import domilopment.apkextractor.domain.usecase.appList.RemoveAppUseCase
import domilopment.apkextractor.domain.usecase.appList.UpdateAppsUseCase
import domilopment.apkextractor.utils.settings.ApplicationUtil
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val addApp: AddAppUseCase,
    private val appList: GetAppListUseCase,
    private val saveApp: SaveAppsUseCase,
    private val shareApps: ShareAppsUseCase,
    private val uninstallApp: RemoveAppUseCase,
    private val updateApps: UpdateAppsUseCase,
) : ViewModel(), ProgressDialogViewModel {

    private val _mainFragmentState: MutableStateFlow<AppListScreenState> =
        MutableStateFlow(AppListScreenState())
    val mainFragmentState: StateFlow<AppListScreenState> = _mainFragmentState.asStateFlow()

    private val _searchQuery: MutableStateFlow<String?> = MutableStateFlow(null)

    private val _progressDialogState: MutableStateFlow<ProgressDialogUiState?> =
        MutableStateFlow(null)
    override val progressDialogState: StateFlow<ProgressDialogUiState?> =
        _progressDialogState.asStateFlow()

    private val _extractionResult: MutableSharedFlow<ExtractionResult> = MutableSharedFlow()
    val extractionResult: SharedFlow<ExtractionResult> = _extractionResult.asSharedFlow()

    private val _shareResult: MutableSharedFlow<ShareResult> = MutableSharedFlow()
    val shareResult: SharedFlow<ShareResult> = _shareResult.asSharedFlow()

    private val appListFavorites = preferenceRepository.appListFavorites
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

    // Task for ProgressDialog to Cancel
    private var runningTask: Job? = null

    init {
        // Set applications in view once they are loaded
        viewModelScope.launch {
            appList(_searchQuery).collect { appList ->
                _mainFragmentState.update { state ->
                    state.copy(
                        appList = appList,
                        isRefreshing = false,
                        selectedApp = state.selectedApp?.let { sa ->
                            appList.find {
                                it.appPackageName == sa.appPackageName
                            }
                        })
                }
            }
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
            updateApps.invoke()
        }
    }

    fun addApps(packageName: String) {
        viewModelScope.launch {
            addApp(packageName)
        }
    }

    fun uninstallApps(app: ApplicationModel) {
        viewModelScope.launch {
            uninstallApp(app.appPackageName)
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
            state.copy(
                appList = state.appList.toMutableList()
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
        saveApp.invoke(list).collect {
            when (it) {
                ExtractionResult.None -> resetProgress()
                is ExtractionResult.Init -> _progressDialogState.update { state ->
                    ProgressDialogUiState(
                        title = UiText(R.string.progress_dialog_title_save), tasks = it.tasks
                    )
                }

                is ExtractionResult.Progress -> _progressDialogState.update { state ->
                    state?.copy(
                        process = it.app.appPackageName,
                        progress = state.progress + it.progressIncrement
                    )
                }

                is ExtractionResult.SuccessMultiple, is ExtractionResult.SuccessSingle, is ExtractionResult.Failure -> {
                    _extractionResult.emit(it)
                    resetProgress()
                }
            }
        }
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
        shareApps.invoke(list).collect {
            when (it) {
                ShareResult.None -> resetProgress()
                is ShareResult.Init -> _progressDialogState.update { state ->
                    ProgressDialogUiState(
                        title = UiText(R.string.progress_dialog_title_share), tasks = it.tasks
                    )
                }

                ShareResult.Progress -> _progressDialogState.update { state ->
                    state?.copy(
                        progress = state.progress + 1
                    )
                }

                is ShareResult.SuccessSingle, is ShareResult.SuccessMultiple -> {
                    _shareResult.emit(it)
                    resetProgress()
                }
            }
        }
    }

    override fun resetProgress() {
        runningTask?.cancel()
        runningTask = null
        _progressDialogState.value = null
    }
}