package domilopment.apkextractor.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.R
import domilopment.apkextractor.data.ProgressDialogUiState
import domilopment.apkextractor.data.UiText
import domilopment.apkextractor.data.model.appList.AppDetailScreenState
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.data.model.appList.ExtractionResult
import domilopment.apkextractor.data.model.appList.ShareResult
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.domain.usecase.appList.*
import domilopment.apkextractor.utils.apkActions.ApkActionIntent
import domilopment.apkextractor.utils.settings.ApplicationUtil
import domilopment.apkextractor.ui.navigation.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AppDetailViewModel.Factory::class)
class AppDetailViewModel @AssistedInject constructor(
    @Assisted private val navKey: Route.AppDetails,
    private val getAppDetailsUseCase: GetAppDetailsUseCase,
    private val preferenceRepository: PreferenceRepository,
    private val saveApp: SaveAppsUseCase,
    private val shareApps: ShareAppsUseCase,
    private val saveAppIcon: SaveImageUseCase,
    private val openSettings: ShowAppSettingsUseCase,
    private val openApp: OpenAppUseCase,
    private val uninstallApp: UninstallAppUseCase,
    private val openAppStorePage: OpenAppShopDetailsUseCase,
    // Apps repository interactions
    private val removeApp: RemoveAppUseCase,
) : ViewModel(), ProgressDialogViewModel {
    @AssistedFactory
    interface Factory {
        fun create(navKey: Route.AppDetails): AppDetailViewModel
    }

    private val _uiState = MutableStateFlow(AppDetailScreenState())
    val uiState: StateFlow<AppDetailScreenState> = _uiState.asStateFlow()

    private val _progressDialogState: MutableStateFlow<ProgressDialogUiState?> =
        MutableStateFlow(null)
    override val progressDialogState: StateFlow<ProgressDialogUiState?> =
        _progressDialogState.asStateFlow()

    private val _extractionResult = MutableSharedFlow<ExtractionResult>()
    val extractionResult = _extractionResult.asSharedFlow()

    private val _shareResult = MutableSharedFlow<ShareResult>()
    val shareResult = _shareResult.asSharedFlow()

    // Task for ProgressDialog to Cancel
    private var runningTask: Job? = null

    init {
        loadAppDetails()
    }

    private fun loadAppDetails() {
        viewModelScope.launch {
            val result = async(Dispatchers.IO) { getAppDetailsUseCase(navKey.packageName) }
            _uiState.update { it.copy(app = result.await(), isLoading = false) }
        }
    }

    fun removeApp(app: ApplicationModel) {
        viewModelScope.launch {
            removeApp.invoke(app.appPackageName)
        }
    }

    fun editFavorites(checked: Boolean) {
        viewModelScope.launch {
            val favorites = preferenceRepository.appListFavorites.first()
            ApplicationUtil.editFavorites(favorites, navKey.packageName, checked).let {
                preferenceRepository.setAppListFavorites(it)
            }
            _uiState.update { state ->
                state.copy(app = state.app?.copy(isFavorite = checked))
            }
        }
    }

    fun appActionIntent(intent: ApkActionIntent) {
        viewModelScope.launch {
            when (intent) {
                is ApkActionIntent.Save -> save(intent.app)
                is ApkActionIntent.Share -> createShareUrisForApps(intent.app)
                is ApkActionIntent.Icon -> saveAppIcon(intent.app, intent.showSnackbar)
                is ApkActionIntent.Settings -> openSettings(intent.app)
                is ApkActionIntent.Open -> openApp(intent.app)
                is ApkActionIntent.Uninstall -> uninstallApp(intent.app)
                is ApkActionIntent.StorePage -> openAppStorePage(intent.app, intent.showSnackbar)
                ApkActionIntent.None -> Unit
            }
        }
    }

    /**
     * save multiple apps to filesystem
     * @param list of apps user wants to save
     */
    private suspend fun save(app: ApplicationModel) = coroutineScope {
        saveApp.invoke(listOf(app)).collect {
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

                ExtractionResult.NoSaveDir -> {
                    _extractionResult.emit(it)
                    resetProgress()
                }
            }
        }
    }

    /**
     * create temp files for apps user want to save and get share Uris for them
     * @param list list of all apps
     */
    private suspend fun createShareUrisForApps(app: ApplicationModel) = coroutineScope {
        shareApps.invoke(listOf(app)).collect {
            when (it) {
                ShareResult.None -> resetProgress()
                is ShareResult.Init -> _progressDialogState.update { state ->
                    ProgressDialogUiState(
                        title = UiText(R.string.progress_dialog_title_share), tasks = it.tasks
                    )
                }

                ShareResult.Progress -> _progressDialogState.update { state ->
                    state?.copy(
                        progress = state.progress + 1f
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
        _progressDialogState.value = null // Hide Dialog after everything should be cleaned up
    }
}