package domilopment.apkextractor.utils

import android.content.pm.PackageInstaller
import domilopment.apkextractor.data.model.appList.ApplicationModel

sealed class InstallApkResult(open val packageName: String?) {
    data class OnPrepare(val session: PackageInstaller.Session, val sessionId: Int) :
        InstallApkResult("")

    data class OnProgress(override val packageName: String?, val progress: Float) :
        InstallApkResult(packageName)

    data class OnSuccess(val app: ApplicationModel?) : InstallApkResult(app?.appPackageName)
    data class OnFail(override val packageName: String?) : InstallApkResult(packageName)
}