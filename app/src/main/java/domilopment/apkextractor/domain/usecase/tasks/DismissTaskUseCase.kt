package domilopment.apkextractor.domain.usecase.tasks

import domilopment.apkextractor.data.repository.tasks.TaskStore
import kotlin.uuid.Uuid

interface DismissTaskUseCase {
    operator fun invoke(id: String)
}

class DismissTaskUseCaseImpl(private val taskStore: TaskStore): DismissTaskUseCase {
    override fun invoke(id: String) {
        val uuid = runCatching { Uuid.parse(id) }.getOrNull() ?: return

        taskStore.remove(uuid)
    }
}