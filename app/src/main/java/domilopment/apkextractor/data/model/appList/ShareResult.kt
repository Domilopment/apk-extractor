package domilopment.apkextractor.data.model.appList

import android.net.Uri

/**
 * Class used as return flow for creating share uri for one or multiple apk files
 */
sealed interface ShareResult {
    data class Init(val tasks: Int): ShareResult
    data class SuccessSingle(val uri: Uri): ShareResult
    data class SuccessMultiple(val uris: ArrayList<Uri>): ShareResult
    data object Progress: ShareResult
    data object None: ShareResult
}
