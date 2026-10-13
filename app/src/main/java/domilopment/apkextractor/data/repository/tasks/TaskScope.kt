package domilopment.apkextractor.data.repository.tasks

import domilopment.apkextractor.data.model.tasks.TaskFailure

interface TaskScope {
    suspend fun updateProgress(update: (TaskState.Progress) -> TaskState.Progress)

    fun fail(failure: TaskFailure): Nothing
}