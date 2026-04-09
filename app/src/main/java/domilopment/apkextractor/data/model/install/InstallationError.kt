package domilopment.apkextractor.data.model.install

sealed interface InstallationError {
    val throwable: Throwable

    data class IOException(override val throwable: Throwable) : InstallationError
    data class SessionCreationException(override val throwable: Throwable) : InstallationError
    data class ActivityNotFoundException(override val throwable: Throwable) : InstallationError
    data class UnknownException(override val throwable: Throwable) : InstallationError
}
