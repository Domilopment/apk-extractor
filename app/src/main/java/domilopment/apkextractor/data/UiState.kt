package domilopment.apkextractor.data

sealed interface UiState {
    data object Default: UiState
    data object Search: UiState
    data class ActionMode(val previousState: UiState): UiState
}