package domilopment.apkextractor.data.repository.tasks

interface TaskScope {
    suspend fun updateProgress(update: (TaskState.Progress) -> TaskState.Progress)
}