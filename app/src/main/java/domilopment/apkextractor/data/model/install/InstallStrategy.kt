package domilopment.apkextractor.data.model.install

import android.app.Activity
import android.net.Uri
import domilopment.apkextractor.data.repository.installation.InstallationRepository
import kotlinx.coroutines.flow.Flow

sealed interface InstallStrategy {
    fun install(repository: InstallationRepository, fileUri: Uri): Flow<InstallationCallback>

    class Internal<T : Activity>(private val statusReceiver: Class<T>) : InstallStrategy {
        override fun install(
            repository: InstallationRepository, fileUri: Uri
        ): Flow<InstallationCallback> = repository.install(fileUri, statusReceiver)
    }

    data object External : InstallStrategy {
        override fun install(
            repository: InstallationRepository, fileUri: Uri
        ): Flow<InstallationCallback> = repository.extern(fileUri)
    }
}