package domilopment.apkextractor.domain.usecase.tasks

import domilopment.apkextractor.data.TaskUiState
import domilopment.apkextractor.data.repository.tasks.TaskStore
import domilopment.apkextractor.domain.mapper.TaskProgressToTaskUiStateMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

interface GetTaskProgressUseCase {
    operator fun invoke(id: String): Flow<TaskUiState?>
}

class GetTaskProgressUseCaseImpl(
    private val taskStore: TaskStore
) : GetTaskProgressUseCase {
    override fun invoke(id: String): Flow<TaskUiState?> {
        val uuid = runCatching { Uuid.parse(id) }.getOrNull()
            ?: return flowOf(null)

        return taskStore.get(uuid)
            .map { state -> state?.let(TaskProgressToTaskUiStateMapper::map) }
            .distinctUntilChanged()
    }
}