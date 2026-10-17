package domilopment.apkextractor.ui.viewModels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.data.MainScreenState
import domilopment.apkextractor.data.repository.analytics.AnalyticsRepository
import domilopment.apkextractor.data.repository.preferences.PreferenceRepository
import domilopment.apkextractor.domain.usecase.prefs.SaveDirUriUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val saveDirUseCase: SaveDirUriUseCase,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {
    private val _mainScreenState =
        MutableStateFlow<MainScreenState>(MainScreenState.AppBarMode.Default)
    val mainScreenStateFlow = _mainScreenState.asStateFlow()

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

    fun searchBarMode(query: String = "") {
        _mainScreenState.update { state ->
            val searchState = state as? MainScreenState.AppBarMode.Search
            searchState?.copy(query = query) ?: MainScreenState.AppBarMode.Search(query)
        }
    }

    fun resetAppBarState() {
        _mainScreenState.update {
            MainScreenState.AppBarMode.Default
        }
    }

    private fun actionMode(
        state: MainScreenState,
        selectAllItems: Boolean = (state as? MainScreenState.ActionMode)?.isSelectAllActive ?: false,
        selectedItems: Int = (state as? MainScreenState.ActionMode)?.selectedItemCount ?: 0
    ) : MainScreenState {
        return when (state) {
            is MainScreenState.AppBarMode -> if (selectedItems > 0) {
                MainScreenState.ActionMode(
                    returnTo = state,
                    isSelectAllActive = selectAllItems,
                    selectedItemCount = selectedItems
                )
            } else state

            is MainScreenState.ActionMode -> if (selectedItems == 0) {
                state.returnTo
            } else state.copy(
                isSelectAllActive = selectAllItems, selectedItemCount = selectedItems
            )
        }
    }

    fun actionMode(selectAllItems: Boolean, selectedItems: Int) {
        _mainScreenState.update { state ->
            actionMode(state, selectAllItems, selectedItems)
        }
    }

    fun actionMode(selectAllItems: Boolean) {
        _mainScreenState.update { state ->
            actionMode(state, selectAllItems)
        }
    }

    fun onReturnUiMode() {
        _mainScreenState.update { state ->
            when (state) {
                is MainScreenState.AppBarMode.Search -> MainScreenState.AppBarMode.Default
                is MainScreenState.ActionMode -> state.returnTo
                else -> state
            }
        }
    }

    fun hideSplashScreen() {
        _keepSplashScreen.value = false
    }

    fun setSaveDir(value: Uri) {
        viewModelScope.launch { saveDirUseCase.invoke(value) }
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