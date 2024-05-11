package domilopment.apkextractor.domain.usecase.installer

import android.app.Activity
import android.net.Uri
import domilopment.apkextractor.dependencyInjection.installation.InstallationRepository
import domilopment.apkextractor.utils.InstallApkResult
import kotlinx.coroutines.flow.Flow

interface InstallUseCase {
    operator fun <T : Activity> invoke(fileUri: Uri, cls: Class<T>): Flow<InstallApkResult>
}

class InstallUseCaseImpl(
    private val installationRepository: InstallationRepository,
) : InstallUseCase {
    override operator fun <T : Activity> invoke(
        fileUri: Uri, cls: Class<T>
    ): Flow<InstallApkResult> {
        return installationRepository.install(fileUri, cls)
    }
}