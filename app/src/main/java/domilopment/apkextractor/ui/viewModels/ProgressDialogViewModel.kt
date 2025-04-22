package domilopment.apkextractor.ui.viewModels

import domilopment.apkextractor.data.ProgressDialogUiState
import kotlinx.coroutines.flow.StateFlow

interface ProgressDialogViewModel {
    val progressDialogState: StateFlow<ProgressDialogUiState?>

    /**
     * Reset Progress dialog state back to default
     */
    fun resetProgress()
}