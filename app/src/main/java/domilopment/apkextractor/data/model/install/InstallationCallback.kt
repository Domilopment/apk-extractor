package domilopment.apkextractor.data.model.install

import android.content.pm.PackageInstaller
import android.net.Uri

sealed interface InstallationCallback {
    val packageName: String?

    data class OnPrepare(
        val session: PackageInstaller.Session, val sessionId: Int,
    ) : InstallationCallback {
        override val packageName: String? = null
    }

    data class OnProgress(override val packageName: String?, val progress: Float) : InstallationCallback

    sealed interface InstallationResult : InstallationCallback {
        data class OnFinished(override val packageName: String?, val success: Boolean) : InstallationResult
        data class OnError(override val packageName: String?, val fileUri: Uri, val error: InstallationError) : InstallationResult
        data class OnExtern(override val packageName: String?): InstallationResult
    }
}