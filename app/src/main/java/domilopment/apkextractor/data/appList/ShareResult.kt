package domilopment.apkextractor.data.appList

import android.net.Uri

sealed interface ShareResult {
    data class SuccessSingle(val uri: Uri): ShareResult
    data class SuccessMultiple(val uris: ArrayList<Uri>): ShareResult
    data object None: ShareResult
}
