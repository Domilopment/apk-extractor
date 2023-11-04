package domilopment.apkextractor.utils

import android.net.Uri

sealed class ExtractionResult {
    data class Success(val uri: Uri): ExtractionResult()
    data class Failure(val errorMessage: String?): ExtractionResult()
}
