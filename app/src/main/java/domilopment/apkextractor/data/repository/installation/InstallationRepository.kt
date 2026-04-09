package domilopment.apkextractor.data.repository.installation

import android.app.Activity
import android.net.Uri
import domilopment.apkextractor.di.installation.InstallationService
import domilopment.apkextractor.data.model.install.InstallationCallback
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface InstallationRepository {
    fun <T : Activity> install(fileUri: Uri, statusReceiver: Class<T>): Flow<InstallationCallback>
    suspend fun <T : Activity> uninstall(packageName: String, statusReceiver: Class<T>)

    fun extern(fileUri: Uri): Flow<InstallationCallback>
}

class InstallationRepositoryImpl @Inject constructor(
    private val installationService: InstallationService,
) : InstallationRepository {
    override fun <T : Activity> install(
        fileUri: Uri, statusReceiver: Class<T>
    ): Flow<InstallationCallback> {
        return installationService.install(fileUri, statusReceiver)
    }

    override suspend fun <T : Activity> uninstall(packageName: String, statusReceiver: Class<T>) {
        installationService.uninstall(packageName, statusReceiver)
    }

    override fun extern(fileUri: Uri): Flow<InstallationCallback> {
        return installationService.fallbackInstall(fileUri)
    }
}