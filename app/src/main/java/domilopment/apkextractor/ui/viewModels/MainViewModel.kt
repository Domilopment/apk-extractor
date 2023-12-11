package domilopment.apkextractor.ui.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import domilopment.apkextractor.data.ActionModeState
import domilopment.apkextractor.data.MainScreenState

class MainViewModel : ViewModel() {
    var mainScreenState by mutableStateOf(MainScreenState())
        private set

    var actionModeState by mutableStateOf(ActionModeState())
        private set

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
}