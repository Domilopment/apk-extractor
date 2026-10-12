package domilopment.apkextractor.data.model.appList

import domilopment.apkextractor.data.model.tasks.ProgressResult

/**
 * Class used as return flow for saving one or multiple apk files to public storage
 * @param data Object containing basic Application info
 */
sealed class ExtractionResult(override val data: ApplicationModel?) : ProgressResult<ApplicationModel?> {
    data class Init(val tasks: Int): ExtractionResult(null)
    data class SuccessSingle(val app: ApplicationModel): ExtractionResult(app)
    data class SuccessMultiple(val app: ApplicationModel, val backupsCount: Int): ExtractionResult(app)
    data class Progress(val app: ApplicationModel, val progressIncrement: Int): ExtractionResult(app)
    data class Failure(val app: ApplicationModel, val errorMessage: String?): ExtractionResult(app)

    data object NoSaveDir: ExtractionResult(null)
    data object None : ExtractionResult(null)
}
