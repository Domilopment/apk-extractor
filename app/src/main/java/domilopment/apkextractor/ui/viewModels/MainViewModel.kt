package domilopment.apkextractor.ui.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import domilopment.apkextractor.data.ActionModeState
import domilopment.apkextractor.data.MainScreenState
import domilopment.apkextractor.dependencyInjection.preferenceDataStore.PreferenceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val preferenceRepository: PreferenceRepository) :
    ViewModel() {
    var mainScreenState by mutableStateOf(MainScreenState())
        private set

    var actionModeState by mutableStateOf(ActionModeState())
        private set

    val saveDir = preferenceRepository.saveDir.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), null
    )
    val materialYou = preferenceRepository.useMaterialYou
    val autoBackup = preferenceRepository.autoBackupService
    val updateOnStart = preferenceRepository.checkUpdateOnStart

    fun updateSearchQuery(query: String) {
        mainScreenState = mainScreenState.copy(appBarSearchText = query)
    }

    fun setSearchBarState(open: Boolean) {
        mainScreenState = mainScreenState.copy(isAppBarSearchActive = open)
    }

    fun setActionModeState(active: Boolean) {
        mainScreenState = mainScreenState.copy(isActionModeActive = active)
    }

    fun resetAppBarState() {
        mainScreenState = MainScreenState()
    }

    fun updateActionMode(
        selectAllItems: Boolean = actionModeState.selectAllItemsCheck,
        selectedItems: Int = actionModeState.selectedItemCount
    ) {
        actionModeState = actionModeState.copy(
            selectAllItemsCheck = selectAllItems, selectedItemCount = selectedItems
        )
    }

    fun resetActionMode() {
        actionModeState = ActionModeState()
    }

    fun setSaveDir(value: String) {
        viewModelScope.launch { preferenceRepository.setSaveDir(value) }
    }

    fun setUpdateOnStart(b: Boolean) {
        viewModelScope.launch { preferenceRepository.setCheckUpdateOnStart(b) }
    }
}