package domilopment.apkextractor.utils

import android.net.Uri

sealed class SaveApkResult {
    data class Success(val uri: Uri): SaveApkResult()
    data class Failure(val errorMessage: String?): SaveApkResult()
}
