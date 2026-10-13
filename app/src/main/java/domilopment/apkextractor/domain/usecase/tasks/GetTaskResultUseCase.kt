package domilopment.apkextractor.domain.usecase.tasks

import domilopment.apkextractor.data.TaskUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.takeWhile

interface GetTaskResultUseCase {
    operator fun invoke(id: String): Flow<TaskUiState.Result>
}

class GetTaskResultUseCaseImpl(
    private val getTaskProgress: GetTaskProgressUseCase
) : GetTaskResultUseCase {
    override fun invoke(id: String): Flow<TaskUiState.Result> {
        return getTaskProgress(id)
            .takeWhile { it != null }
            .filterIsInstance<TaskUiState.Result>()
    }
}
