package domilopment.apkextractor.data

sealed class ApkInstallationResultType(open val packageName: String?) {
    data class Success(override val packageName: String?) : ApkInstallationResultType(packageName)
    data class Failure(override val packageName: String?, val errorMessage: String?) :
        ApkInstallationResultType(packageName)
}

data class ApkInstallationResult(val result: ApkInstallationResultType)
