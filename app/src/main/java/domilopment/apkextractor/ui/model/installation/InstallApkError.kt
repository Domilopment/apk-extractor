package domilopment.apkextractor.ui.model.installation

import android.net.Uri
import androidx.annotation.StringRes
import domilopment.apkextractor.R

/**
 * Represents specific, user-facing errors that can occur during the APK installation process.
 * Each error type contains data to construct a helpful message for the user.
 */
sealed class InstallApkError(
    @param:StringRes open val messageResId: Int,
    open val errorMessage: String? = null,
) {
    /**
     * The installation was actively aborted by the user from the system's installation dialog.
     * This is a common and expected scenario.
     * Corresponds to [android.content.pm.PackageInstaller.STATUS_FAILURE_ABORTED].
     */
    class AbortedByUser() :
        InstallApkError(R.string.installation_error_aborted)

    /**
     * A generic failure occurred, but it wasn't one of the other specific cases.
     * This can happen for a variety of reasons, like storage issues or system policy.
     * Corresponds to [android.content.pm.PackageInstaller.STATUS_FAILURE].
     */
    class Generic(override val errorMessage: String?) :
        InstallApkError(R.string.installation_error_generic, errorMessage)

    /**
     * The installation was blocked by a system policy (e.g., Device Admin).
     * Corresponds to [android.content.pm.PackageInstaller.STATUS_FAILURE_BLOCKED].
     */
    class BlockedByPolicy() :
        InstallApkError(R.string.installation_error_blocked)

    /**
     * The package is invalid, corrupt, or unsigned.
     * Corresponds to [android.content.pm.PackageInstaller.STATUS_FAILURE_INVALID].
     */
    class InvalidApk() :
        InstallApkError(R.string.installation_error_invalid)

    /**
     * The new package has a conflict with an existing, different package
     * (e.g., different signature).
     * Corresponds to [android.content.pm.PackageInstaller.STATUS_FAILURE_CONFLICT].
     */
    class Conflict() :
        InstallApkError(R.string.installation_error_conflict)

    /**
     * The system ran out of storage space while trying to install the package.
     * Corresponds to [android.content.pm.PackageInstaller.STATUS_FAILURE_STORAGE].
     */
    class InsufficientStorage() :
        InstallApkError(R.string.installation_error_storage)

    /**
     * The package is not compatible with the device's API level (SDK version).
     * Corresponds to [android.content.pm.PackageInstaller.STATUS_FAILURE_INCOMPATIBLE].
     */
    class Incompatible() :
        InstallApkError(R.string.installation_error_incompatible)

    /**
     * An error occurred while trying to create the installation session itself,
     * before any files were transferred. This can happen due to an I/O error or
     * if the system blocks session creation (SecurityException).
     */
    class SessionCreationFailure(val fileUri: Uri, override val errorMessage: String?) :
        InstallApkError(R.string.installation_error_session_creation, errorMessage)

    /**
     * An error occurred while reading the APK file from storage.
     */
    class FileReadError(val fileUri: Uri, override val errorMessage: String?) :
        InstallApkError(R.string.installation_error_file_read, errorMessage)

    class ExternNotFoundError(override val errorMessage: String?) :
        InstallApkError(R.string.installation_error_file_read, errorMessage)
}