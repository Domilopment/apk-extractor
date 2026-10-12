package domilopment.apkextractor.data.model.tasks

open class ProgressError(
    override val errorMessage: String?,
    override val throwable: Throwable?,
    override val extra: Any? = null
) : TaskFailure