package domilopment.apkextractor.domain.mapper

import domilopment.apkextractor.data.TaskUiState
import domilopment.apkextractor.data.repository.tasks.TaskState

object TaskProgressToTaskUiStateMapper: Mapper<TaskState, TaskUiState> {
    override fun map(from: TaskState): TaskUiState {
        return when (from) {
            is TaskState.Pending -> TaskUiState.Pending
            is TaskState.Result.Success<*> -> TaskUiState.Result.Success(from.data)
            is TaskState.Result.Error -> TaskUiState.Result.Error(from.reason)
            is TaskState.Progress -> TaskUiState.Progress(
                title = from.title,
                process = from.process,
                progress = from.progress,
                tasks = from.tasks
            )

            is TaskState.Suspended -> TaskUiState.Suspended
        }
    }
}
