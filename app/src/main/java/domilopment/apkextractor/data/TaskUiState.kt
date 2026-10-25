package domilopment.apkextractor.data

import domilopment.apkextractor.data.model.tasks.ProgressResult
import domilopment.apkextractor.data.model.tasks.TaskFailure

sealed interface TaskUiState {
    data object Pending : TaskUiState

    data class Progress(
        val title: UiText? = null,
        val process: String? = null,
        val progress: Float = 0f,
        val tasks: Int = 0
    ) : TaskUiState

    sealed interface Result : TaskUiState {
        data class Success<T>(val data: ProgressResult<T>) : Result
        data class Error(val reason: TaskFailure) : Result
    }

    data object Canceled : TaskUiState
}
