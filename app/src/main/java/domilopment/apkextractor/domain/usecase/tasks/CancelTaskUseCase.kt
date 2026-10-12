package domilopment.apkextractor.domain.usecase.tasks

import domilopment.apkextractor.data.repository.tasks.TaskEngine
import javax.inject.Inject
import kotlin.uuid.Uuid

interface CancelTaskUseCase {
    suspend operator fun invoke(id: String)
}

class CancelTaskUseCaseImpl @Inject constructor(private val taskEngine: TaskEngine) : CancelTaskUseCase {
    override suspend operator fun invoke(id: String) {
        val uuid = runCatching { Uuid.parse(id) }.getOrNull() ?: return

        taskEngine.cancel(uuid)
    }
}