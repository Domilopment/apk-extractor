package domilopment.apkextractor.domain.usecase.tasks

import domilopment.apkextractor.data.TaskUiState
import domilopment.apkextractor.data.model.tasks.ProgressResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile

interface GetTaskResultUseCase {
    operator fun invoke(id: String): Flow<ProgressResult<*>>
}

class GetTaskResultUseCaseImpl(
    private val getTaskProgress: GetTaskProgressUseCase
) : GetTaskResultUseCase {
    override fun invoke(id: String): Flow<ProgressResult<*>> {
        return getTaskProgress(id)
            .takeWhile { it != null }
            .filterIsInstance<TaskUiState.Result.Success<*>>()
            .map { it.data }
    }
}
