package domilopment.apkextractor.data.repository.tasks

import domilopment.apkextractor.data.UiText
import domilopment.apkextractor.data.model.tasks.ProgressResult
import domilopment.apkextractor.data.model.tasks.TaskFailure

sealed interface TaskState {
    data object Pending : TaskState

    data class Progress(
        val title: UiText? = null,
        val process: String? = null,
        val progress: Float = 0f,
        val tasks: Int = 0
    ) : TaskState

    sealed interface Result : TaskState {
        data class Success<T>(val data: ProgressResult<T>) : Result
        data class Error(val reason: TaskFailure) : Result
    }

    data object Suspended : TaskState
}