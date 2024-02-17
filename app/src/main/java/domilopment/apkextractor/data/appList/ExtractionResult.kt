package domilopment.apkextractor.data.appList

import android.net.Uri

sealed class ExtractionResult(open val app: ApplicationModel) {
    data class SuccessSingle(override val app: ApplicationModel): ExtractionResult(app)
    data class SuccessMultiple(override val app: ApplicationModel, val backupsCount: Int): ExtractionResult(app)
    data class Failure(override val app: ApplicationModel, val errorMessage: String?): ExtractionResult(app)
}