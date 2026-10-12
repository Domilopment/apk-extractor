package domilopment.apkextractor.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.data.model.appList.AppDetailScreenState
import domilopment.apkextractor.data.model.appList.ApplicationModel
import domilopment.apkextractor.data.model.appList.ExtractionResult
import domilopment.apkextractor.data.model.appList.ShareResult
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.domain.usecase.appList.*
import domilopment.apkextractor.domain.usecase.tasks.DismissTaskUseCase
import domilopment.apkextractor.domain.usecase.tasks.GetTaskResultUseCase
import domilopment.apkextractor.utils.apkActions.ApkActionIntent
import domilopment.apkextractor.utils.settings.ApplicationUtil
import domilopment.apkextractor.ui.navigation.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AppDetailViewModel.Factory::class)
class AppDetailViewModel @AssistedInject constructor(
    @Assisted private val navKey: Route.AppDetails,
    private val getAppDetailsUseCase: GetAppDetailsUseCase,
    private val preferenceRepository: PreferenceRepository,
    private val saveAppIcon: SaveImageUseCase,
    private val openSettings: ShowAppSettingsUseCase,
    private val openApp: OpenAppUseCase,
    private val uninstallApp: UninstallAppUseCase,
    private val openAppStorePage: OpenAppShopDetailsUseCase,
    // Apps repository interactions
    private val removeApp: RemoveAppUseCase,
    private val saveApps: SaveAppsUseCase,
    private val shareApps: ShareAppsUseCase,
    // Task Store interactions
    private val taskResult: GetTaskResultUseCase,
    private val dismissTask: DismissTaskUseCase
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(navKey: Route.AppDetails): AppDetailViewModel
    }

    private val _uiState = MutableStateFlow(AppDetailScreenState())
    val uiState: StateFlow<AppDetailScreenState> = _uiState.asStateFlow()

    private val _onProgressRequest = MutableSharedFlow<String>()
    val onProgressRequest: SharedFlow<String> = _onProgressRequest.asSharedFlow()

    private val _extractionResult = MutableSharedFlow<ExtractionResult>()
    val extractionResult = _extractionResult.asSharedFlow()

    private val _shareResult = MutableSharedFlow<ShareResult>()
    val shareResult = _shareResult.asSharedFlow()

    init {
        loadAppDetails()

        // TODO: Remove when dedicated Tasks list screen is implemented
        viewModelScope.launch {
            onProgressRequest.collect { id ->
                taskResult(id).collect { result ->
                    when (result) {
                        is ExtractionResult -> _extractionResult.emit(result)
                        is ShareResult -> _shareResult.emit(result)
                    }
                    dismissTask(id)
                }
            }
        }
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
                is ApkActionIntent.Share -> createShareUrisForApp(intent.app)
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
     * @param app model of apk user wants to save
     */
    private fun save(app: ApplicationModel) {
        viewModelScope.launch {
            val id = saveApps(listOf(app))
            _onProgressRequest.emit(id)
        }
    }

    /**
     * create temp files for app user want to save and get share Uri for it
     * @param app model of app
     */
    private fun createShareUrisForApp(app: ApplicationModel) {
        viewModelScope.launch {
            val id = shareApps(listOf(app))
            _onProgressRequest.emit(id)
        }
    }
}
