package domilopment.apkextractor.ui.viewModels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.data.ActionModeState
import domilopment.apkextractor.data.MainScreenState
import domilopment.apkextractor.data.UiState
import domilopment.apkextractor.data.repository.analytics.AnalyticsRepository
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {
    var mainScreenState by mutableStateOf(MainScreenState())
        private set

    var actionModeState by mutableStateOf(ActionModeState())
        private set

    private val _keepSplashScreen = MutableStateFlow(true)
    val keepSplashScreen = _keepSplashScreen.asStateFlow()

    val saveDir = preferenceRepository.saveDir.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), null
    )
    val materialYou = preferenceRepository.useMaterialYou.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), true
    )
    val autoBackup = preferenceRepository.autoBackupService.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), false
    )
    val updateOnStart = preferenceRepository.checkUpdateOnStart.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), true
    )

    val firstLaunch = preferenceRepository.firstLaunch.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), false
    )

    fun updateSearchQuery(query: String) {
        mainScreenState = mainScreenState.copy(appBarSearchText = query)
    }

    fun setSearchBarState() {
        mainScreenState = mainScreenState.copy(uiState = UiState.Search)
    }

    fun setActionModeState() {
        mainScreenState =
            mainScreenState.copy(uiState = UiState.ActionMode(previousState = mainScreenState.uiState))
    }

    fun resetAppBarState() {
        mainScreenState = MainScreenState()
    }

    fun updateActionMode(
        selectAllItems: Boolean = actionModeState.selectAllItemsCheck,
        selectedItems: Int = actionModeState.selectedItemCount
    ) {
        if (selectedItems == 0) {
            mainScreenState = mainScreenState.copy(
                uiState = (mainScreenState.uiState as UiState.ActionMode).previousState
            )
            actionModeState = ActionModeState()
        } else actionModeState = actionModeState.copy(
            selectAllItemsCheck = selectAllItems, selectedItemCount = selectedItems
        )
    }

    fun onReturnUiMode() {
        mainScreenState = mainScreenState.copy(
            uiState = when (mainScreenState.uiState) {
                UiState.Search -> UiState.Default
                is UiState.ActionMode -> {
                    actionModeState = ActionModeState()
                    (mainScreenState.uiState as UiState.ActionMode).previousState
                }

                else -> mainScreenState.uiState
            }
        )
    }

    fun hideSplashScreen() {
        _keepSplashScreen.value = false
    }

    fun setSaveDir(value: Uri) {
        viewModelScope.launch { preferenceRepository.setSaveDir(value) }
    }

    fun setUpdateOnStart(b: Boolean) {
        viewModelScope.launch { preferenceRepository.setCheckUpdateOnStart(b) }
    }

    fun setFirstLaunch(analytics: Boolean, crashlytics: Boolean, performance: Boolean) {
        analyticsRepository.setAnalyticsCollectionEnabled(analytics)
        analyticsRepository.setCrashlyticsCollectionEnabled(crashlytics)
        analyticsRepository.setPerformanceCollectionEnabled(performance)
        viewModelScope.launch {
            preferenceRepository.setAnalytics(analytics)
            preferenceRepository.setCrashlytics(crashlytics)
            preferenceRepository.setPerformance(performance)
            preferenceRepository.setFirstLaunch(false)
        }
    }
}