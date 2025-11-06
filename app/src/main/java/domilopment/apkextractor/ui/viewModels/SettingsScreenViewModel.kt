package domilopment.apkextractor.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import domilopment.apkextractor.data.SettingsScreenAppAutoBackUpListState
import domilopment.apkextractor.data.SettingsScreenState
import domilopment.apkextractor.data.repository.analytics.AnalyticsRepository
import domilopment.apkextractor.data.repository.applications.ApplicationRepository
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.domain.mapper.AppModelToApplicationListModelMapper
import domilopment.apkextractor.domain.mapper.mapAll
import domilopment.apkextractor.domain.usecase.appList.IsAppInstalledUseCase
import domilopment.apkextractor.utils.FileUtil
import domilopment.apkextractor.utils.settings.AppSortOptions
import domilopment.apkextractor.utils.settings.ApplicationUtil
import domilopment.apkextractor.utils.settings.Spacer
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
    @param:ApplicationContext private val context: Context,
    private val settings: PreferenceRepository,
    private val analytics: AnalyticsRepository,
    private val isAppInstalled: IsAppInstalledUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<SettingsScreenState> =
        MutableStateFlow(SettingsScreenState())
    val uiState: StateFlow<SettingsScreenState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appsRepository.apps.map { apps ->
                async(Dispatchers.Default) {
                    ApplicationUtil.selectedAppTypes(
                        apps,
                        selectUpdatedSystemApps = true,
                        selectSystemApps = false,
                        selectUserApps = true,
                    ).filter { app ->
                        isAppInstalled(app.packageName)
                    }.let {
                        AppModelToApplicationListModelMapper(context.packageManager).mapAll(it)
                            .filterNotNull()
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
            settings.appSaveNameSpacer.collect {
                _uiState.update { state -> state.copy(saveNameSpacer = it) }
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
            settings.backupModeApkBundle.collect {
                _uiState.update { state -> state.copy(backupModeApkBundle = it) }
            }
        }
        viewModelScope.launch {
            settings.analytics.collect {
                _uiState.update { state -> state.copy(analytics = it) }
            }
        }
        viewModelScope.launch {
            settings.crashlytics.collect {
                _uiState.update { state -> state.copy(crashlytics = it) }
            }
        }
        viewModelScope.launch {
            settings.performance.collect {
                _uiState.update { state -> state.copy(performance = it) }
            }
        }
        viewModelScope.launch {
            settings.bundleFileInfo.collect {
                _uiState.update { state -> state.copy(bundleFileInfo = it) }
            }
        }
    }

    fun setAppSaveName(set: Set<String>) {
        viewModelScope.launch { settings.setAppSaveName(set) }
    }

    fun setAppSaveNameSpacer(spacer: String) {
        viewModelScope.launch {
            spacer.let {
                Spacer.fromName(it) ?: Spacer.SPACE
            }.let {
                settings.setAppSaveNameSpacer(it)
            }
        }
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

    fun setBackupModeApkBundle(b: Boolean) {
        viewModelScope.launch { settings.setBackupModeApkBundle(b) }
    }

    fun onDeleteFirebaseInstallationsId() {
        viewModelScope.launch { analytics.delete() }
    }

    fun setAnalytics(b: Boolean) {
        viewModelScope.launch {
            analytics.setAnalyticsCollectionEnabled(b)
            settings.setAnalytics(b)
        }
    }

    fun setCrashlytics(b: Boolean) {
        viewModelScope.launch {
            analytics.setCrashlyticsCollectionEnabled(b)
            settings.setCrashlytics(b)
        }
    }

    fun setPerformance(b: Boolean) {
        viewModelScope.launch {
            analytics.setPerformanceCollectionEnabled(b)
            settings.setPerformance(b)
        }
    }

    fun setBundleFileInfo(suffix: String) {
        viewModelScope.launch {
            suffix.let {
                FileUtil.FileInfo.fromSuffix(it) ?: FileUtil.FileInfo.APKS
            }.let {
                settings.setBundleFileInfo(it)
            }
        }
    }
}
