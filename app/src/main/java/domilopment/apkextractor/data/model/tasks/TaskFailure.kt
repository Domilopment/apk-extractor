package domilopment.apkextractor.data.model.tasks

interface TaskFailure {
    val errorMessage: String?
    val throwable: Throwable?
    val extra: Any?
}