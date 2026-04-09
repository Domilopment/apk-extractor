package domilopment.apkextractor.ui.model.installation

import android.net.Uri

sealed class InstallationResultType(open val packageName: String?) {
    sealed class Success(override val packageName: String?) : InstallationResultType(packageName) {
        data class Installed(override val packageName: String?) : Success(packageName)
        data class Uninstalled(override val packageName: String?) : Success(packageName)
    }

    sealed class Failure(override val packageName: String?, open val errorMessage: String?) :
        InstallationResultType(packageName) {
        data class Install(
            override val packageName: String?,
            val fileUri: Uri,
            val error: InstallApkError
        ) : Failure(packageName, error.errorMessage)

        data class Uninstall(
            override val packageName: String?, override val errorMessage: String?
        ) : Failure(packageName, errorMessage)

        data class Security(override val errorMessage: String?) : Failure(null, errorMessage)
    }
}
