package domilopment.apkextractor.data.model.appList

import domilopment.apkextractor.data.model.tasks.ProgressResult
import domilopment.apkextractor.data.model.tasks.TaskFailure

sealed interface ExtractionResult {
    sealed class ExtractionSuccess(override val data: ApplicationModel?) : ExtractionResult,
        ProgressResult<ApplicationModel?> {
        data class SuccessSingle(val app: ApplicationModel) : ExtractionSuccess(app)
        data class SuccessMultiple(val app: ApplicationModel, val backupsCount: Int) :
            ExtractionSuccess(app)
    }

    sealed interface ExtractionError : ExtractionResult, TaskFailure {
        data class Failure(
            override val errorMessage: String?,
            override val throwable: Throwable? = null,
            override val extra: ApplicationModel
        ) : ExtractionError

        data object NoSaveDir : ExtractionError {
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
}
