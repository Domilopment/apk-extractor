package domilopment.apkextractor.data.model.appList

import domilopment.apkextractor.data.model.tasks.TaskFailure

sealed interface ExtractionError: TaskFailure {
    data class Failure(
        override val errorMessage: String?,
        override val throwable: Throwable? = null,
        override val extra: ApplicationModel
    ): ExtractionError

    data object NoSaveDir: ExtractionError {
        override val errorMessage: String? = null
        override val throwable: Throwable? = null
        override val extra: Any? = null
    }

    data object None : ExtractionError {
        override val errorMessage: String? = null
        override val throwable: Throwable? = null
        override val extra: Any? = null
    }
}