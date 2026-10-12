package domilopment.apkextractor.data.model.appList

import android.net.Uri
import domilopment.apkextractor.data.model.tasks.ProgressResult

/**
 * Class used as return flow for creating share uri for one or multiple apk files
 */
sealed class ShareResult(override val data: Uri?) : ProgressResult<Uri> {
    data class Init(val tasks: Int): ShareResult(null)
    data class SuccessSingle(val uri: Uri): ShareResult(uri)
    data class SuccessMultiple(val uris: ArrayList<Uri>): ShareResult(uris.first())
    data object Progress: ShareResult(null)
    data object None: ShareResult(null)
}
