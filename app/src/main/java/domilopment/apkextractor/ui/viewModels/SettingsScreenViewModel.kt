package domilopment.apkextractor.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.data.SettingsScreenAppAutoBackUpListState
import domilopment.apkextractor.data.SettingsScreenState
import domilopment.apkextractor.data.repository.applications.ApplicationRepository
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.domain.usecase.appList.IsAppInstalledUseCase
import domilopment.apkextractor.utils.settings.AppSortOptions
import domilopment.apkextractor.utils.settings.ApplicationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    appsRepository: ApplicationRepository,
    private val settings: PreferenceRepository,
    private val isAppInstalled: IsAppInstalledUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<SettingsScreenState> =
        MutableStateFlow(SettingsScreenState())
    val uiState: StateFlow<SettingsScreenState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appsRepository.apps.map { apps ->
                async(Dispatchers.IO) {
                    ApplicationUtil.selectedAppTypes(
                        apps,
                        selectUpdatedSystemApps = true,
                        selectSystemApps = false,
                        selectUserApps = true,
                        emptySet()
                    ).filter { app ->
                        isAppInstalled(app.appPackageName)
                    }.let {
                        ApplicationUtil.sortAppData(
                            it,
                            sortMode = AppSortOptions.SORT_BY_NAME.ordinal,
                            sortFavorites = false,
                            sortAsc = true
                        )
                    }.let { list ->
                        SettingsScreenAppAutoBackUpListState(list)
                    }
                }
            }.collect {
                _uiState.update { state -> state.copy(autoBackupAppsListState = it.await()) }
            }
        }
        viewModelScope.launch {
            settings.saveDir.collect {
                _uiState.update { state -> state.copy(saveDir = it) }
            }
        }
        viewModelScope.launch {
            settings.appSaveName.collect {
                _uiState.update { state -> state.copy(saveName = it) }
            }
        }
        viewModelScope.launch {
            settings.autoBackupService.collect {
                _uiState.update { state -> state.copy(autoBackupService = it) }
            }
        }
        viewModelScope.launch {
            settings.autoBackupAppList.collect {
                _uiState.update { state -> state.copy(autoBackupList = it) }
            }
        }
        viewModelScope.launch {
            settings.nightMode.collect {
                _uiState.update { state -> state.copy(nightMode = it) }
            }
        }
        viewModelScope.launch {
            settings.useMaterialYou.collect {
                _uiState.update { state -> state.copy(useMaterialYou = it) }
            }
        }
        viewModelScope.launch {
            settings.appRightSwipeAction.collect {
                _uiState.update { state -> state.copy(rightSwipeAction = it.preferenceValue) }
            }
        }
        viewModelScope.launch {
            settings.appLeftSwipeAction.collect {
                _uiState.update { state -> state.copy(leftSwipeAction = it.preferenceValue) }
            }
        }
        viewModelScope.launch {
            settings.appSwipeActionCustomThreshold.collect {
                _uiState.update { state -> state.copy(swipeActionCustomThreshold = it) }
            }
        }
        viewModelScope.launch {
            settings.appSwipeActionThresholdMod.collect {
                _uiState.update { state -> state.copy(swipeActionThresholdMod = it) }
            }
        }
        viewModelScope.launch {
            settings.checkUpdateOnStart.collect {
                _uiState.update { state -> state.copy(checkUpdateOnStart = it) }
            }
        }
        viewModelScope.launch {
            settings.backupModeXapk.collect {
                _uiState.update { state -> state.copy(backupModeXapk = it) }
            }
        }
    }

    fun setAppSaveName(set: Set<String>) {
        viewModelScope.launch { settings.setAppSaveName(set) }
    }

    fun setAutoBackupService(b: Boolean) {
        viewModelScope.launch { settings.setAutoBackupService(b) }
    }

    fun setAutoBackupList(set: Set<String>) {
        viewModelScope.launch { settings.setListOfAutoBackupApps(set) }
    }

    fun setNightMode(mode: Int) {
        viewModelScope.launch { settings.setNightMode(mode) }
    }

    fun setUseMaterialYou(b: Boolean) {
        viewModelScope.launch { settings.setUseMaterialYou(b) }
    }

    fun setRightSwipeAction(s: String) {
        viewModelScope.launch { settings.setRightSwipeAction(s) }
    }

    fun setLeftSwipeAction(s: String) {
        viewModelScope.launch { settings.setLeftSwipeAction(s) }
    }

    fun setSwipeActionCustomThreshold(b: Boolean) {
        viewModelScope.launch { settings.setSwipeActionCustomThreshold(b) }
    }

    fun setSwipeActionThresholdMod(f: Float) {
        viewModelScope.launch { settings.setSwipeActionThresholdMod(f) }
    }

    fun setCheckUpdateOnStart(b: Boolean) {
        viewModelScope.launch { settings.setCheckUpdateOnStart(b) }
    }

    fun setBackupModeXapk(b: Boolean) {
        viewModelScope.launch { settings.setBackupModeXapk(b) }
    }
}
