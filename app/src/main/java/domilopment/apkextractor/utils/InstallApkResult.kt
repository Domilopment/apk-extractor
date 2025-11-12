package domilopment.apkextractor.utils

import android.content.pm.PackageInstaller
import domilopment.apkextractor.data.model.appList.ApplicationModel

sealed interface InstallApkResult {
    val packageName: String?

    data class OnPrepare(
        val session: PackageInstaller.Session, val sessionId: Int,
    ) : InstallApkResult {
        override val packageName: String? = null
    }

    data class OnProgress(override val packageName: String?, val progress: Float) : InstallApkResult

    sealed class OnFinish(override val packageName: String?) : InstallApkResult {
        data class OnSuccess(val app: ApplicationModel?) : OnFinish(app?.appPackageName)
        data class OnError(override val packageName: String?, val error: String) :
            OnFinish(packageName)

        data class OnFinished(override val packageName: String?) : OnFinish(packageName)

        data class OnExtern(override val packageName: String?): OnFinish(packageName)
    }
}