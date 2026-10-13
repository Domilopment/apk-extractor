package domilopment.apkextractor.domain.usecase.tasks

import domilopment.apkextractor.data.repository.tasks.TaskEngine
import domilopment.apkextractor.data.repository.tasks.TaskScope
import domilopment.apkextractor.data.repository.tasks.TaskState
import javax.inject.Inject

interface LaunchTaskUseCase {
    suspend operator fun invoke(
        initialState: TaskState.Progress,
        block: suspend TaskScope.() -> TaskState.Result
    ): String
}

class LaunchTaskUseCaseImpl @Inject constructor(private val taskEngine: TaskEngine) : LaunchTaskUseCase {
    override suspend operator fun invoke(initialState: TaskState.Progress, block: suspend TaskScope.() -> TaskState.Result): String {
        return taskEngine.launch(initialState, block).toString()
    }
}