package domilopment.apkextractor.data.appList

/**
 * Class used as return flow for saving one or multiple apk files to public storage
 * @param app Package info of application that was saved
 */
sealed class ExtractionResult(open val app: ApplicationModel?) {
    data class SuccessSingle(override val app: ApplicationModel): ExtractionResult(app)
    data class SuccessMultiple(override val app: ApplicationModel, val backupsCount: Int): ExtractionResult(app)
    data class Failure(override val app: ApplicationModel, val errorMessage: String?): ExtractionResult(app)
    data object None : ExtractionResult(null)
}