package domilopment.apkextractor.data.model.appList

import android.net.Uri
import domilopment.apkextractor.data.model.tasks.ProgressResult
import domilopment.apkextractor.data.model.tasks.TaskFailure

/**
 * Class used as return flow for creating share uri for one or multiple apk files
 */
sealed interface ShareResult {
    sealed class ShareSuccess(override val data: Uri?) : ShareResult, ProgressResult<Uri> {
        data class SuccessSingle(val uri: Uri): ShareSuccess(uri)
        data class SuccessMultiple(val uris: ArrayList<Uri>): ShareSuccess(uris.first())
    }

    sealed interface ShareError: ShareResult, TaskFailure {
        data class Failure(
            override val errorMessage: String?,
            override val throwable: Throwable? = null,
            override val extra: ApplicationModel
        ): ShareError

        data object None : ShareError {
            override val errorMessage: String? = null
            override val throwable: Throwable? = null
            override val extra: Any? = null
        }
    }
}
