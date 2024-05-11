package domilopment.apkextractor.dependencyInjection.installation

import android.app.Activity
import android.net.Uri
import domilopment.apkextractor.utils.InstallApkResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface InstallationRepository {
    fun <T : Activity> install(fileUri: Uri, statusReceiver: Class<T>): Flow<InstallApkResult>
    suspend fun <T : Activity> uninstall(packageName: String, statusReceiver: Class<T>)
}

class InstallationRepositoryImpl @Inject constructor(
    private val installationService: InstallationService,
) : InstallationRepository {
    override fun <T : Activity> install(
        fileUri: Uri, statusReceiver: Class<T>
    ): Flow<InstallApkResult> {
        return installationService.install(fileUri, statusReceiver)
    }

    override suspend fun <T : Activity> uninstall(packageName: String, statusReceiver: Class<T>) {
        installationService.uninstall(packageName, statusReceiver)
    }

}